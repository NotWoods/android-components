/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.sessionhistory

import android.annotation.TargetApi
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.ViewConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.getSessionWithIdOrSelected
import mozilla.components.concept.engine.history.HistoryList
import mozilla.components.support.base.feature.LifecycleAwareFeature

class SessionHistoryFeature(
    private val sessionManager: SessionManager,
    private val historyView: SessionHistoryView,
    private val sessionId: String? = null
) : LifecycleAwareFeature {

    private val scope = MainScope()
    private var checkLongPressJob: Job? = null

    override fun start() = Unit

    override fun stop() {
        scope.cancel()
    }

    fun showHistory(action: Action): Boolean {
        val session = sessionManager.getSessionWithIdOrSelected(sessionId) ?: return false

        val slicedList = when (action) {
            Action.ALL -> session.historyList
            Action.BACK -> session.historyList.dropLastWhile { !it.selected }
            Action.FORWARD -> session.historyList.dropWhile { !it.selected }
        }
        return historyView.show(slicedList)
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // Bug 1304688: Android N has broken passing onKeyLongPress events for the back button, so we
        // instead copy the long-press-handler technique from Android's KeyButtonView.
        // - For short presses, we cancel the callback in onKeyUp
        // - For long presses, the normal keypress is marked as cancelled, hence won't be handled elsewhere
        //   (but Android still provides the haptic feedback), and the runnable is run.
        if (SDK_INT == Build.VERSION_CODES.N && keyCode == KEYCODE_BACK) {
            checkLongPressJob?.cancel()
            checkLongPressJob = scope.launch { checkLongPress() }
        }

        return false
    }

    fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (SDK_INT == Build.VERSION_CODES.N && keyCode == KEYCODE_BACK) {
            checkLongPressJob?.cancel()
        }

        return false
    }

    /**
     * This will detect if the key pressed is back. If so, will show the history.
     */
    fun onLongKeyPress(keyCode: Int, event: KeyEvent): Boolean {
        // onKeyLongPress is broken in Android N, see onKeyDown() for more information. We add a version
        // check here to match our fallback code in order to avoid handling a long press twice (which
        // could happen if newer versions of android and/or other vendors were to  fix this problem).
        return if (SDK_INT != Build.VERSION_CODES.N && keyCode == KEYCODE_BACK) {
            onBackLongPress()
        } else {
            false
        }
    }

    private fun onBackLongPress(): Boolean =
        if (historyView.isShowing) true else showHistory(Action.ALL)

    @TargetApi(Build.VERSION_CODES.N)
    private suspend fun checkLongPress() {
        // Only initialise the function if we are on N.
        // See onKeyDown() for more details of the back-button long-press workaround
        if (SDK_INT == Build.VERSION_CODES.N) {
            delay(ViewConfiguration.getLongPressTimeout().toLong())
            onBackLongPress()
        }
    }

    enum class Action {
        ALL, BACK, FORWARD;
    }
}

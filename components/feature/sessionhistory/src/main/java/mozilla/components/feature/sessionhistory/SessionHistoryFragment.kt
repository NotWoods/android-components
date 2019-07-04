/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.sessionhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.concept.engine.history.HistoryItem
import java.util.ArrayList
import com.intellij.util.VisibilityUtil.setVisibility



class SessionHistoryFragment : Fragment(), View.OnClickListener {

    private val historyList: List<HistoryItem> by lazy {
        arguments?.getParcelableArrayList(ARG_LIST) ?: emptyList()
    }

    private lateinit var dialogList: RecyclerView
    private var container: ViewGroup? = null

    private var backStackId = -1
    private var dismissed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.apply { backStackId = getInt(BACK_STACK_ID, -1) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.mozac_feature_sessionhistory_history_layout, container, false)

        this.container = container?.apply {
            visibility = View.VISIBLE
        }

        view.setOnClickListener(this)
        dialogList = view.findViewById(R.id.session_history_list)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val urlAdapter = SessionHistoryAdapter(activity, historyList)
        dialogList.adapter = urlAdapter
    }

    /**
     * Since the fragment view fills the entire screen, any clicks outside of the history
     * RecyclerView will end up here.
     */
    override fun onClick(v: View) {
        if (v == container) dismiss()
    }

    override fun onPause() {
        super.onPause()
        onDismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (backStackId > -1) {
            outState.putInt(BACK_STACK_ID, backStackId)
        }
    }

    /**
     * Adds this fragment to activity state with [containerViewId] as parent.
     * This is similar in functionality to DialogFragment.show() except that containerId is provided here.
     */
    fun show(containerViewId: Int, transaction: FragmentTransaction, tag: String) {
        dismissed = false
        transaction.replace(containerViewId, this, tag)
        transaction.addToBackStack(tag)
        // Populating the tab history requires a gecko call (which can be slow) - therefore the app
        // state by the time we try to show this fragment is unknown, and we could be in the
        // middle of shutting down:
        backStackId = transaction.commitAllowingStateLoss()
    }

    /**
     * Pop the fragment from backstack if it exists.
     */
    fun dismiss() {
        if (backStackId >= 0) {
            fragmentManager!!.popBackStackImmediate(backStackId, POP_BACK_STACK_INCLUSIVE)
            backStackId = -1
        }
        onDismiss()
    }

    private fun onDismiss() {
        if (dismissed) return
        dismissed = true

        container?.visibility = View.GONE
    }

    companion object {
        private const val ARG_LIST = "historyPageList"
        private const val BACK_STACK_ID = "backStateId"

        fun newInstance(historyList: List<HistoryItem>) = SessionHistoryFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ARG_LIST, historyList as ArrayList<HistoryItem>)
            }
        }
    }
}

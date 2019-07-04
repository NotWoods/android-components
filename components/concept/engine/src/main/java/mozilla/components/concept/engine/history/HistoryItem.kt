/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.concept.engine.history

import android.os.Parcel
import android.os.Parcelable
import mozilla.components.support.ktx.android.os.readBoolean
import mozilla.components.support.ktx.android.os.writeBoolean

/**
 * A representation of an entry in browser history.
 */
data class HistoryItem(
    val title: String,
    val uri: String,
    val selected: Boolean
) : Parcelable {

    constructor(source: Parcel) : this(
        title = source.readString()!!,
        uri = source.readString()!!,
        selected = source.readBoolean()
    )

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(title)
        out.writeString(uri)
        out.writeBoolean(selected)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<HistoryItem> {
        override fun createFromParcel(source: Parcel) = HistoryItem(source)
        override fun newArray(size: Int) = arrayOfNulls<HistoryItem?>(size)
    }
}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.support.ktx.android.os

import android.os.Parcel

fun Parcel.readBoolean() = readInt() != 0

fun Parcel.writeBoolean(value: Boolean) {
    writeInt(if (value) 1 else 0)
}

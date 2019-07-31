/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.support.utils

import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.ColorInt
import androidx.annotation.Size

object ColorUtils {

    private const val RGB_COMPONENTS_LENGTH = 3L
    private const val RGB_MAX_INT = 255

    /**
     * Convert RGB (red-green-blue) components to a color int.
     *
     * - rgb[0] is Red [0...1]
     * - rgb[1] is Green [0...1]
     * - rgb[2] is Blue [0...1]
     */
    @Suppress("FunctionNaming")
    @ColorInt
    fun RGBToColor(@Size(RGB_COMPONENTS_LENGTH) rgb: FloatArray): Int =
        if (SDK_INT >= Build.VERSION_CODES.O) {
            Color.rgb(rgb[0], rgb[1], rgb[2])
        } else {
            Color.rgb(rgbFloatToInt(rgb[0]), rgbFloatToInt(rgb[1]), rgbFloatToInt(rgb[2]))
        }

    /**
     * Convert the color int to its RGB (red-green-blue) components.
     *
     * - outRgb[0] is Red [0...1]
     * - outRgb[1] is Green [0...1]
     * - outRgb[2] is Blue [0...1]
     */
    fun colorToRGB(@ColorInt color: Int, @Size(RGB_COMPONENTS_LENGTH) outRgb: FloatArray) {
        outRgb[0] = rgbIntToFloat(Color.red(color))
        outRgb[1] = rgbIntToFloat(Color.green(color))
        outRgb[2] = rgbIntToFloat(Color.blue(color))
    }

    private fun rgbFloatToInt(rgbComponent: Float) = (rgbComponent * RGB_MAX_INT).toInt()
    private fun rgbIntToFloat(rgbComponent: Int) = rgbComponent / RGB_MAX_INT.toFloat()

    /**
     * Get text color (white or black) that is readable on top of the provided background color.
     */
    @JvmStatic
    fun getReadableTextColor(@ColorInt backgroundColor: Int): Int {
        return if (isDark(backgroundColor)) Color.WHITE else Color.BLACK
    }

    /**
     * Returns true if the color is dark enough that white text should be used on top of it.
     */
    @JvmStatic
    @SuppressWarnings("MagicNumber")
    fun isDark(@ColorInt color: Int): Boolean {
        val greyValue = grayscaleFromRGB(color)
        // 186 chosen rather than the seemingly obvious 128 because of gamma.
        return greyValue < 186
    }

    @SuppressWarnings("MagicNumber")
    private fun grayscaleFromRGB(@ColorInt color: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        // Magic weighting taken from a stackoverflow post, supposedly related to how
        // humans perceive color.
        return (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
    }
}

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.prompts.color

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Size
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils.HSLToColor
import androidx.core.graphics.ColorUtils.colorToHSL
import mozilla.components.feature.prompts.R
import mozilla.components.support.utils.ColorUtils.RGBToColor
import mozilla.components.support.utils.ColorUtils.colorToRGB

class ColorSliders @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle),
    View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private val controlType: TextView
    @Size(NUM_COMPONENTS) private val inputs: Array<Pair<SeekBar, EditText>>
    @Size(NUM_COMPONENTS) private var components = floatArrayOf(0f, 0f, 0f)

    var mode: Mode = Mode.RGB
        set(value) {
            // Pull out the color int value from the old backing array and update the mode
            val colorInt = color
            field = value
            // Put in the old color, which uses a new setter appropriate to the mode.
            color = colorInt

            controlType.setText(context.getString(mode.typeLabel))
            inputs.forEachIndexed { index, (slider, editText) ->
                val label = context.getString(value.labels[index])
                slider.max = value.dataTypes[index].max
                slider.contentDescription = label
                slider.progressDrawable = context.getDrawable(mode.sliderBackgrounds[index])
                editText.hint = label
            }
        }

    @get:ColorInt
    var color: Int
        get() = when (mode) {
            Mode.RGB -> RGBToColor(components)
            Mode.HSL -> HSLToColor(components)
        }
        set(@ColorInt value) {
            when (mode) {
                Mode.RGB -> colorToRGB(value, components)
                Mode.HSL -> colorToHSL(value, components)
            }
            updateSliders()
            updateEditTexts()
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.mozac_feature_prompts_color_sliders, this)

        controlType = findViewById(R.id.controlType)
        inputs = arrayOf(
            Pair(findViewById(R.id.topSlider), findViewById(R.id.topValue)),
            Pair(findViewById(R.id.midSlider), findViewById(R.id.midValue)),
            Pair(findViewById(R.id.bottomSlider), findViewById(R.id.bottomValue))
        )
        mode = Mode.RGB

        controlType.setOnClickListener(this)
        inputs.forEachIndexed { index, (slider, editText) ->
            slider.setOnSeekBarChangeListener(this)
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

                override fun afterTextChanged(s: Editable) {
                    val componentInt = s.toString().toIntOrNull() ?: 0
                    val newValue = mode.dataTypes[index].intToFloat(componentInt)
                    components[index] = newValue
                    slider.progress = componentInt
                }
            })
        }
    }

    /**
     * Switches the current mode between RGB and HSL.
     */
    override fun onClick(v: View?) {
        mode = when (mode) {
            Mode.RGB -> Mode.HSL
            Mode.HSL -> Mode.RGB
        }
    }

    override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val sliderIndex = inputs.indexOfFirst { (itSlider) -> itSlider === slider }
            if (sliderIndex !in 0 until NUM_COMPONENTS) return

            val newValue = mode.dataTypes[sliderIndex].intToFloat(progress)
            components[sliderIndex] = newValue

            val editText = inputs[sliderIndex].second
            editText.setText(progress.toString())
        }
    }

    override fun onStartTrackingTouch(slider: SeekBar) = Unit
    override fun onStopTrackingTouch(slider: SeekBar) = Unit

    private fun updateSliders() {
        inputs.forEachIndexed { index, (slider) ->
            slider.progress = mode.dataTypes[index].floatToInt(components[index])
        }
    }

    private fun updateEditTexts() {
        inputs.forEachIndexed { index, (_, editText) ->
            editText.setText(mode.dataTypes[index].floatToInt(components[index]).toString())
        }
    }

    sealed class Mode(
        @StringRes val typeLabel: Int,
        @StringRes @Size(NUM_COMPONENTS) val labels: IntArray,
        @DrawableRes @Size(NUM_COMPONENTS) val sliderBackgrounds: IntArray,
        @Size(NUM_COMPONENTS) val dataTypes: Array<DataType>
    ) {
        object RGB : Mode(
            R.string.mozac_feature_prompts_color_rgb,
            intArrayOf(
                R.string.mozac_feature_prompts_color_red,
                R.string.mozac_feature_prompts_color_green,
                R.string.mozac_feature_prompts_color_blue
            ),
            intArrayOf(
                R.drawable.color_slider_red,
                R.drawable.color_slider_green,
                R.drawable.color_slider_blue
            ),
            arrayOf(DataType.HEX, DataType.HEX, DataType.HEX)
        )
        object HSL : Mode(
            R.string.mozac_feature_prompts_color_hsl,
            intArrayOf(
                R.string.mozac_feature_prompts_color_hue,
                R.string.mozac_feature_prompts_color_saturation,
                R.string.mozac_feature_prompts_color_lightness
            ),
            intArrayOf(
                R.drawable.color_slider_hue,
                R.drawable.color_slider_saturation,
                R.drawable.color_slider_lightness
            ),
            arrayOf(DataType.DEGREE, DataType.PERCENT, DataType.PERCENT)
        )
    }

    @Suppress("MagicNumber")
    sealed class DataType(val max: Int, val suffix: String) {
        open fun intToFloat(int: Int) = int.toFloat() / max
        open fun floatToInt(float: Float) = (float * max).toInt()

        object HEX : DataType(255, "")
        object PERCENT : DataType(100, "%")
        object DEGREE : DataType(359, "°") {
            // Degree types are [0...360) rather than [0...1]
            override fun intToFloat(int: Int) = int.toFloat()
            override fun floatToInt(float: Float) = float.toInt()
        }
    }

    companion object {
        private const val NUM_COMPONENTS = 3L
    }
}

package ke.tang.ruler.sample.kt

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ke.tang.ruler.*
import ke.tang.ruler.sample.MainActivity
import ke.tang.ruler.sample.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val icons by lazy { resources.obtainTypedArray(R.array.icons) }
    private lateinit var state: DefaultState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        ruler.onRulerValueChangeListener = object : OnRulerValueChangeListener {
            override fun onRulerValueChanged(value: Int, displayValue: String) {
                result?.text = displayValue
            }
        }
        state = DefaultState(
                ruler.stepWidth,
                ruler.scaleColor,
                ruler.rulerColor,
                ruler.sectionScaleCount,
                ruler.indicator,
                ruler.scaleMinHeight,
                ruler.scaleMaxHeight,
                ruler.scaleSize,
                ruler.maxValue,
                ruler.minValue,
                ruler.value,
                ruler.textSize,
                ruler.textColor
        )

        stepWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ruler.stepWidth = (state.stepWidth + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        scaleColor.adapter = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_dropdown_item)
        scaleColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> ruler.scaleColor = state.scaleColor
                    1 -> ruler.setScaleColor(Color.RED)
                    2 -> ruler.setScaleColor(Color.BLUE)
                    3 -> ruler.setScaleColor(Color.YELLOW)
                }
            }
        }

        rulerColor.adapter = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_dropdown_item)
        rulerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> ruler.rulerColor = state.rulerColor
                    1 -> ruler.setRulerColor(Color.RED)
                    2 -> ruler.setRulerColor(Color.BLUE)
                    3 -> ruler.setRulerColor(Color.YELLOW)
                }
            }
        }

        sectionScaleCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ruler.sectionScaleCount = (state.sectionScaleCount + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        indicators.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.defaultIndicator -> ruler.indicator = state.indicator
                R.id.dashedLineIndicator -> ruler.setIndicator(R.drawable.dashed_indicator)
                R.id.arrowIndicator -> ruler.setIndicator(R.drawable.arrow_indicator_tint)
            }
        }

        scaleMinHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ruler.scaleMinHeight = (state.scaleMinHeight + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, getResources().getDisplayMetrics()) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        scaleMaxHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ruler.scaleMaxHeight = (state.scaleMaxHeight + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        scaleSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ruler.scaleSize = (state.scaleSize + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        rulerSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ruler.rulerSize = (state.scaleSize + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        maxValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val maxValue: Int = state.maxValue - state.maxValue / 100 * progress
                if (maxValue >= ruler.minValue) {
                    ruler.maxValue = maxValue
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.notification_max_value_exceed, ruler.maxValue), Toast.LENGTH_LONG).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        minValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minValue: Int = state.minValue + state.maxValue / 100 * progress
                if (minValue <= ruler.maxValue) {
                    ruler.minValue = minValue
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.notification_min_value_exceed, ruler.maxValue), Toast.LENGTH_LONG).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        submitValue.setOnClickListener {
            try {
                ruler.value = value.text.toString().toInt()
            } catch (e: Exception) {
            }
        }

        textSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ruler.textSize = state.textSize + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics) / 100 * progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        textColor.adapter = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_dropdown_item)
        textColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> ruler.textColor = state.textColor
                    1 -> ruler.setTextColor(Color.RED)
                    2 -> ruler.setTextColor(Color.BLUE)
                    3 -> ruler.setTextColor(Color.YELLOW)
                }
            }

        }

        formatText.adapter = ArrayAdapter.createFromResource(this, R.array.formatters, android.R.layout.simple_spinner_dropdown_item)
        formatText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> ruler.rulerValueFormatter = null
                    1 -> ruler.rulerValueFormatter = MoneyRulerValueFormatter()
                }
            }

        }

        customMarker.setOnClickListener {
            val marker = DrawableMarker(icons.getResourceId((Math.random() * icons.length()).toInt(), View.NO_ID), ruler.value, null)
            marker.onMarkerClickListener = object : OnMarkerClickListener {
                override fun onMarkerClick(m: Marker) {
                    Toast.makeText(this@MainActivity, R.string.notification_marker_clicked, Toast.LENGTH_SHORT).show()
                }
            }
            ruler.addMarker(marker)
        }
    }

    private data class DefaultState(
            val stepWidth: Int,
            val scaleColor: ColorStateList?,
            val rulerColor: ColorStateList?,
            val sectionScaleCount: Int,
            val indicator: Drawable?,
            val scaleMinHeight: Int,
            val scaleMaxHeight: Int,
            val scaleSize: Int,
            val maxValue: Int,
            val minValue: Int,
            val value: Int,
            val textSize: Float,
            val textColor: ColorStateList?
    )
}
package ke.tang.ruler.sample

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
import ke.tang.ruler.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val icons by lazy { resources.obtainTypedArray(R.array.icons) }
    private lateinit var state: DefaultState
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        binding.ruler.onRulerValueChangeListener = object : OnRulerValueChangeListener {
            override fun onRulerValueChanged(value: Int, displayValue: String) {
                binding.result.text = displayValue
            }
        }
        state = DefaultState(
            binding.ruler.stepWidth,
            binding.ruler.scaleColor,
            binding.ruler.rulerColor,
            binding.ruler.sectionScaleCount,
            binding.ruler.indicator,
            binding.ruler.scaleMinHeight,
            binding.ruler.scaleMaxHeight,
            binding.ruler.scaleSize,
            binding.ruler.maxValue,
            binding.ruler.minValue,
            binding.ruler.value,
            binding.ruler.textSize,
            binding.ruler.textColor
        )

        binding.stepWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ruler.stepWidth = (state.stepWidth + TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    50f,
                    resources.displayMetrics
                ) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.scaleColor.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.colors,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.scaleColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> binding.ruler.scaleColor = state.scaleColor
                    1 -> binding.ruler.setScaleColor(Color.RED)
                    2 -> binding.ruler.setScaleColor(Color.BLUE)
                    3 -> binding.ruler.setScaleColor(Color.YELLOW)
                }
            }
        }

        binding.rulerColor.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.colors,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.rulerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> binding.ruler.rulerColor = state.rulerColor
                    1 -> binding.ruler.setRulerColor(Color.RED)
                    2 -> binding.ruler.setRulerColor(Color.BLUE)
                    3 -> binding.ruler.setRulerColor(Color.YELLOW)
                }
            }
        }

        binding.sectionScaleCount.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ruler.sectionScaleCount =
                    (state.sectionScaleCount + TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        30f,
                        resources.displayMetrics
                    ) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding.indicators.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.defaultIndicator -> binding.ruler.indicator = state.indicator
                R.id.dashedLineIndicator -> binding.ruler.setIndicator(R.drawable.dashed_indicator)
                R.id.arrowIndicator -> binding.ruler.setIndicator(R.drawable.arrow_indicator_tint)
            }
        }

        binding.scaleMinHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ruler.scaleMinHeight = (state.scaleMinHeight + TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    100f,
                    getResources().getDisplayMetrics()
                ) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.scaleMaxHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ruler.scaleMaxHeight = (state.scaleMaxHeight + TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    100f,
                    resources.displayMetrics
                ) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.scaleSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ruler.scaleSize = (state.scaleSize + TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    100f,
                    resources.displayMetrics
                ) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.rulerSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ruler.rulerSize = (state.scaleSize + TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    100f,
                    resources.displayMetrics
                ) / 100 * progress).toInt()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.maxValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val maxValue: Int = state.maxValue - state.maxValue / 100 * progress
                if (maxValue >= binding.ruler.minValue) {
                    binding.ruler.maxValue = maxValue
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.notification_max_value_exceed, binding.ruler.maxValue),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.minValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minValue: Int = state.minValue + state.maxValue / 100 * progress
                if (minValue <= binding.ruler.maxValue) {
                    binding.ruler.minValue = minValue
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.notification_min_value_exceed, binding.ruler.maxValue),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.submitValue.setOnClickListener {
            try {
                binding.ruler.value = binding.value.text.toString().toInt()
            } catch (e: Exception) {
            }
        }

        binding.textSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.ruler.textSize = state.textSize + TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    40f,
                    resources.displayMetrics
                ) / 100 * progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        binding.textColor.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.colors,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.textColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> binding.ruler.textColor = state.textColor
                    1 -> binding.ruler.setTextColor(Color.RED)
                    2 -> binding.ruler.setTextColor(Color.BLUE)
                    3 -> binding.ruler.setTextColor(Color.YELLOW)
                }
            }

        }

        binding.formatText.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.formatters,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.formatText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> binding.ruler.rulerValueFormatter = null
                    1 -> binding.ruler.rulerValueFormatter = MoneyRulerValueFormatter()
                }
            }

        }

        binding.customMarker.setOnClickListener {
            val marker = DrawableMarker(
                icons.getResourceId(
                    (Math.random() * icons.length()).toInt(),
                    View.NO_ID
                ), binding.ruler.value, null
            )
            marker.onMarkerClickListener = object : OnMarkerClickListener {
                override fun onMarkerClick(m: Marker) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.notification_marker_clicked,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            binding.ruler.addMarker(marker)
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
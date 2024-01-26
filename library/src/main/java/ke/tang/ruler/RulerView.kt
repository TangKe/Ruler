package ke.tang.ruler

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.core.math.MathUtils
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class RulerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.rulerViewStyle) : View(context, attrs, defStyleAttr) {
    var stepWidth = 0
        set(value) {
            field = max(1, value)
            rebuildContentOffsetRange()
            this.value = this.value
        }
    var rulerValueFormatter: RulerValueFormatter? = null
        set(value) {
            field = value
            notifyValueChanged()
            invalidate()
        }
    var scaleColor: ColorStateList? = null
        set(value) {
            field = value
            invalidate()
        }

    var rulerColor: ColorStateList? = null
        set(value) {
            field = value
            invalidate()
        }
    var sectionScaleCount: Int = 0
        set(value) {
            field = max(0, value)
            invalidate()
        }
    var indicator: Drawable? = null
        set(value) {
            field?.callback = null
            field = value
            field?.callback = this
            invalidate()

        }
    var scaleMinHeight: Int = 0
        set(value) {
            field = max(0, value)
            requestLayout()
            invalidate()
        }
    var scaleMaxHeight: Int = 0
        set(value) {
            field = max(0, value)
            requestLayout()
            invalidate()
        }
    var scaleSize: Int = 0
        set(value) {
            field = max(0, value)
            invalidate()
        }
    var rulerSize: Int = 0
        set(value) {
            field = max(0, value)
            requestLayout()
            invalidate()
        }

    @IntRange(from = 0, to = MAX_VALUE.toLong())
    var maxValue: Int = 0
        set(value) {
            require(value >= minValue) { "最大值: $value 不能小于最小值: $minValue" }
            field = value
            rebuildContentOffsetRange()
            this.value = this.value
        }

    @IntRange(from = 0, to = MAX_VALUE.toLong())
    var minValue: Int = 0
        set(value) {
            require(value <= maxValue) { "最小值: $value 不能大于最大值: $maxValue" }
            field = value
            rebuildContentOffsetRange()
            this.value = this.value
        }

    var value: Int
        set(value) {
            _value = MathUtils.clamp(value, minValue, maxValue)
            contentOffset = getContentOffsetForValue(_value)
            resetStateAndAbortScroll()
            invalidate()
            notifyValueChanged()
        }
        @IntRange(from = 0, to = MAX_VALUE.toLong())
        get() = _value
    private var _value: Int = 0

    var textSize: Float = 0f
        set(value) {
            field = value
            labelPaint.textSize = value
            fontMetrics = labelPaint.fontMetrics
            requestLayout()
            invalidate()
        }

    var textColor: ColorStateList? = null
        set(value) {
            field = value
            invalidate()
        }

    var onRulerValueChangeListener: OnRulerValueChangeListener? = null

    private val scroller by lazy {
        OverScroller(context).apply {
            setFriction(0.005f)
        }
    }
    private val touchSlop by lazy { ViewConfiguration.get(context).scaledTouchSlop }
    private val flingVelocity by lazy { ViewConfiguration.get(context).scaledMinimumFlingVelocity to ViewConfiguration.get(context).scaledMaximumFlingVelocity }

    private var contentOffset = 0
    private var contentOffsetRange: kotlin.ranges.IntRange = 0..0
    private var lastX = 0f
    private var downX = 0f
    private var downDistance = 0f
    private var lastDistance = 0f
    private lateinit var fontMetrics: Paint.FontMetrics
    private val velocityTracker = VelocityTracker.obtain()
    private var state = STATE_IDLE
    private val markers = mutableListOf<Marker>()

    private val tempRect = Rect()
    private val tempRectF = RectF()
    private var markerHeight = 0

    private val rulerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    init {
        setWillNotDraw(false)
        with(context.obtainStyledAttributes(attrs, R.styleable.RulerView, defStyleAttr, R.style.Widget_RulerView)) {
            stepWidth = getDimensionPixelOffset(R.styleable.RulerView_stepWidth, 10.toDip())
            getString(R.styleable.RulerView_rulerValueFormatter).takeUnless { it.isNullOrBlank() }?.also {
                kotlin.runCatching {
                    Class.forName(it).takeIf { RulerValueFormatter::class.java.isAssignableFrom(it) }?.also {
                        rulerValueFormatter = it.getConstructor().newInstance() as? RulerValueFormatter
                    }
                }.onFailure {
                    if (it is NoSuchMethodException) {
                        throw IllegalArgumentException("${it}类必须包含默认构造函数")
                    }
                }
            }

            setScaleColor(getColor(R.styleable.RulerView_scaleColor, Color.BLACK))
            setRulerColor(getColor(R.styleable.RulerView_rulerColor, Color.BLACK))
            sectionScaleCount = getInt(R.styleable.RulerView_sectionScaleCount, 10)
            indicator = getDrawable(R.styleable.RulerView_indicator)
            scaleMinHeight = getDimensionPixelSize(R.styleable.RulerView_scaleMinHeight, 10.toDip())
            scaleMaxHeight = getDimensionPixelSize(R.styleable.RulerView_scaleMaxHeight, 20.toDip())
            rulerSize = getDimensionPixelSize(R.styleable.RulerView_rulerSize, 2.toDip())
            scaleSize = getDimensionPixelSize(R.styleable.RulerView_scaleSize, 2.toDip())
            maxValue = getInt(R.styleable.RulerView_maxValue, MAX_VALUE)
            minValue = getInt(R.styleable.RulerView_minValue, 0)
            require(maxValue > minValue) { "最大值不能小于最小值" }
            value = getInt(R.styleable.RulerView_value, minValue)
            require(value in minValue..maxValue) { "值需要介于最小值(${minValue})和最大值(${maxValue})之间" }
            textSize = getDimension(R.styleable.RulerView_android_textSize, 10.toSp())
            textColor = getColorStateList(R.styleable.RulerView_android_textColor)
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var targetHeight = rulerSize //横线高度
        targetHeight += max(scaleMaxHeight, scaleMinHeight) //刻度高度
        targetHeight += (fontMetrics.bottom - fontMetrics.top).toInt() //刻度文本高度
        targetHeight = max(indicator?.intrinsicHeight ?: 0, targetHeight) //选中指示器高度
        targetHeight += paddingTop + paddingBottom //上下padding

        markerHeight = markers.maxOfOrNull { tempRect.apply { it.getBounds(this) }.height() }
            ?: 0 //Marker最大高度
        targetHeight += markerHeight
        setMeasuredDimension(resolveSize(suggestedMinimumWidth + paddingLeft + paddingRight, widthMeasureSpec), resolveSize(targetHeight, heightMeasureSpec))
    }

    fun setScaleColor(@ColorInt color: Int) {
        scaleColor = ColorStateList.valueOf(color)
    }

    fun setRulerColor(@ColorInt color: Int) {
        rulerColor = ColorStateList.valueOf(color)
    }

    fun setIndicator(@DrawableRes indicatorRes: Int) {
        indicator = resources.getDrawable(indicatorRes)
    }

    fun setTextColor(@ColorInt color: Int) {
        textColor = ColorStateList.valueOf(color)
    }

    private fun getContentOffsetForValue(relativeValue: Int) = try {
        val result = relativeValue.toLong() * stepWidth.toLong()
        require(result.compareTo(result.toInt()) == 0) { "integer overflow" }
        result.toInt()
    } catch (e: Exception) {
        Int.MAX_VALUE
    }

    private fun resetStateAndAbortScroll() {
        state = STATE_IDLE
        scroller.abortAnimation()
    }

    private fun notifyValueChanged() {
        onRulerValueChangeListener?.onRulerValueChanged(value, rulerValueFormatter?.formatValue(value)
                ?: value.toString())
    }

    private fun rebuildContentOffsetRange() {
        contentOffsetRange = getContentOffsetForValue(minValue)..getContentOffsetForValue(maxValue)
    }

    override fun onDraw(canvas: Canvas) {
        val height = height
        val width = width
        val contentOffset = contentOffset
        val paddingTop = paddingTop
        val paddingLeft = paddingLeft
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom
        val rulerBottom = height - markerHeight - paddingBottom
        val rulerTop = rulerBottom - rulerSize
        val rulerSize = rulerSize
        val drawableState = drawableState
        val maxScaleCount = maxValue
        val minScaleCount = minValue
        val insetWidth = width - paddingLeft - paddingRight
        val halfInsetWidth = insetWidth / 2
        val stepWidth = stepWidth
        val valueRange = minScaleCount..maxScaleCount
        val scaleSize = scaleSize.toFloat()

        rulerPaint.color = scaleColor?.getColorForState(drawableState, Color.BLACK) ?: Color.BLACK
        canvas.save()
        canvas.clipRect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)

        labelPaint.color = textColor?.getColorForState(drawableState, Color.BLACK) ?: Color.BLACK
        val fontY = rulerBottom - rulerSize - scaleMaxHeight - fontMetrics.bottom
        val count = contentOffset / stepWidth
        for (index in min(count, maxScaleCount) downTo minScaleCount) {
            val scalePosition: Int = index * stepWidth
            val centerX: Float = paddingLeft + halfInsetWidth + scalePosition - contentOffset.toFloat()
            val left = centerX - scaleSize / 2
            val right = centerX + scaleSize / 2
            val label = rulerValueFormatter?.formatValue(index) ?: index.toString()
            val labelRight = centerX + labelPaint.measureText(label) / 2
            if (labelRight > 0) {
                if (0 == index % sectionScaleCount || index == maxScaleCount || index == minScaleCount) {
                    canvas.drawRect(left, rulerTop - scaleMaxHeight.toFloat(), right, rulerTop.toFloat(), rulerPaint)
                    canvas.drawText(label, centerX, fontY, labelPaint)
                } else {
                    canvas.drawRect(left, rulerTop - scaleMinHeight.toFloat(), right, rulerTop.toFloat(), rulerPaint)
                }
            } else {
                break
            }
        }

        for (index in max(minScaleCount, count)..maxScaleCount) {
            val scalePosition: Int = index * stepWidth
            val centerX = paddingLeft + halfInsetWidth + scalePosition - contentOffset.toFloat()
            val left = centerX - scaleSize / 2
            val right = centerX + scaleSize / 2
            val label = rulerValueFormatter?.formatValue(index) ?: index.toString()
            val labelLeft = centerX - labelPaint.measureText(label) / 2
            if (labelLeft < width) {
                if (0 == index % sectionScaleCount || index == maxScaleCount || index == minScaleCount) {
                    canvas.drawRect(left, rulerBottom - rulerSize - scaleMaxHeight.toFloat(), right, rulerTop.toFloat(), rulerPaint)
                    canvas.drawText(label, centerX, fontY, labelPaint)
                } else {
                    canvas.drawRect(left, rulerBottom - rulerSize - scaleMinHeight.toFloat(), right, rulerTop.toFloat(), rulerPaint)
                }
            } else {
                break
            }
        }


        //绘制底部横线
        rulerPaint.color = rulerColor?.getColorForState(drawableState, Color.BLACK) ?: Color.BLACK
        canvas.drawRect(max(0, paddingLeft + halfInsetWidth - contentOffset + contentOffsetRange.first).toFloat(), rulerTop.toFloat(), min(width, width - paddingRight - halfInsetWidth + contentOffsetRange.last - contentOffset).toFloat(), rulerBottom.toFloat(), rulerPaint)

        //绘制Marker
        markers.forEach {
            val value = it.value()
            val scalePosition = value * stepWidth
            it.getBounds(tempRect)
            val centerX = paddingLeft + halfInsetWidth + scalePosition - contentOffset
            val left = centerX - tempRect.width().toFloat() / 2
            val right = centerX + tempRect.width().toFloat() / 2
            val x = left
            val y = (height - paddingBottom - markerHeight).toFloat()
            it.x = x
            it.y = y
            if ((right > 0 || left < width) && value in valueRange) {
                canvas.save()
                canvas.translate(x, y)
                it.onDraw(canvas)
                canvas.restore()
            }
        }

        //绘制指示器
        indicator?.apply {
            state = drawableState
            setBounds(paddingLeft + halfInsetWidth - intrinsicWidth / 2, paddingTop, paddingLeft + halfInsetWidth + intrinsicWidth / 2, rulerBottom)
            draw(canvas)
        }

        canvas.restore()
    }

    private fun getMaxDistanceOfPointers(event: MotionEvent): Float {
        var minX = 0f
        var maxX = 0f
        for (index in 0 until event.pointerCount) {
            event.getX(index).let {
                minX = min(minX, it)
                maxX = max(maxX, it)
            }
        }
        return abs(maxX - minX)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        event.pointerCount
        val x = event.x
        val y = event.y
        super.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                resetStateAndAbortScroll()
                downX = x
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount > 1) {
                    downDistance = getMaxDistanceOfPointers(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount > 1) {
                    val currentDistance = getMaxDistanceOfPointers(event)
                    if (STATE_PINCH != state && STATE_SCROLL != state) {
                        if (abs(currentDistance - downDistance) > touchSlop) {
                            state = STATE_PINCH
                        }
                    }

                    if (STATE_PINCH == state) {
                        val dDistance = currentDistance - lastDistance
                        stepWidth = max(1, (stepWidth + dDistance / 2).toInt())
                        invalidate()
                    }
                    lastDistance = currentDistance
                } else {
                    if (STATE_SCROLL != state && STATE_PINCH != state) {
                        if (abs(x - downX) > touchSlop) {
                            parent?.requestDisallowInterceptTouchEvent(true)
                            state = STATE_SCROLL
                        }
                    }

                    if (STATE_SCROLL == state) {
                        val dx = x - lastX
                        contentOffset -= (contentOffset - dx).let {
                            if (it.toInt() in contentOffsetRange) dx else dx / 2
                        }.toInt()
                        _value = getValueForContentOffset(contentOffset)
                        notifyValueChanged()
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                when (state) {
                    STATE_IDLE -> {
                        markers.forEach {
                            it.getBounds(tempRect)
                            tempRectF.set(tempRect)
                            tempRectF.offset(it.x, it.y)
                            if (tempRectF.contains(x, y)) {
                                it.performClick()
                            }
                        }
                        state = STATE_RESET
                        scrollToRoundedValue()
                    }
                    STATE_PINCH -> {
                        state = STATE_RESET
                        scrollToRoundedValue()
                    }
                    STATE_SCROLL -> {
                        velocityTracker.computeCurrentVelocity(1000, flingVelocity.second.toFloat())
                        val velocityX = velocityTracker.xVelocity
                        when {
                            contentOffset !in contentOffsetRange -> {
                                state = STATE_RESET
                                scroller.springBack(contentOffset, 0, contentOffsetRange.first, contentOffsetRange.last, 0, 0)
                            }
                            abs(velocityX) > flingVelocity.first -> {
                                state = STATE_FLING
                                var resolvedVelocityX = (-velocityX).toInt()

                                //矫正Fling速度，让最后始终停留在具体的刻度上
                                val flingOffset = scroller.getSplineFlingDistance(resolvedVelocityX).toInt()
                                val targetOffset: Int = contentOffset + flingOffset
                                if (targetOffset in contentOffsetRange) {
                                    resolvedVelocityX = scroller.getSplineFlingVelocity(getContentOffsetForValue(getValueForContentOffset(targetOffset)) - contentOffset.toDouble())
                                }
                                scroller.fling(contentOffset, 0, resolvedVelocityX, 0, contentOffsetRange.first, contentOffsetRange.last, 0, 0, (width / 8f).toInt(), 0)
                                invalidate()
                            }
                            else -> {
                                state = STATE_RESET
                                scrollToRoundedValue()
                            }
                        }
                    }
                }
                invalidate()
                velocityTracker.clear()
            }
            MotionEvent.ACTION_CANCEL -> velocityTracker.clear()
        }
        velocityTracker.addMovement(event)
        lastX = x
        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            contentOffset = scroller.currX
            _value = getValueForContentOffset(contentOffset)
            notifyValueChanged()
            invalidate()
        } else {
            if (needScrollToRoundValuePosition()) {
                scrollToRoundedValue()
            } else if (STATE_FLING == state || STATE_RESET == state) {
                state = STATE_IDLE
            }
        }
    }

    private fun needScrollToRoundValuePosition(): Boolean {
        val currentValue = contentOffset * 1.0f / stepWidth
        val roundedValue = round(currentValue)
        return currentValue != roundedValue && (STATE_RESET == state || STATE_FLING == state)
    }

    private fun getValueForContentOffset(contentOffset: Int) = MathUtils.clamp(round(contentOffset * 1.0f / stepWidth).toInt(), minValue, maxValue)

    private fun scrollToRoundedValue() {
        val roundedValue: Int = getRoundedValue(contentOffset)
        scroller.abortAnimation()
        scroller.startScroll(contentOffset, 0, getContentOffsetForValue(roundedValue) - contentOffset, 0, 800)
        invalidate()
    }

    private fun getRoundedValue(offset: Int) = MathUtils.clamp(round(offset * 1.0f / stepWidth).toInt(), minValue, maxValue)

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who == indicator
    }

    fun addMarker(marker: Marker) {
        markers.add(marker)
        Collections.sort(markers, MARKER_COMPARATOR)
        marker.onAttach(this)
        requestLayout()
        invalidate()
    }

    fun removeMarker(marker: Marker) {
        markers.remove(marker)
        requestLayout()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState()).apply {
            stepWidth = this@RulerView.stepWidth
            scaleColor = this@RulerView.scaleColor
            rulerColor = this@RulerView.rulerColor
            sectionScaleCount = this@RulerView.sectionScaleCount
            scaleMinHeight = this@RulerView.scaleMinHeight
            scaleMaxHeight = this@RulerView.scaleMaxHeight
            scaleSize = this@RulerView.scaleSize
            rulerSize = this@RulerView.rulerSize
            maxValue = this@RulerView.maxValue
            minValue = this@RulerView.minValue
            value = this@RulerView.value
            textSize = this@RulerView.textSize
            textColor = this@RulerView.textColor
            state = this@RulerView.state
            contentOffset = this@RulerView.contentOffset
            maxContentOffset = this@RulerView.contentOffsetRange.last
            minContentOffset = this@RulerView.contentOffsetRange.first
            markers.addAll(this@RulerView.markers)
        }
    }

    override fun onRestoreInstanceState(savedState: Parcelable?) {
        (savedState as? SavedState)?.also {
            super.onRestoreInstanceState(it.superState)
            stepWidth = it.stepWidth
            scaleColor = it.scaleColor
            rulerColor = it.rulerColor
            sectionScaleCount = it.sectionScaleCount
            scaleMinHeight = it.scaleMinHeight
            scaleMaxHeight = it.scaleMaxHeight
            scaleSize = it.scaleSize
            rulerSize = it.rulerSize
            maxValue = it.maxValue
            minValue = it.minValue
            value = it.value
            textSize = it.textSize
            textColor = it.textColor
            state = it.state
            contentOffset = it.contentOffset
            contentOffsetRange = it.minContentOffset..it.maxContentOffset
            it.markers.forEach {
                markers.add(it)
                it.onAttach(this)
            }
        }
    }

    private class SavedState : BaseSavedState {
        var stepWidth = 0
        var scaleColor: ColorStateList? = null
        var rulerColor: ColorStateList? = null
        var sectionScaleCount = 0
        var scaleMinHeight = 0
        var scaleMaxHeight = 0
        var scaleSize = 0
        var rulerSize = 0
        var maxValue = 0
        var minValue = 0
        var value = 0
        var textSize = 0f
        var textColor: ColorStateList? = null
        var state = 0
        var contentOffset = 0
        var maxContentOffset = 0
        var minContentOffset = 0
        val markers = ArrayList<Marker>()

        constructor(source: Parcel) : super(source) {
            stepWidth = source.readInt()
            scaleColor = source.readParcelable(ColorStateList::class.java.classLoader)
            rulerColor = source.readParcelable(ColorStateList::class.java.classLoader)
            sectionScaleCount = source.readInt()
            scaleMinHeight = source.readInt()
            scaleMaxHeight = source.readInt()
            scaleSize = source.readInt()
            rulerSize = source.readInt()
            maxValue = source.readInt()
            minValue = source.readInt()
            value = source.readInt()
            textSize = source.readFloat()
            textColor = source.readParcelable(ColorStateList::class.java.classLoader)
            state = source.readInt()
            contentOffset = source.readInt()
            maxContentOffset = source.readInt()
            minContentOffset = source.readInt()
            source.readList(markers, Marker::class.java.classLoader)
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(stepWidth)
            out.writeParcelable(scaleColor, 0)
            out.writeParcelable(rulerColor, 0)
            out.writeInt(sectionScaleCount)
            out.writeInt(scaleMinHeight)
            out.writeInt(scaleMaxHeight)
            out.writeInt(scaleSize)
            out.writeInt(rulerSize)
            out.writeInt(maxValue)
            out.writeInt(minValue)
            out.writeInt(value)
            out.writeFloat(textSize)
            out.writeParcelable(textColor, 0)
            out.writeInt(state)
            out.writeInt(contentOffset)
            out.writeInt(maxContentOffset)
            out.writeInt(minContentOffset)
            out.writeList(markers)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }

    private fun Int.toDip() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), resources.displayMetrics).toInt()
    private fun Int.toSp() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, toFloat(), resources.displayMetrics)

    companion object {
        const val MAX_VALUE: Int = 10000
        const val STATE_IDLE = 0
        const val STATE_PINCH = 1
        const val STATE_SCROLL = 2
        const val STATE_FLING = 3
        const val STATE_RESET = 4
        private val MARKER_COMPARATOR = Comparator<Marker> { o1, o2 -> o1.value().compareTo(o2.value()) }
    }
}
/*
 * Copyright (C) 2018 TangKe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ke.tang.ruler;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RulerView extends View {
    public final static int MAX_VALUE = 10000;
    private final static int STATE_IDLE = 0;
    private final static int STATE_PINCH = 1;
    private final static int STATE_SCROLL = 2;
    private final static int STATE_FLING = 3;
    private final static int STATE_RESET = 4;

    /**
     * 刻度宽度
     */
    private int mStepWidth;

    /**
     * 格式化
     */
    private RulerValueFormatter mRulerValueFormatter;

    /**
     * 刻度颜色
     */
    private ColorStateList mScaleColor;

    /**
     * 尺颜色
     */
    private ColorStateList mRulerColor;

    /**
     * 区间刻度数
     */
    private int mSectionScaleCount;

    /**
     * 指示器
     */
    private Drawable mIndicator;

    /**
     * 刻度最小高度
     */
    private int mScaleMinHeight;

    /**
     * 刻度最大高度
     */
    private int mScaleMaxHeight;

    /**
     * 刻度尺寸
     */
    private int mScaleSize;

    /**
     * 标尺尺寸
     */
    private int mRulerSize;

    /**
     * 最大值
     */
    @IntRange(from = 0, to = MAX_VALUE)
    private int mMaxValue;

    /**
     * 最小值
     */
    @IntRange(from = 0, to = MAX_VALUE)
    private int mMinValue;

    /**
     * 当前值, 需要乘以步长得到最终值
     */
    @IntRange(from = 0, to = MAX_VALUE)
    private int mValue;

    /**
     * 刻度文本大小
     */
    private float mTextSize;

    /**
     * 刻度文本颜色
     */
    private ColorStateList mTextColor;

    private OnRulerValueChangeListener mOnRulerValueChangeListener;

    private OverScroller mScroller;
    private int mContentOffset;
    private int mMaxContentOffset;
    private int mMinContentOffset;

    private Paint mRulerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint mLabelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private float mLastX;
    private float mDownX;
    private float mDownDistance;
    private float mLastDistance;

    private Paint.FontMetrics mFontMetrics;

    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();

    private int mState = STATE_IDLE;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mTouchSlop;
    private List<Marker> mMarkers = new ArrayList<>();
    private Comparator<Marker> mMarkerComparator = new Comparator<Marker>() {
        @Override
        public int compare(Marker o1, Marker o2) {
            int value1 = o1.value();
            int value2 = o2.value();
            if (value1 > value2) {
                return 1;
            } else if (value1 < value2) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    private Rect mTempRect = new Rect();
    private RectF mTempRectF = new RectF();
    private int mMarkerHeight;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.rulerViewStyle);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new OverScroller(context);
        mScroller.setFriction(0.005f);
        setWillNotDraw(false);

        mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        final Resources resources = context.getResources();
        final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.RulerView, defStyleAttr, R.style.Widget_RulerView);
        mStepWidth = a.getDimensionPixelOffset(R.styleable.RulerView_stepWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics));

        String valueFormatterClassName = a.getString(R.styleable.RulerView_rulerValueFormatter);
        if (!TextUtils.isEmpty(valueFormatterClassName)) {
            try {
                Class valueFormatterClass = Class.forName(valueFormatterClassName);
                if (!RulerValueFormatter.class.isAssignableFrom(valueFormatterClass)) {
                    throw new IllegalArgumentException(valueFormatterClassName + "类必须实现RulerValueFormatter");
                }
                Constructor constructor = valueFormatterClass.getConstructor();
                mRulerValueFormatter = (RulerValueFormatter) constructor.newInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(valueFormatterClassName + "类必须包含默认构造函数");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }


        setScaleColor(a.getColor(R.styleable.RulerView_scaleColor, Color.BLACK));
        setRulerColor(a.getColor(R.styleable.RulerView_rulerColor, Color.BLACK));

        mSectionScaleCount = a.getInt(R.styleable.RulerView_sectionScaleCount, 10);
        mIndicator = a.getDrawable(R.styleable.RulerView_indicator);
        mScaleMinHeight = a.getDimensionPixelSize(R.styleable.RulerView_scaleMinHeight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics));
        mScaleMaxHeight = a.getDimensionPixelSize(R.styleable.RulerView_scaleMaxHeight, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, displayMetrics));

        mRulerSize = a.getDimensionPixelSize(R.styleable.RulerView_rulerSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, displayMetrics));
        mScaleSize = a.getDimensionPixelSize(R.styleable.RulerView_scaleSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, displayMetrics));
        mMaxValue = a.getInt(R.styleable.RulerView_maxValue, MAX_VALUE);
        mMinValue = a.getInt(R.styleable.RulerView_minValue, 0);
        if (mMaxValue < mMinValue) {
            throw new IllegalArgumentException("最大值不能小于最小值");
        }
        mValue = a.getInt(R.styleable.RulerView_value, mMinValue);
        if (mValue > mMaxValue || mValue < mMinValue) {
            throw new IllegalArgumentException("值需要介于最小值(" + mMinValue + ")和最大值(" + mMaxValue + ")之间");
        }

        mLabelPaint.setTextAlign(Paint.Align.CENTER);
        setTextSize(a.getDimension(R.styleable.RulerView_android_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics)));
        setTextColor(a.getColorStateList(R.styleable.RulerView_android_textColor));
        setValue(mValue);
        a.recycle();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mContentOffset = mScroller.getCurrX();
            mValue = getValueForContentOffset(mContentOffset);
            notifyValueChanged();
            invalidate();
        } else {
            if (needScrollToRoundValuePosition()) {
                scrollToRoundedValue();
            } else if (STATE_FLING == mState || STATE_RESET == mState) {
                mState = STATE_IDLE;
            }
        }
    }

    private void notifyValueChanged() {
        if (null != mOnRulerValueChangeListener) {
            mOnRulerValueChangeListener.onRulerValueChanged(mValue, null != mRulerValueFormatter ? mRulerValueFormatter.formatValue(mValue) : String.valueOf(mValue));
        }
    }

    private void scrollToRoundedValue() {
        int roundedValue = getRoundedValue(mContentOffset);
        mScroller.abortAnimation();
        mScroller.startScroll(mContentOffset, 0, getContentOffsetForValue(roundedValue) - mContentOffset, 0, 800);
        invalidate();
    }

    private int getRoundedValue(int offset) {
        return Math.max(mMinValue, Math.min(Math.round(offset * 1.0f / mStepWidth), mMaxValue));
    }

    private boolean needScrollToRoundValuePosition() {
        float currentValue = mContentOffset * 1.0f / mStepWidth;
        int roundedValue = Math.round(currentValue);
        return currentValue != roundedValue && (STATE_RESET == mState || STATE_FLING == mState);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int targetHeight = 0;
        targetHeight += mRulerSize; //横线高度
        targetHeight += Math.max(mScaleMaxHeight, mScaleMinHeight);
        targetHeight += mFontMetrics.bottom - mFontMetrics.top;
        if (null != mIndicator) {
            targetHeight = Math.max(mIndicator.getIntrinsicHeight(), targetHeight);
        }
        targetHeight += getPaddingTop() + getPaddingBottom();
        if (!mMarkers.isEmpty()) {
            int maxMarkerHeight = 0;
            for (Marker marker : mMarkers) {
                marker.getBounds(mTempRect);
                maxMarkerHeight = Math.max(maxMarkerHeight, mTempRect.height());
            }
            targetHeight += maxMarkerHeight;
            mMarkerHeight = maxMarkerHeight;
        }

        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight(), widthMeasureSpec), resolveSize(targetHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int contentOffset = mContentOffset;
        final int maxContentOffset = mMaxContentOffset;
        final int minContentOffset = mMinContentOffset;
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        final int width = getWidth();
        final int height = getHeight();
        final int rulerHeight = height - mMarkerHeight;
        final int insetWidth = width - paddingLeft - paddingRight;
        final int halfInsetWidth = insetWidth / 2;
        final float scaleSize = mScaleSize;
        final float rulerSize = mRulerSize;
        final int maxScaleCount = mMaxValue;
        final int minScaleCount = mMinValue;
        final int[] drawableState = getDrawableState();

        mRulerPaint.setColor(mScaleColor.isStateful() ? mScaleColor.getColorForState(drawableState, Color.BLACK) : mScaleColor.getDefaultColor());

        //向前绘制刻度，绘制到左边界停止
        if (null != mTextColor) {
            mLabelPaint.setColor(mTextColor.getColorForState(drawableState, Color.BLACK));
        }
        final float fontY = rulerHeight - getPaddingBottom() - rulerSize - mScaleMaxHeight - mFontMetrics.bottom;
        int count = contentOffset / mStepWidth;
        for (int index = Math.min(count, maxScaleCount); index >= minScaleCount; index--) {
            int scalePosition = index * mStepWidth;
            final float centerX = paddingLeft + halfInsetWidth + scalePosition - contentOffset;
            final float left = centerX - scaleSize / 2;
            final float right = centerX + scaleSize / 2;
            String label = null != mRulerValueFormatter ? mRulerValueFormatter.formatValue(index) : String.valueOf(index);
            final float labelRight = centerX + mLabelPaint.measureText(label) / 2;
            if (labelRight > 0) {
                if (0 == index % mSectionScaleCount || index == maxScaleCount || index == minScaleCount) {
                    canvas.drawRect(left, rulerHeight - rulerSize - paddingBottom - mScaleMaxHeight, right, rulerHeight - rulerSize - paddingBottom, mRulerPaint);
                    canvas.drawText(label, centerX, fontY, mLabelPaint);
                } else {
                    canvas.drawRect(left, rulerHeight - rulerSize - paddingBottom - mScaleMinHeight, right, rulerHeight - rulerSize - paddingBottom, mRulerPaint);
                }
            } else {
                break;
            }
        }

        //向后绘制刻度，绘制到右边界停止
        for (int index = Math.max(minScaleCount, count); index <= maxScaleCount; index++) {
            int scalePosition = index * mStepWidth;
            final float centerX = paddingLeft + halfInsetWidth + scalePosition - contentOffset;
            final float left = centerX - scaleSize / 2;
            final float right = centerX + scaleSize / 2;
            String label = null != mRulerValueFormatter ? mRulerValueFormatter.formatValue(index) : String.valueOf(index);
            final float labelLeft = centerX - mLabelPaint.measureText(label) / 2;
            if (labelLeft < width) {
                if (0 == index % mSectionScaleCount || index == maxScaleCount || index == minScaleCount) {
                    canvas.drawRect(left, rulerHeight - rulerSize - paddingBottom - mScaleMaxHeight, right, rulerHeight - rulerSize - paddingBottom, mRulerPaint);
                    canvas.drawText(label, centerX, fontY, mLabelPaint);
                } else {
                    canvas.drawRect(left, rulerHeight - rulerSize - paddingBottom - mScaleMinHeight, right, rulerHeight - rulerSize - paddingBottom, mRulerPaint);
                }
            } else {
                break;
            }
        }
        //绘制底部横线
        mRulerPaint.setColor(mRulerColor.isStateful() ? mRulerColor.getColorForState(drawableState, Color.BLACK) : mRulerColor.getDefaultColor());
        canvas.drawRect(Math.max(0, paddingLeft + halfInsetWidth - contentOffset + minContentOffset), rulerHeight - paddingBottom - rulerSize, Math.min(width, width - paddingRight - halfInsetWidth + maxContentOffset - contentOffset), rulerHeight - paddingBottom, mRulerPaint);

        //绘制Marker
        if (!mMarkers.isEmpty()) {
            for (Marker marker : mMarkers) {
                int scalePosition = marker.value() * mStepWidth;
                marker.getBounds(mTempRect);
                final float centerX = paddingLeft + halfInsetWidth + scalePosition - contentOffset;
                final float left = centerX - mTempRect.width() / 2;
                final float right = centerX + mTempRect.width() / 2;
                final float x = left, y = height - mMarkerHeight;
                marker.setX(x);
                marker.setY(y);
                if (right > 0 || left < width) {
                    canvas.save();
                    canvas.translate(x, y);
                    marker.onDraw(canvas);
                    canvas.restore();
                }
            }
        }

        //绘制指示器
        if (null != mIndicator) {
            final Drawable indicator = mIndicator;
            if (indicator.isStateful()) {
                indicator.setState(drawableState);
            }
            indicator.setBounds(paddingLeft + halfInsetWidth - indicator.getIntrinsicWidth() / 2, paddingTop, paddingLeft + halfInsetWidth + indicator.getIntrinsicWidth() / 2, rulerHeight - paddingBottom);
            indicator.draw(canvas);
        }
    }

    private int getValueForContentOffset(int contentOffset) {
        return Math.max(Math.min(Math.round(contentOffset * 1.0f / mStepWidth), mMaxValue), mMinValue);
    }

    private int getContentOffsetForValue(int relativeValue) {
        try {
            return MathUtils.multiplyExact(relativeValue, mStepWidth);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        boolean result = super.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        int width = getWidth();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                resetStateAndAbortScroll();
                mDownX = x;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerCount > 1) {
                    mDownDistance = getMaxDistanceOfPointers(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerCount > 1) {
                    float currentDistance = getMaxDistanceOfPointers(event);
                    if (STATE_PINCH != mState && STATE_SCROLL != mState) {
                        if (Math.abs(currentDistance - mDownDistance) > mTouchSlop) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mState = STATE_PINCH;
                        }
                    }
                    if (STATE_PINCH == mState) {
                        float dDistance = currentDistance - mLastDistance;
                        mStepWidth = Math.max(1, (int) (mStepWidth + dDistance / 2));
                        mValue = Math.max(mMinValue, Math.min(mValue, mMaxValue));
                        mContentOffset = getContentOffsetForValue(mValue);
                        mMaxContentOffset = getContentOffsetForValue(mMaxValue);
                        mMinContentOffset = getContentOffsetForValue(mMinValue);
                        invalidate();
                    }
                    mLastDistance = currentDistance;
                } else {
                    float dx = x - mLastX;
                    if (STATE_SCROLL != mState && STATE_PINCH != mState) {
                        if (Math.abs(x - mDownX) > mTouchSlop) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            mState = STATE_SCROLL;
                        }
                    }
                    if (STATE_SCROLL == mState) {
                        if (mContentOffset - dx < mMinContentOffset || mContentOffset - dx > mMaxContentOffset) {
                            dx = dx / 2;
                        }
                        mContentOffset -= dx;
                        mValue = getValueForContentOffset(mContentOffset);
                        notifyValueChanged();
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                switch (mState) {
                    case STATE_IDLE:
                        if (!mMarkers.isEmpty()) {
                            for (Marker marker : mMarkers) {
                                marker.getBounds(mTempRect);
                                mTempRectF.set(mTempRect);
                                mTempRectF.offset(marker.getX(), marker.getY());
                                if (mTempRectF.contains(x, y)) {
                                    marker.performClick();
                                    //只触发一个
                                    break;
                                }
                            }
                        }
                    case STATE_PINCH:
                        mState = STATE_RESET;
                        scrollToRoundedValue();
                        break;
                    default:
                    case STATE_FLING:
                    case STATE_RESET:
                    case STATE_SCROLL:
                        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                        float velocityX = mVelocityTracker.getXVelocity();
                        if (mContentOffset < mMinContentOffset) {
                            mState = STATE_RESET;
                            mScroller.springBack(mContentOffset, 0, mMinContentOffset, mMaxContentOffset, 0, 0);
                        } else if (mContentOffset > mMaxContentOffset) {
                            mState = STATE_RESET;
                            mScroller.springBack(mContentOffset, 0, mMinContentOffset, mMaxContentOffset, 0, 0);
                        } else if (Math.abs(velocityX) > mMinimumVelocity) {
                            mState = STATE_FLING;
                            int resolvedVelocityX = (int) -velocityX;

                            //矫正Fling速度，让最后始终停留在我具体的刻度上
                            int flingOffset = (int) mScroller.getSplineFlingDistance(resolvedVelocityX);
                            int targetOffset = mContentOffset + flingOffset;
                            if (targetOffset >= mMinContentOffset && targetOffset <= mMaxContentOffset) {
                                resolvedVelocityX = mScroller.getSplineFlingVelocity(getContentOffsetForValue(getValueForContentOffset(targetOffset)) - mContentOffset);
                            }
                            mScroller.fling(mContentOffset, 0, resolvedVelocityX, 0, mMinContentOffset, mMaxContentOffset, 0, 0, (int) (width / 8f), 0);
                        } else {
                            mState = STATE_RESET;
                            scrollToRoundedValue();
                        }
                        break;
                }
                invalidate();
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.clear();
                break;
        }
        mVelocityTracker.addMovement(event);
        mLastX = event.getX();
        return true;
    }

    private float getMaxDistanceOfPointers(MotionEvent event) {
        final int pointerCount = event.getPointerCount();
        float maxX = 0, minX = 0;
        for (int index = 0; index < pointerCount; index++) {
            float currentX = event.getX(index);
            maxX = Math.max(currentX, maxX);
            minX = Math.min(currentX, minX);
        }
        return Math.abs(maxX - minX);
    }

    /**
     * 设置文本尺寸
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {
        mTextSize = textSize;
        mLabelPaint.setTextSize(textSize);
        mFontMetrics = mLabelPaint.getFontMetrics();
        requestLayout();
        invalidate();
    }

    /**
     * 设置文本颜色
     *
     * @param color
     */
    public void setTextColor(@ColorInt int color) {
        setTextColor(ColorStateList.valueOf(color));
    }

    /**
     * 设置刻度颜色
     *
     * @param color
     */
    public void setScaleColor(@ColorInt int color) {
        setScaleColor(ColorStateList.valueOf(color));
    }

    /**
     * 设置刻度颜色
     *
     * @param color
     */
    public void setScaleColor(ColorStateList color) {
        mScaleColor = color;
        invalidate();
    }

    /**
     * 设置标尺颜色，底部横线
     *
     * @param color
     */
    public void setRulerColor(@ColorInt int color) {
        setRulerColor(ColorStateList.valueOf(color));
    }

    /**
     * 设置标尺颜色，底部横线
     *
     * @param color
     */
    public void setRulerColor(ColorStateList color) {
        mRulerColor = color;
        invalidate();
    }

    /**
     * 设置用于格式化标尺值
     *
     * @param rulerValueFormatter
     */
    public void setRulerValueFormatter(RulerValueFormatter rulerValueFormatter) {
        mRulerValueFormatter = rulerValueFormatter;
        notifyValueChanged();
        invalidate();
    }

    /**
     * 设置当前值
     * 标尺会立即显示为当前值
     *
     * @param value
     */
    public void setValue(@IntRange(from = 0, to = MAX_VALUE) int value) {
        mValue = Math.max(mMinValue, Math.min(value, mMaxValue));
        mContentOffset = getContentOffsetForValue(mValue);
        mMaxContentOffset = getContentOffsetForValue(mMaxValue);
        mMinContentOffset = getContentOffsetForValue(mMinValue);
        resetStateAndAbortScroll();
        invalidate();
        notifyValueChanged();
    }

    private void resetStateAndAbortScroll() {
        mState = STATE_IDLE;
        mScroller.abortAnimation();
    }

    /**
     * 获取当前值
     *
     * @return
     */
    public int getValue() {
        return mValue;
    }

    /**
     * 获取格式化的值，如果没有设置{@link RulerValueFormatter}，就是默认的值转换成文本类型
     *
     * @return
     */
    public String getFormatValue() {
        return null != mRulerValueFormatter ? mRulerValueFormatter.formatValue(mValue) : String.valueOf(mValue);
    }

    /**
     * 设置标尺值变化回调
     *
     * @param onRulerValueChangeListener
     */
    public void setOnRulerValueChangeListener(OnRulerValueChangeListener onRulerValueChangeListener) {
        mOnRulerValueChangeListener = onRulerValueChangeListener;
    }

    /**
     * 设置标尺文本颜色
     *
     * @param color
     */
    public void setTextColor(ColorStateList color) {
        mTextColor = color;
        invalidate();
    }

    /**
     * 设置标尺文本颜色
     *
     * @param res
     */
    public void setTextColorResource(@ColorRes int res) {
        setTextColor(res > 0 ? getResources().getColorStateList(res) : ColorStateList.valueOf(Color.BLACK));
    }

    /**
     * 设置标尺颜色
     *
     * @param res
     */
    public void setRulerColorResource(@ColorRes int res) {
        setRulerColor(res > 0 ? getResources().getColorStateList(res) : ColorStateList.valueOf(Color.BLACK));
    }

    /**
     * 设置刻度颜色
     *
     * @param res
     */
    public void setScaleColorResource(@ColorRes int res) {
        setScaleColor(res > 0 ? getResources().getColorStateList(res) : ColorStateList.valueOf(Color.BLACK));
    }

    /**
     * 设置指示器，用于显示在标尺中间
     *
     * @param res
     */
    public void setIndicator(@DrawableRes int res) {
        setIndicator(res > 0 ? getResources().getDrawable(res) : null);
    }

    /**
     * 设置指示器，用于显示在标尺中间
     *
     * @param indicator
     */
    public void setIndicator(Drawable indicator) {
        if (null != mIndicator) {
            mIndicator.setCallback(null);
        }
        mIndicator = indicator;
        if (null != indicator) {
            indicator.setCallback(this);
        }
        invalidate();
    }

    /**
     * 设置最大值限制
     * 如果当前值大于设置的最大值，会自动调整当前值为最大值
     *
     * @param maxValue
     */
    public void setMaxValue(@IntRange(from = 0, to = MAX_VALUE) int maxValue) {
        if (maxValue < mMinValue) {
            throw new IllegalArgumentException("最大值: " + maxValue + " 不能小于最小值: " + mMinValue);
        }
        mMaxValue = maxValue;
        setValue(mValue);
    }

    /**
     * 设置最小值限制
     * 如果当前值小于设置的最小值，会自动调整当前值为最小值
     *
     * @param minValue
     */
    public void setMinValue(@IntRange(from = 0, to = MAX_VALUE) int minValue) {
        if (minValue > mMaxValue) {
            throw new IllegalArgumentException("最小值: " + minValue + " 不能大于最大值: " + mMaxValue);
        }
        mMinValue = minValue;
        setValue(mValue);
    }

    /**
     * 设置刻度宽度
     *
     * @param scaleSize
     */
    public void setScaleSize(int scaleSize) {
        mScaleSize = Math.max(0, scaleSize);
        requestLayout();
        invalidate();
    }

    /**
     * 设置标尺高度（底部横线）
     *
     * @param rulerSize
     */
    public void setRulerSize(int rulerSize) {
        mRulerSize = Math.max(0, rulerSize);
        requestLayout();
        invalidate();
    }

    /**
     * 设置刻度与刻度之间的距离，必须大于1
     *
     * @param stepWidth 单位：像素
     */
    public void setStepWidth(int stepWidth) {
        mStepWidth = Math.max(1, stepWidth);
        setValue(mValue);
    }

    /**
     * 设置大刻度和大刻度之间小刻度数量
     *
     * @param sectionScaleCount
     */
    public void setSectionScaleCount(int sectionScaleCount) {
        mSectionScaleCount = Math.max(0, sectionScaleCount);
        invalidate();
    }

    /**
     * 设置小刻度的高度
     *
     * @param scaleMinHeight
     */
    public void setScaleMinHeight(int scaleMinHeight) {
        mScaleMinHeight = scaleMinHeight;
        requestLayout();
        invalidate();
    }

    /**
     * 设置大刻度的高度
     *
     * @param scaleMaxHeight
     */
    public void setScaleMaxHeight(int scaleMaxHeight) {
        mScaleMaxHeight = scaleMaxHeight;
        requestLayout();
        invalidate();
    }

    /**
     * 获取刻度与刻度之间的距离
     *
     * @return 单位：像素
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getStepWidth() {
        return mStepWidth;
    }

    /**
     * 获取刻度颜色
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public ColorStateList getScaleColor() {
        return mScaleColor;
    }

    /**
     * 获取标尺颜色（底部横线）
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public ColorStateList getRulerColor() {
        return mRulerColor;
    }

    /**
     * 获取大刻度和大刻度之间的小刻度数
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getSectionScaleCount() {
        return mSectionScaleCount;
    }

    /**
     * 获取指示器
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public Drawable getIndicator() {
        return mIndicator;
    }

    /**
     * 获取小刻度的高度
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getScaleMinHeight() {
        return mScaleMinHeight;
    }

    /**
     * 获取大刻度的高度
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getScaleMaxHeight() {
        return mScaleMaxHeight;
    }

    /**
     * 获取刻度宽度
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getScaleSize() {
        return mScaleSize;
    }

    /**
     * 获取标尺高度（底部横线）
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getRulerSize() {
        return mRulerSize;
    }

    /**
     * 获取最大值
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getMaxValue() {
        return mMaxValue;
    }

    /**
     * 获取最小值
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public int getMinValue() {
        return mMinValue;
    }

    /**
     * 获取文本尺寸
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * 获取文本颜色
     *
     * @return
     */
    @ViewDebug.ExportedProperty(category = "custom")
    public ColorStateList getTextColor() {
        return mTextColor;
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == mIndicator;
    }

    public void addMarker(Marker marker) {
        mMarkers.add(marker);
        Collections.sort(mMarkers, mMarkerComparator);
        requestLayout();
        invalidate();
    }

    public void removeMarker(Marker marker) {
        mMarkers.remove(marker);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mStepWidth = savedState.mStepWidth;
        mScaleColor = savedState.mScaleColor;
        mRulerColor = savedState.mRulerColor;
        mSectionScaleCount = savedState.mSectionScaleCount;
        mScaleMinHeight = savedState.mScaleMinHeight;
        mScaleMaxHeight = savedState.mScaleMaxHeight;
        mScaleSize = savedState.mScaleSize;
        mRulerSize = savedState.mRulerSize;
        mMaxValue = savedState.mMaxValue;
        mMinValue = savedState.mMinValue;
        mValue = savedState.mValue;
        setTextSize(savedState.mTextSize);
        mTextColor = savedState.mTextColor;
        mState = savedState.mState;
        mContentOffset = savedState.mContentOffset;
        mMaxContentOffset = savedState.mMaxContentOffset;
        mMinContentOffset = savedState.mMinContentOffset;
        mMarkers = savedState.mMarkers;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.mStepWidth = mStepWidth;
        state.mScaleColor = mScaleColor;
        state.mRulerColor = mRulerColor;
        state.mSectionScaleCount = mSectionScaleCount;
        state.mScaleMinHeight = mScaleMinHeight;
        state.mScaleMaxHeight = mScaleMaxHeight;
        state.mScaleSize = mScaleSize;
        state.mRulerSize = mRulerSize;
        state.mMaxValue = mMaxValue;
        state.mMinValue = mMinValue;
        state.mValue = mValue;
        state.mTextSize = mTextSize;
        state.mTextColor = mTextColor;
        state.mState = mState;
        state.mContentOffset = mContentOffset;
        state.mMaxContentOffset = mMaxContentOffset;
        state.mMinContentOffset = mMinContentOffset;
        state.mMarkers = mMarkers;
        return state;
    }

    private static class SavedState extends BaseSavedState {
        private int mStepWidth;
        private ColorStateList mScaleColor;
        private ColorStateList mRulerColor;
        private int mSectionScaleCount;
        private int mScaleMinHeight;
        private int mScaleMaxHeight;
        private int mScaleSize;
        private int mRulerSize;
        private int mMaxValue;
        private int mMinValue;
        private int mValue;
        private float mTextSize;
        private ColorStateList mTextColor;
        private int mState;
        private int mContentOffset;
        private int mMaxContentOffset;
        private int mMinContentOffset;
        private List<Marker> mMarkers;

        public SavedState(Parcel source) {
            super(source);
            mStepWidth = source.readInt();
            mScaleColor = source.readParcelable(ColorStateList.class.getClassLoader());
            mRulerColor = source.readParcelable(ColorStateList.class.getClassLoader());
            mSectionScaleCount = source.readInt();
            mScaleMinHeight = source.readInt();
            mScaleMaxHeight = source.readInt();
            mScaleSize = source.readInt();
            mRulerSize = source.readInt();
            mMaxValue = source.readInt();
            mMinValue = source.readInt();
            mValue = source.readInt();
            mTextSize = source.readFloat();
            mTextColor = source.readParcelable(ColorStateList.class.getClassLoader());
            mState = source.readInt();
            mContentOffset = source.readInt();
            mMaxContentOffset = source.readInt();
            mMinContentOffset = source.readInt();
            mMarkers = source.readArrayList(Marker.class.getClassLoader());
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mStepWidth);
            out.writeParcelable(mScaleColor, 0);
            out.writeParcelable(mRulerColor, 0);
            out.writeInt(mSectionScaleCount);
            out.writeInt(mScaleMinHeight);
            out.writeInt(mScaleMaxHeight);
            out.writeInt(mScaleSize);
            out.writeInt(mRulerSize);
            out.writeInt(mMaxValue);
            out.writeInt(mMinValue);
            out.writeInt(mValue);
            out.writeFloat(mTextSize);
            out.writeParcelable(mTextColor, 0);
            out.writeInt(mState);
            out.writeInt(mContentOffset);
            out.writeInt(mMaxContentOffset);
            out.writeInt(mMinContentOffset);
            out.writeList(mMarkers);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}

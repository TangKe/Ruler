package ke.tang.ruler

import androidx.annotation.IntRange

interface RulerValueFormatter {
    /**
     * 当标尺需要显示一个值的时候调用该方法获取格式化后的值
     * [value] 当前的值，取值范围从0到[RulerView.MAX_VALUE]
     * 返回当前值对应的文本
     */
    fun formatValue(@IntRange(from = 0, to = RulerView.MAX_VALUE.toLong()) value: Int): String
}
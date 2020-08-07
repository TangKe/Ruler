package ke.tang.ruler

/**
 * 当标尺的值变化时的回调接口
 */
interface OnRulerValueChangeListener {
    /**
     * 标尺值变化回调
     * [value] 当前标尺的值
     * [displayValue] 当前标尺显示的值（格式化后）
     *
     */
    fun onRulerValueChanged(value: Int, displayValue: String)

}
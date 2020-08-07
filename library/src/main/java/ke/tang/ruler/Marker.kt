package ke.tang.ruler

import android.graphics.Canvas
import android.graphics.Rect
import android.os.Parcelable
import android.view.View

interface Marker : Parcelable {
    var x: Float
    var y: Float

    fun onDraw(canvas: Canvas)

    fun getBounds(rect: Rect)

    fun performClick()

    fun value(): Int

    fun onAttach(v: View)
}
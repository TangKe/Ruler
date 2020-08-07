package ke.tang.ruler

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.DrawableRes
import java.util.*

class DrawableMarker : ClickableMarker {
    @Transient
    private var drawable: Drawable? = null

    @DrawableRes
    private val drawableRes: Int
    private val value: Int

    constructor(@DrawableRes drawableRes: Int, value: Int, onMarkerClickListener: OnMarkerClickListener? = null) : super(onMarkerClickListener) {
        this.drawableRes = drawableRes
        this.value = value
    }


    constructor(parcel: Parcel) : super(parcel) {
        drawableRes = parcel.readInt()
        value = parcel.readInt()
    }

    override fun onDraw(canvas: Canvas) {
        drawable?.also {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            it.draw(canvas)
        }
    }

    override fun getBounds(rect: Rect) {
        rect.set(0, 0, drawable?.intrinsicWidth ?: 0, drawable?.intrinsicHeight ?: 0)
    }

    override fun value() = value

    override fun onAttach(v: View) {
        if (drawableRes != View.NO_ID) {
            drawable = v.resources.getDrawable(drawableRes)
            drawableCaches.entries.removeAll { it.value == cacheKey }
            drawableCaches[drawable] = cacheKey
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(drawableRes)
        dest.writeInt(value)
    }

    override fun describeContents(): Int = 0

    companion object {
        val drawableCaches = WeakHashMap<Drawable?, String?>()

        @JvmStatic
        val CREATOR = object : Parcelable.Creator<DrawableMarker> {
            override fun createFromParcel(parcel: Parcel): DrawableMarker {
                return DrawableMarker(parcel)
            }

            override fun newArray(size: Int): Array<DrawableMarker?> {
                return arrayOfNulls(size)
            }
        }

    }
}
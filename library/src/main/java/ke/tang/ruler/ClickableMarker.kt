package ke.tang.ruler

import android.os.Parcel
import java.util.*

abstract class ClickableMarker : Marker {
    override var x: Float = 0f
    override var y: Float = 0f
    internal val cacheKey: String
    var onMarkerClickListener: OnMarkerClickListener? = null
        set(value) {
            forEach {
                if (it.value == cacheKey) {
                    remove(it.key)
                }
            }
            field = value
            if(null != value){
                put(value, cacheKey)
            }
        }

    constructor(onMarkerClickListener: OnMarkerClickListener?) {
        this.onMarkerClickListener = onMarkerClickListener
        cacheKey = UUID.randomUUID().toString()
    }

    constructor(parcel: Parcel) {
        cacheKey = parcel.readString()
        forEach {
            if (it.value == cacheKey) {
                onMarkerClickListener = it.key
            }
        }
    }

    override fun performClick() {
        onMarkerClickListener?.onMarkerClick(this)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(cacheKey)
    }

    companion object onMarkerClickListenerCaches : WeakHashMap<OnMarkerClickListener, String>()
}
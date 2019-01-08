package ke.tang.ruler;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.support.annotation.DrawableRes;
import android.view.View;

import java.util.UUID;
import java.util.WeakHashMap;

public class DrawableMarker extends ClickableMarker {
    private static WeakHashMap<String, Drawable> sDrawableCaches = new WeakHashMap<>();
    private transient Drawable mDrawable;
    @DrawableRes
    private int mDrawableRes;
    private int mValue;
    private String mDrawableCacheKey = UUID.randomUUID().toString();

    public DrawableMarker(@DrawableRes int res, int value) {
        mDrawableRes = res;
        mValue = value;
    }

    public DrawableMarker(Drawable drawable, int value) {
        mDrawable = drawable;
        mValue = value;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (null != mDrawable) {
            mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void onAttach(View v) {
        super.onAttach(v);
        if (0 != mDrawableRes) {
            mDrawable = v.getContext().getResources().getDrawable(mDrawableRes);
        }
    }

    @Override
    public void getBounds(Rect rect) {
        if (null != mDrawable) {
            rect.set(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }
    }

    @Override
    public int value() {
        return mValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDrawableRes);
        dest.writeInt(this.mValue);
        dest.writeString(this.mDrawableCacheKey);
        if (0 == mDrawableRes && null != mDrawable) {
            sDrawableCaches.put(mDrawableCacheKey, mDrawable);
        }
    }

    protected DrawableMarker(Parcel in) {
        this.mDrawableRes = in.readInt();
        this.mValue = in.readInt();
        this.mDrawableCacheKey = in.readString();
        if (0 == mDrawableRes) {
            mDrawable = sDrawableCaches.get(mDrawableCacheKey);
            sDrawableCaches.remove(mDrawableCacheKey);
        }
    }

    public static final Creator<DrawableMarker> CREATOR = new Creator<DrawableMarker>() {
        @Override
        public DrawableMarker createFromParcel(Parcel source) {
            return new DrawableMarker(source);
        }

        @Override
        public DrawableMarker[] newArray(int size) {
            return new DrawableMarker[size];
        }
    };
}

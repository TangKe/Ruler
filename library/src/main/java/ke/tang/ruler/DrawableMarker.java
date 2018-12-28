package ke.tang.ruler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

public class DrawableMarker extends ClickableMarker {
    private Drawable mDrawable;
    private int mValue;

    public DrawableMarker(Context context, @DrawableRes int res, int value) {
        this(context.getResources().getDrawable(res), value);
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
    public void getBounds(Rect rect) {
        if (null != mDrawable) {
            rect.set(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }
    }

    @Override
    public int value() {
        return mValue;
    }
}

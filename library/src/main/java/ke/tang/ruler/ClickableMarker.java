package ke.tang.ruler;

import android.view.View;

public abstract class ClickableMarker implements Marker {
    private OnMarkerClickListener mOnMarkerClickListener;

    private float mX;
    private float mY;

    public ClickableMarker(OnMarkerClickListener onMarkerClickListener) {
        mOnMarkerClickListener = onMarkerClickListener;
    }

    public ClickableMarker() {
    }

    public OnMarkerClickListener getOnMarkerClickListener() {
        return mOnMarkerClickListener;
    }

    public void setOnMarkerClickListener(OnMarkerClickListener onMarkerClickListener) {
        mOnMarkerClickListener = onMarkerClickListener;
    }

    @Override
    public void performClick() {
        if (null != mOnMarkerClickListener) {
            mOnMarkerClickListener.onMarkerClick(this);
        }
    }

    @Override
    public void onAttach(View v) {
        
    }

    public interface OnMarkerClickListener {
        void onMarkerClick(Marker marker);
    }

    @Override
    public float getX() {
        return mX;
    }

    @Override
    public float getY() {
        return mY;
    }

    @Override
    public void setX(float x) {
        mX = x;
    }

    @Override
    public void setY(float y) {
        mY = y;
    }
}

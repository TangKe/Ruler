package ke.tang.ruler;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcelable;
import android.view.View;

public interface Marker extends Parcelable {
    void onDraw(Canvas canvas);

    void getBounds(Rect rect);

    void performClick();

    int value();

    void setX(float x);

    void setY(float y);

    float getX();

    float getY();

    void onAttach(View v);
}

package ke.tang.ruler;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface Marker {
    void onDraw(Canvas canvas);

    void getBounds(Rect rect);

    void performClick();

    int value();

    void setX(float x);

    void setY(float y);

    float getX();

    float getY();
}

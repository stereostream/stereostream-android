package io.complicated.stereostream.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

public class VideoViewCustom extends VideoView {
    public VideoViewCustom(Context context) {
        super(context, null);
    }

    public VideoViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //some code....
                break;
            case MotionEvent.AXIS_LTRIGGER:
            case MotionEvent.ACTION_BUTTON_PRESS:
            case MotionEvent.ACTION_UP:
                v.performClick();
                break;
            default:
                break;
        }
        return true;
    }
}

package com.example.videomagnification.utils.seekbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatSeekBar;

@SuppressLint("ClickableViewAccessibility")
public class AccurateSeekBar extends AppCompatSeekBar {

    private final float THRESHOLD_MULTIPLIER = 2;
    private float lastXPosition;

    public AccurateSeekBar(Context context) {
        super(context);
    }

    public AccurateSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccurateSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean shouldTrackNormal = Math.abs(event.getY()) < ((float)this.getHeight() * THRESHOLD_MULTIPLIER);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!shouldTrackNormal) {
                    float trackingHorizontalDistance = event.getX() - this.lastXPosition;
                    float valuePerPixel = (float)this.getMax() / (float)this.getWidth();
                    float valueDivisor = Math.abs(event.getY()) / (float)this.getHeight();
                    float offset = (trackingHorizontalDistance * valuePerPixel) / valueDivisor;
                    this.setProgress(this.getProgress() + (int)offset);
                }
                break;
        }
        this.lastXPosition = event.getX();
        return shouldTrackNormal ? super.onTouchEvent(event) : true;
    }

}
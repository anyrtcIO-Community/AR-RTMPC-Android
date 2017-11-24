package org.anyrtc.widgets;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;


public class MultiCircleDrawable extends Drawable implements Animatable, Drawable.Callback {

    // 每个Drawable动画启动的间隔
    private static final int EACH_CIRCLE_SPACE = 200;
    // CircleDrawable数组
    private CircleDrawable[] mCircleDrawables;


    public MultiCircleDrawable() {
        mCircleDrawables = new CircleDrawable[] {
                new CircleDrawable(),
                new CircleDrawable(),
                new CircleDrawable()
        };
        for (int i = 0; i < mCircleDrawables.length; i++) {
            // 设置动画启动延迟
            mCircleDrawables[i].setAnimatorDelay(EACH_CIRCLE_SPACE * i);
            // 设置回调监听，当CircleDrawable发生重绘时就会调用 invalidateDrawable(Drawable who) 方法
            mCircleDrawables[i].setCallback(this);
        }
    }


    @Override
    public void draw(Canvas canvas) {
        for (CircleDrawable drawable : mCircleDrawables) {
            // 分层绘制每个CircleDrawable
            int count = canvas.save();
            drawable.draw(canvas);
            canvas.restoreToCount(count);
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.RGBA_8888;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        for (CircleDrawable drawable : mCircleDrawables) {
            drawable.onBoundsChange(bounds);
        }
    }
    /************************************************************/

    @Override
    public void start() {
        for (CircleDrawable drawable : mCircleDrawables) {
            drawable.start();
        }
    }

    @Override
    public void stop() {
        for (CircleDrawable drawable : mCircleDrawables) {
            drawable.stop();
        }
    }

    @Override
    public boolean isRunning() {
        for (CircleDrawable drawable : mCircleDrawables) {
            if (drawable.isRunning()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        // 需要重绘，子Drawable发生重绘会调用这个方法通知父Drawable，如果有设置Callback回调监听的话
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
    }
}

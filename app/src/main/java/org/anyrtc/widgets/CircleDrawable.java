package org.anyrtc.widgets;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Property;

public class CircleDrawable extends Drawable implements Animatable {

    private Paint mPaint;
    // 动画控制
    private ValueAnimator mValueAnimator;
    // 扩散半径
    private int mRadius;
    // 绘制的矩形框
    private RectF mRect = new RectF();
    // 动画启动延迟时间
    private int mStartDelay;

    // 自定义一个扩散半径属性
    Property<CircleDrawable, Integer> mRadiusProperty = new Property<CircleDrawable, Integer>(Integer.class, "radius") {
        @Override
        public void set(CircleDrawable object, Integer value) {
            object.setRadius(value);
        }

        @Override
        public Integer get(CircleDrawable object) {
            return object.getRadius();
        }
    };
    public int getRadius() {
        return mRadius;
    }
    public void setRadius(int radius) {
        mRadius = radius;
    }


    public CircleDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
    }


    @Override
    public void draw(Canvas canvas) {
        // 绘制圆圈
        canvas.drawCircle(mRect.centerX(), mRect.centerY(), mRadius, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.RGBA_8888;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRect.set(_clipSquare(bounds));
        if (isRunning()) {
            stop();
        }
        // 计算最大半径
        int maxRadius = (int) ((mRect.right - mRect.left) / 2);
        // 控制扩散半径的属性变化
        PropertyValuesHolder radiusHolder = PropertyValuesHolder.ofInt(mRadiusProperty, 0, maxRadius);
        // 控制透明度的属性变化
        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", 255, 100);
        mValueAnimator = ObjectAnimator.ofPropertyValuesHolder(this, radiusHolder, alphaHolder);
        mValueAnimator.setStartDelay(mStartDelay);
        mValueAnimator.setDuration(1000);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 监听属性动画并进行重绘
                invalidateSelf();
            }

        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setAlpha(0);
                invalidateSelf();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        // 设置动画无限循环
//        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
//        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        start();
    }

    /**
     * 裁剪Rect为正方形
     * @param rect
     * @return
     */
    private Rect _clipSquare(Rect rect) {
        int w = rect.width();
        int h = rect.height();
        int min = Math.min(w, h);
        int cx = rect.centerX();
        int cy = rect.centerY();
        int r = min / 2;
        return new Rect(
                cx - r,
                cy - r,
                cx + r,
                cy + r
        );
    }

    /************************************************************/

    @Override
    public void start() {
        mValueAnimator.start();
    }

    @Override
    public void stop() {
        mValueAnimator.end();
    }

    @Override
    public boolean isRunning() {
        return mValueAnimator != null && mValueAnimator.isRunning();
    }

    public void setAnimatorDelay(int startDelay) {
        mStartDelay = startDelay;
    }
}

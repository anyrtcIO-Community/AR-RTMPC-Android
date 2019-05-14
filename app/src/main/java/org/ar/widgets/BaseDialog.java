package org.ar.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by KathLine on 2016/11/8.
 */

public abstract class BaseDialog extends Dialog {

    public BaseDialog(Context context) {
        super(context);
    }

    protected abstract void onTouchOutside();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /* 触摸外部弹窗 */
        if (isOutOfBounds(getContext(), event)) {
            onTouchOutside();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 判断当前用户触摸是否超出了Dialog的显示区域
     *
     * @param context
     * @param event
     * @return
     */
    private boolean isOutOfBounds(Context context, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        final View decorView = getWindow().getDecorView();
        return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop))
                || (y > (decorView.getHeight() + slop));
    }
}

package org.anyrtc.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import org.anyrtc.live_line.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Skyline on 2016/12/20.
 */
public class DiffuseView extends View {

    /** 扩散圆圈颜色 */
    private int mColor = getResources().getColor(R.color.colorAccent);
    /** 圆圈中心颜色 */
    private int mCoreColor = getResources().getColor(R.color.colorPrimary);
    /** 圆圈中心图片 */
    private Bitmap mBitmap, mCenterBitmap;
    /** 中心圆半径 */
    private float mCoreRadius = 50;
    /** 扩散圆宽度 */
    private int mDiffuseWidth = 6;
    /** 最大宽度 */
    private Integer mMaxWidth = 255;
    /** 是否正在扩散中 */
    private boolean mIsDiffuse = false;
    // 透明度集合
    private List<Integer> mAlphas = new ArrayList<>();
    // 扩散圆半径集合
    private List<Integer> mWidths = new ArrayList<>();
    private Paint mPaint;

    public DiffuseView(Context context) {
        this(context, null);
    }

    public DiffuseView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DiffuseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiffuseView, defStyleAttr, 0);
        mColor = a.getColor(R.styleable.DiffuseView_diffuse_color, mColor);
        mCoreColor = a.getColor(R.styleable.DiffuseView_diffuse_coreColor, mCoreColor);
        mCoreRadius = a.getFloat(R.styleable.DiffuseView_diffuse_coreRadius, mCoreRadius);
        mDiffuseWidth = a.getInt(R.styleable.DiffuseView_diffuse_width, mDiffuseWidth);
        mMaxWidth = a.getInt(R.styleable.DiffuseView_diffuse_maxWidth, mMaxWidth);
        int imageId = a.getResourceId(R.styleable.DiffuseView_diffuse_coreImage, -1);
        if(imageId != -1) {
            mBitmap = BitmapFactory.decodeResource(getResources(), imageId);
            mCenterBitmap = createCircleImage(mBitmap, mBitmap.getWidth());
//            mMaxWidth = mCenterBitmap.getWidth() * 2 / 5;
        }
        a.recycle();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mAlphas.add(120);
        mWidths.add(0);
    }

    @Override
    public void invalidate() {
        if(hasWindowFocus()){
            super.invalidate();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if(hasWindowFocus){
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        // 绘制扩散圆
        mPaint.setColor(mColor);
        if(mIsDiffuse) {
            for (int i = 0; i < mAlphas.size(); i++) {
                // 设置透明度
                Integer alpha = mAlphas.get(i);
                mPaint.setAlpha(alpha);
                // 绘制扩散圆
                Integer width = mWidths.get(i);
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, mCoreRadius + width, mPaint);

                if(alpha > 0 && width < mMaxWidth){
                    mAlphas.set(i, alpha - 1);
                    mWidths.set(i, width + 1);
                }
            }
            // 判断当扩散圆扩散到指定宽度时添加新扩散圆
            if (mWidths.get(mWidths.size() - 1) == mMaxWidth / mDiffuseWidth) {
                mAlphas.add(255);
                mWidths.add(0);
            }
            // 超过10个扩散圆，删除最外层
            if(mWidths.size() >= 10){
                mWidths.remove(0);
                mAlphas.remove(0);
            }

            // 绘制中心圆及图片
            mPaint.setAlpha(255);
            mPaint.setColor(mCoreColor);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, mCoreRadius, mPaint);
        }

        if(mBitmap != null){
            canvas.drawBitmap(mCenterBitmap, getWidth() / 2 - mCenterBitmap.getWidth() / 2
                    , getHeight() / 2 - mCenterBitmap.getHeight() / 2, mPaint);
        }

        if(mIsDiffuse){
            invalidate();
        }
    }

    /**
     * 开始扩散
     */
    public void start() {
        mIsDiffuse = true;
        invalidate();
    }

    /**
     * 停止扩散
     */
    public void stop() {
        mIsDiffuse = false;
        invalidate();
    }

    /**
     * 是否扩散中
     */
    public boolean isDiffuse(){
        return mIsDiffuse;
    }

    /**
     * 设置扩散圆颜色
     */
    public void setColor(int colorId){
        mColor = colorId;
    }

    /**
     * 设置中心圆颜色
     */
    public void setCoreColor(int colorId){
        mCoreColor = colorId;
    }

    /**
     * 设置中心圆图片
     */
    public void setCoreImage(int imageId){
        mBitmap = BitmapFactory.decodeResource(getResources(), imageId);
        mCenterBitmap = createCircleImage(mBitmap, mBitmap.getWidth());
//        mMaxWidth = mCenterBitmap.getWidth() * 2 / 5;
    }

    /**
     * 设置中心圆半径
     */
    public void setCoreRadius(int radius){
        mCoreRadius = radius;
    }

    /**
     * 设置扩散圆宽度(值越小宽度越大)
     */
    public void setDiffuseWidth(int width){
        mDiffuseWidth = width;
    }

    /**
     * 设置最大宽度
     */
    public void setMaxWidth(int maxWidth){
        mMaxWidth = maxWidth;
    }

    //根据原图和边长绘制圆形图片
    private Bitmap createCircleImage(Bitmap source, int min)
    {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        //产生一个同样大小的画布
        Canvas canvas = new Canvas(target);
        //首先绘制圆形
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        //使用SRC_IN
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //绘制图片
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }
}

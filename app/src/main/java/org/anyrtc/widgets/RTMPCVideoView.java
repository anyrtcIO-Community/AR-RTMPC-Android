package org.anyrtc.widgets;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.anyrtc.utils.ScreenUtils;
import org.webrtc.EglBase;
import org.webrtc.PercentFrameLayout;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Eric on 2016/7/26.
 */
public class RTMPCVideoView implements RTMPCViewHelper {
    private static Context mContext;
    private static int SUB_X = 72;
    private static int SUB_Y = 10;
    private static int SUB_WIDTH = 24;
    private static int SUB_HEIGHT = 20;

    private static int mScreenWidth;
    private static int mScreenHeight;

    private BtnVideoCloseEvent mVideoCloseEvent;
    private boolean isHost;

    public interface BtnVideoCloseEvent {
        void CloseVideoRender(View view, String strPeerId);

        void OnSwitchCamera(View view);
    }

    public enum RTMPCVideoLayout {
        RTMPC_V_1X3(0),       // Default - One big screen and 3 subscreens
        RTMPC_V_2X2(1);   // All screens as same size & auto layout

        public final int level;

        RTMPCVideoLayout(int level) {
            this.level = level;
        }
    }

    /**
     * 设置连线关闭按钮事件
     *
     * @param btnVideoCloseEvent
     */
    public void setBtnCloseEvent(BtnVideoCloseEvent btnVideoCloseEvent) {
        this.mVideoCloseEvent = btnVideoCloseEvent;
    }

    protected static class VideoView {
        public String strPeerId;
        public int index;
        public int x;
        public int y;
        public int w;
        public int h;
        public PercentFrameLayout mLayout = null;
        public SurfaceViewRenderer mView = null;
        public VideoRenderer mRenderer = null;
        public ImageView btnClose = null;
        private RelativeLayout layoutCamera = null;

        public VideoView(String strPeerId, Context ctx, EglBase eglBase, int index, int x, int y, int w, int h, RendererCommon.ScalingType scalingType) {
            this.strPeerId = strPeerId;
            this.index = index;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            mLayout = new PercentFrameLayout(ctx);
            mLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
            View view = View.inflate(ctx, org.anyrtc.rtmpc_hybrid.R.layout.layout_top_right, null);
            mView = (SurfaceViewRenderer) view.findViewById(org.anyrtc.rtmpc_hybrid.R.id.suface_view);
            btnClose = (ImageView) view.findViewById(org.anyrtc.rtmpc_hybrid.R.id.img_close_render);
            layoutCamera = (RelativeLayout) view.findViewById(org.anyrtc.rtmpc_hybrid.R.id.layout_camera);
            mView.init(eglBase.getEglBaseContext(), null);
            mView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
            mView.setScalingType(scalingType);
            mLayout.addView(view);
        }

        public Boolean Fullscreen() {
            return w == 100 || h == 100;
        }

        public void close() {
            mLayout.removeView(mView);
            mView.release();
            mView = null;
            mRenderer = null;
        }
    }

    private boolean mAutoLayout;
    private EglBase mRootEglBase;
    private RelativeLayout mVideoView;
    private VideoView mLocalRender;
    private HashMap<String, VideoView> mRemoteRenders;
    private RTMPCVideoLayout mRTMPCVideoLayout;

    public RTMPCVideoView(Context ctx, RelativeLayout videoView, EglBase eglBase, boolean isHost, RTMPCVideoLayout rtmpcVideoLayout) {
        mContext = ctx;
        mAutoLayout = false;
        mVideoView = videoView;
        mRootEglBase = eglBase;
        mLocalRender = null;
        mRemoteRenders = new HashMap<>();
        this.isHost = isHost;
        mRTMPCVideoLayout = rtmpcVideoLayout;
        mScreenWidth = ScreenUtils.getScreenWidth(mContext);
        mScreenHeight = ScreenUtils.getScreenHeight(mContext) - ScreenUtils.getStatusHeight(mContext);
    }

    private int GetVideoRenderSize() {
        int size = mRemoteRenders.size();
        if (mLocalRender != null) {
            size += 1;
        }
        return size;
    }

    /**
     * 切换本地图像和远程图像
     *
     * @param peerid 远程图像的peerid
     */
    public void SwitchLocalViewToOtherView(String peerid) {
        VideoView fullscrnView = mLocalRender;
        VideoView view1 = mRemoteRenders.get(peerid);
        int index, x, y, w, h;

        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;

        view1.index = fullscrnView.index;
        view1.x = fullscrnView.x;
        view1.y = fullscrnView.y;
        view1.w = fullscrnView.w;
        view1.h = fullscrnView.h;

        fullscrnView.index = index;
        fullscrnView.x = x;
        fullscrnView.y = y;
        fullscrnView.w = w;
        fullscrnView.h = h;

        fullscrnView.mLayout.setPosition(fullscrnView.x, fullscrnView.y, fullscrnView.w, fullscrnView.h);
        view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);
        updateVideoLayout(view1, fullscrnView);
    }

    /**
     * 交换两个图像的位置
     *
     * @param peerid1 图像1的peerid
     * @param peerid2 图像2的peerid
     */
    public void SwitchViewByPeerId(String peerid1, String peerid2) {
        VideoView view1 = mRemoteRenders.get(peerid1);
        VideoView view2 = mRemoteRenders.get(peerid2);
        int index, x, y, w, h;
        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;

        view1.index = view2.index;
        view1.x = view2.x;
        view1.y = view2.y;
        view1.w = view2.w;
        view1.h = view2.h;

        view2.index = index;
        view2.x = x;
        view2.y = y;
        view2.w = w;
        view2.h = h;

        view2.mLayout.setPosition(view2.x, view2.y, view2.w, view2.h);
        view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);
        updateVideoLayout(view1, view2);
    }

    private void SwitchViewToFullscreen(VideoView view1, VideoView fullscrnView) {
        int index, x, y, w, h;

        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;

        view1.index = fullscrnView.index;
        view1.x = fullscrnView.x;
        view1.y = fullscrnView.y;
        view1.w = fullscrnView.w;
        view1.h = fullscrnView.h;

        fullscrnView.index = index;
        fullscrnView.x = x;
        fullscrnView.y = y;
        fullscrnView.w = w;
        fullscrnView.h = h;

        fullscrnView.mLayout.setPosition(fullscrnView.x, fullscrnView.y, fullscrnView.w, fullscrnView.h);
        view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);

        updateVideoLayout(view1, fullscrnView);
    }

    private void SwitchViewPosition(VideoView view1, VideoView view2) {
        int index, x, y, w, h;
        index = view1.index;
        x = view1.x;
        y = view1.y;
        w = view1.w;
        h = view1.h;

        view1.index = view2.index;
        view1.x = view2.x;
        view1.y = view2.y;
        view1.w = view2.w;
        view1.h = view2.h;

        view2.index = index;
        view2.x = x;
        view2.y = y;
        view2.w = w;
        view2.h = h;

        view1.mLayout.setPosition(view1.x, view1.y, view1.w, view1.h);
        view2.mLayout.setPosition(view2.x, view2.y, view2.w, view2.h);
        updateVideoLayout(view1, view2);
    }

    /**
     * 视频切换后更新视频的布局
     *
     * @param view1
     * @param view2
     */
    private void updateVideoLayout(VideoView view1, VideoView view2) {
        if (view1.Fullscreen()) {
            view1.mView.setZOrderMediaOverlay(false);
            view2.mView.setZOrderMediaOverlay(true);
            view1.mLayout.requestLayout();
            view2.mLayout.requestLayout();
            mVideoView.removeView(view1.mLayout);
            mVideoView.removeView(view2.mLayout);
            mVideoView.addView(view1.mLayout, -1);
            mVideoView.addView(view2.mLayout, 0);
        } else if (view2.Fullscreen()) {
            view1.mView.setZOrderMediaOverlay(true);
            view2.mView.setZOrderMediaOverlay(false);
            view2.mLayout.requestLayout();
            view1.mLayout.requestLayout();
            mVideoView.removeView(view1.mLayout);
            mVideoView.removeView(view2.mLayout);
            mVideoView.addView(view1.mLayout, 0);
            mVideoView.addView(view2.mLayout, -1);
        } else {
            view1.mLayout.requestLayout();
            view2.mLayout.requestLayout();
            mVideoView.removeView(view1.mLayout);
            mVideoView.removeView(view2.mLayout);
            mVideoView.addView(view1.mLayout, 0);
            mVideoView.addView(view2.mLayout, 0);
        }
    }

    /**
     * 切换第一个视频为全屏
     *
     * @param fullscrnView
     */
    private void SwitchIndex1ToFullscreen(VideoView fullscrnView) {
        VideoView view1 = null;
        if (mLocalRender != null && mLocalRender.index == 1) {
            view1 = mLocalRender;
        } else {
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                if (render.index == 1) {
                    view1 = render;
                    break;
                }
            }
        }
        if (view1 != null) {
            SwitchViewPosition(view1, fullscrnView);
        }
    }

    public void BubbleSortSubView(VideoView view) {
        if (mLocalRender != null && view.index + 1 == mLocalRender.index) {
            SwitchViewPosition(mLocalRender, view);
        } else {
            Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, VideoView> entry = iter.next();
                VideoView render = entry.getValue();
                if (view.index + 1 == render.index) {
                    SwitchViewPosition(render, view);
                    break;
                }
            }
        }
        if (view.index < mRemoteRenders.size()) {
            BubbleSortSubView(view);
        }
    }

    /**
     * 屏幕发生变化时变换图像的大小
     */
    private Boolean isLandscape() {
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    // Log.d("RTMPCview","下标1的View:"+"x="+SUB_X+"y="+(100 - (SUB_HEIGHT + SUB_Y))+"width="+SUB_WIDTH+"height="+SUB_HEIGHT);

    /**
     * 根据模板更新视频界面的布局
     */
    private void updateVideoView() {
        screenChange();
        // 1x3 模式
        if (mRTMPCVideoLayout == RTMPCVideoLayout.RTMPC_V_1X3) {
            mLocalRender.mLayout.setPosition(0, 0, 100, 100);
            mLocalRender.mView.requestLayout();
            int size = mRemoteRenders.size();
            if (size == 1) {
                Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, VideoView> entry = iter.next();
                    VideoView render = entry.getValue();
                    render.mLayout.setPosition(SUB_X, (100 -  (SUB_HEIGHT + SUB_Y)), SUB_WIDTH, SUB_HEIGHT);
                    render.mView.requestLayout();
                      }
                } else if (size == 2) {
                    Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, VideoView> entry = iter.next();
                        VideoView render = entry.getValue();
                        if (render.index == 1) {
                            render.mLayout.setPosition(SUB_X, (100 -  (SUB_HEIGHT + SUB_Y)), SUB_WIDTH, SUB_HEIGHT);
                            render.mView.requestLayout();
                        } else if (render.index==2){
                            render.mLayout.setPosition(SUB_X, (100 - 2 * (SUB_HEIGHT + SUB_Y * 2 / 3)), SUB_WIDTH, SUB_HEIGHT);
                            render.mView.requestLayout();
                        }
                    }

                } else if (size == 3) {
                    Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, VideoView> entry = iter.next();
                        VideoView render = entry.getValue();
                        if (render.index == 1) {
                            render.mLayout.setPosition(SUB_X, (100 -  (SUB_HEIGHT + SUB_Y)), SUB_WIDTH, SUB_HEIGHT);
                            render.mView.requestLayout();
                        } else if (render.index==2){
                            render.mLayout.setPosition(SUB_X, (100 - 2 * (SUB_HEIGHT + SUB_Y * 2 / 3)), SUB_WIDTH, SUB_HEIGHT);
                            render.mView.requestLayout();
                        }else if (render.index==3){
                            render.mLayout.setPosition(SUB_X,   (100 - 3 * (SUB_HEIGHT + SUB_Y / 2)), SUB_WIDTH, SUB_HEIGHT);
                            render.mView.requestLayout();
                        }
                    }

                }

            } else if (mRTMPCVideoLayout == RTMPCVideoLayout.RTMPC_V_2X2) {
                //平均大小模式
                int size = mRemoteRenders.size();
                if (size == 0) {
                    mLocalRender.mLayout.setPosition(0, 0, 100, 100);
                    mLocalRender.mView.requestLayout();
                } else if (size == 1) {
                    int X = 50;
                    int Y = 30;
                    int WIDTH = 50;
                    int HEIGHT = isLandscape() ? 50 : 30;
                    Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, VideoView> entry = iter.next();

                        VideoView render = entry.getValue();
                        mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                        mLocalRender.mView.requestLayout();
                        if (render.index == 1) {
                            render.mLayout.setPosition(X, Y, WIDTH, HEIGHT);
                            render.mView.requestLayout();
                        }
                    }
                } else if (size == 2) {
                    int X = 50;
                    int Y = 0;
                    int WIDTH = 50;
                    int HEIGHT = isLandscape() ? 50 : 30;
                    ;
                    Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, VideoView> entry = iter.next();

                        VideoView render = entry.getValue();
                        mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                        mLocalRender.mView.requestLayout();
                        if (render.index == 1) {
                            render.mLayout.setPosition(X, Y, WIDTH, HEIGHT);
                            render.mView.requestLayout();
                        } else if (render.index == 2) {
                            render.mLayout.setPosition(X / 2, Y + HEIGHT, WIDTH, HEIGHT);
                            render.mView.requestLayout();
                        }
                    }
                } else if (size == 3) {
                    int X = 50;
                    int Y = 0;
                    int WIDTH = 50;
                    int HEIGHT = isLandscape() ? 50 : 30;
                    ;
                    Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, VideoView> entry = iter.next();

                        VideoView render = entry.getValue();
                        mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                        mLocalRender.mView.requestLayout();
                        if (render.index == 1) {
                            render.mLayout.setPosition(X, Y, WIDTH, HEIGHT);
                            render.mView.requestLayout();
                        } else if (render.index == 2) {
                            render.mLayout.setPosition(0, Y + HEIGHT, WIDTH, HEIGHT);
                            render.mView.requestLayout();
                        } else if (render.index == 3) {
                            render.mLayout.setPosition(X, Y + HEIGHT, WIDTH, HEIGHT);
                            render.mView.requestLayout();
                        }
                    }
                } else if (size >= 4) {
                    int X = 100 / 3;
                    int Y = 0;
                    int WIDTH = 100 / 3;
                    int HEIGHT = isLandscape() ? 30 : 20;
                    Iterator<Map.Entry<String, VideoView>> iter = mRemoteRenders.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, VideoView> entry = iter.next();

                        VideoView render = entry.getValue();
                        mLocalRender.mLayout.setPosition(0, Y, WIDTH, HEIGHT);
                        mLocalRender.mView.requestLayout();
                        if (render.index % 3 == 2) {
                            render.mLayout.setPosition(X * (render.index % 3), Y + (HEIGHT * (render.index / 3)), WIDTH + 1, HEIGHT);
                            render.mView.requestLayout();
                        } else {
                            render.mLayout.setPosition(X * (render.index % 3), Y + (HEIGHT * (render.index / 3)), WIDTH, HEIGHT);
                            render.mView.requestLayout();
                        }
                    }
                }
            }
        }

        /**
         * 横竖屏切换
         */

    public void onScreenChanged() {
        mScreenWidth = ScreenUtils.getScreenWidth(mContext);
        mScreenHeight = ScreenUtils.getScreenHeight(mContext) - ScreenUtils.getStatusHeight(mContext);

        if (mScreenHeight > mScreenWidth) {
            SUB_Y = 10;
            SUB_WIDTH = 24;
            SUB_HEIGHT = 20;
        } else {
            SUB_Y = 10;
            SUB_WIDTH = 24;
            SUB_HEIGHT = 24;
        }
        updateVideoView();
    }

    /**
     * 屏幕发生变化时变换图像的大小
     */
    private void screenChange() {
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SUB_Y = 15;
            SUB_WIDTH = 24;
            SUB_HEIGHT = 24;
        } else if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            SUB_Y = 10;
            SUB_WIDTH = 24;
            SUB_HEIGHT = 20;
        }
    }

    /**
     * Implements for AnyRTCViewEvents.
     */
    @Override
    public VideoRenderer OnRtcOpenLocalRender(RendererCommon.ScalingType scalingType) {
        int size = GetVideoRenderSize();
        if (size == 0) {
            mLocalRender = new VideoView("localRender", mVideoView.getContext(), mRootEglBase, 0, 0, 0, 100, 100, scalingType);
        } else {
            mLocalRender = new VideoView("localRender", mVideoView.getContext(), mRootEglBase, size, SUB_X,
                    (100 - size * (SUB_HEIGHT + SUB_Y)), SUB_WIDTH, SUB_HEIGHT, scalingType);
            mLocalRender.mView.setZOrderMediaOverlay(true);
        }
//        mVideoView.addView(mLocalRender.mLayout);
        if (mRTMPCVideoLayout == RTMPCVideoLayout.RTMPC_V_1X3) {
            mVideoView.addView(mLocalRender.mLayout, -1);
        } else {
            mVideoView.addView(mLocalRender.mLayout, 0);
        }

        mLocalRender.mLayout.setPosition(
                mLocalRender.x, mLocalRender.y, mLocalRender.w, mLocalRender.h);
        //mLocalRender.mView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
//        mLocalRender.mView.setScalingType(scalingType);
        mLocalRender.mRenderer = new VideoRenderer(mLocalRender.mView);
        return mLocalRender.mRenderer;
    }

    @Override
    public void OnRtcRemoveLocalRender() {
        if (mLocalRender != null) {
            mLocalRender.close();
            mLocalRender.mRenderer = null;

            mVideoView.removeView(mLocalRender.mLayout);
            mLocalRender = null;
        }
    }

    @Override
    public VideoRenderer OnRtcOpenRemoteRender(final String strRtcPeerId, RendererCommon.ScalingType scalingType) {
        VideoView remoteRender = mRemoteRenders.get(strRtcPeerId);
        if (remoteRender == null) {
            int size = GetVideoRenderSize();
            if (size == 0) {
                remoteRender = new VideoView(strRtcPeerId, mVideoView.getContext(), mRootEglBase, 0, 0, 0, 100, 100, scalingType);
            } else {
                if (size == 1) {
                    remoteRender = new VideoView(strRtcPeerId, mVideoView.getContext(), mRootEglBase, size, SUB_X,
                            (100 - size * (SUB_HEIGHT + SUB_Y)), SUB_WIDTH, SUB_HEIGHT, scalingType);
                } else if (size == 2) {
                    remoteRender = new VideoView(strRtcPeerId, mVideoView.getContext(), mRootEglBase, size, SUB_X,
                            (100 - size * (SUB_HEIGHT + SUB_Y * 2 / 3)), SUB_WIDTH, SUB_HEIGHT, scalingType);
                } else if (size == 3) {
                    remoteRender = new VideoView(strRtcPeerId, mVideoView.getContext(), mRootEglBase, size, SUB_X,
                            (100 - size * (SUB_HEIGHT + SUB_Y / 2)), SUB_WIDTH, SUB_HEIGHT, scalingType);
                }
                remoteRender.mView.setZOrderMediaOverlay(true);
            }

            mVideoView.addView(remoteRender.mLayout);

            remoteRender.mLayout.setPosition(
                    remoteRender.x, remoteRender.y, remoteRender.w, remoteRender.h);
//            remoteRender.mView.setScalingType(scalingType);
            remoteRender.mRenderer = new VideoRenderer(remoteRender.mView);

            mRemoteRenders.put(strRtcPeerId, remoteRender);
            updateVideoView();
            if (isHost || (!isHost && strRtcPeerId.equals("LocalCameraRender"))) {
                remoteRender.btnClose.setVisibility(View.VISIBLE);
                if (!isHost) {
                    remoteRender.layoutCamera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mVideoCloseEvent) {
                                mVideoCloseEvent.OnSwitchCamera(v);
                            }
                        }
                    });
                }
                remoteRender.btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mVideoCloseEvent) {
                            mVideoCloseEvent.CloseVideoRender(v, strRtcPeerId);
                        }
                    }
                });
            }
        }
        return remoteRender.mRenderer;
    }

    @Override
    public void OnRtcRemoveRemoteRender(String peerId) {
        VideoView remoteRender = mRemoteRenders.get(peerId);
        if (remoteRender != null) {
            if (mRemoteRenders.size() > 1 && remoteRender.index <= mRemoteRenders.size()) {
                BubbleSortSubView(remoteRender);
            }
            remoteRender.close();
            mVideoView.removeView(remoteRender.mLayout);
            mRemoteRenders.remove(peerId);
            updateVideoView();
        }
    }

    public void changeTem(RTMPCVideoLayout rtmpcVideoLayout) {
        mRTMPCVideoLayout = rtmpcVideoLayout;
        updateVideoView();

    }
}

package org.anyrtc.rtmpc_hybrid;

import org.anyrtc.common.enums.AnyRTCRTMPCLineVideoLayout;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;

/**
 * Created by Skyline on 2017/11/21.
 */
@Deprecated
public class RTMPCHosterVideoOption {
    /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    private boolean mBFront = true;
    /**
     * anyRTC屏幕方向；默认：竖屏
     */
    private AnyRTCScreenOrientation mScreenOriention = AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait;
    /**
     * anyRTC视频清晰标准；默认：标清（RTMPC_Video_SD）
     */
    private AnyRTCVideoQualityMode mVideoMode = AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium1;
    /**
     * anyRTC视频通讯模板：默认：1x3模板
     */
    private AnyRTCRTMPCLineVideoLayout mLineVideoLayout = AnyRTCRTMPCLineVideoLayout.RTMPC_LINE_V_1big_3small;

    public RTMPCHosterVideoOption() {
    }

    /**
     * @param mBFront          true: 前置摄像头；false：后置摄像头；
     * @param mScreenOriention AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait：竖屏模式； AnyRTCScreenOrientation.AnyRTC_SCRN_Landscape：横屏模式；
     * @param mVideoMode       AnyRTCRTMPCVideoMode中  RTMPC_Video_Low（640x480/384）,
     *                         RTMPC_Video_SD（640x480/512）,
     *                         RTMPC_Video_QHD（640x480/768）,
     *                         RTMPC_Video_HD（960x540/1024）,
     *                         RTMPC_Video_720P（1280x720/1280）,
     *                         RTMPC_Video_1080P（1920x1280/2048）
     * @param mVideoLayout     AnyRTCRTMPCLineVideoLayout中
     *                         RTMPC_LINE_V_Fullscrn(0), 主全屏,三小副
     *                         RTMPC_LINE_V_1_equal_others(1), 主和副大小相同
     *                         RTMPC_LINE_V_1big_3small(2); 主大(不是全屏),三小副
     */


    public RTMPCHosterVideoOption(boolean mBFront, AnyRTCScreenOrientation mScreenOriention, AnyRTCVideoQualityMode mVideoMode, AnyRTCRTMPCLineVideoLayout mVideoLayout) {
        this.mBFront = mBFront;
        this.mScreenOriention = mScreenOriention;
        this.mVideoMode = mVideoMode;
        this.mLineVideoLayout = mVideoLayout;
    }

    public boolean ismBFront() {
        return mBFront;
    }

    public void setmBFront(boolean mBFront) {
        this.mBFront = mBFront;
    }

    public AnyRTCScreenOrientation getmScreenOriention() {
        return mScreenOriention;
    }

    public void setmScreenOriention(AnyRTCScreenOrientation mScreenOriention) {
        this.mScreenOriention = mScreenOriention;
    }

    public AnyRTCVideoQualityMode getmVideoMode() {
        return mVideoMode;
    }

    public void setmVideoMode(AnyRTCVideoQualityMode mVideoMode) {
        this.mVideoMode = mVideoMode;
    }

    public AnyRTCRTMPCLineVideoLayout getmLineVideoLayout() {
        return mLineVideoLayout;
    }

    public void setmVideoLayout(AnyRTCRTMPCLineVideoLayout mVideoLayout) {
        this.mLineVideoLayout = mVideoLayout;
    }
}

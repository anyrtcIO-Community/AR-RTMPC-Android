package org.anyrtc.rtmpc_hybrid;

import org.anyrtc.common.enums.AnyRTCScreenOrientation;

/**
 * Created by Skyline on 2017/11/21.
 */
@Deprecated
public class RTMPCGuestVideoOption {
    /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    private boolean mBFront = true;

    private boolean mBAudio = false;

    /**
     * anyRTC屏幕方向；默认：竖屏
     */
    private AnyRTCScreenOrientation mScreenOriention = AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait;

    /**
     * RTMPCGuestVideoOption 配置类
     *
     * @param mBFront          true: 前置摄像头；false：后置摄像头；
     * @param mScreenOriention AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait：竖屏模式； AnyRTCScreenOrientation.AnyRTC_SCRN_Landscape：横屏模式；
     * @param mBAudio          true: 音频连麦；false：视频连麦；
     */
    public RTMPCGuestVideoOption(boolean mBFront, AnyRTCScreenOrientation mScreenOriention, boolean mBAudio) {
        this.mBFront = mBFront;
        this.mScreenOriention = mScreenOriention;
        this.mBAudio = mBAudio;
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

    public boolean ismBAudio() {
        return mBAudio;
    }

    public void setmBAudio(boolean mBAudio) {
        this.mBAudio = mBAudio;
    }

    public RTMPCGuestVideoOption() {
    }
}

package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/10.
 */
@Deprecated
public enum AnyRTCVideoMode {
    /**
     * 1920*1080 - 2048kbps
     */
    AnyRTC_Video_HHD(0),
    /**
     * 1280*720 - 1024kbps
     */
    AnyRTC_Video_HD(1),
    /**
     * 960*540 - 768kbps
     */
    AnyRTC_Video_QHD(2),
    /**
     * 640*480 - 512kbps
     */
    AnyRTC_Video_SD(3),
    /**
     * 352*288 - 256kbps
     */
    AnyRTC_Video_Low(4),
    /**
     * 320*240 - 128kbps
     */
    AnyRTC_Video_FLow(5);

    public final int level;

    private AnyRTCVideoMode(int level) {
        this.level = level;
    }
}

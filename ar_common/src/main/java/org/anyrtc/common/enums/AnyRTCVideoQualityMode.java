package org.anyrtc.common.enums;

/**
 * @author Skyline
 */
@Deprecated
public enum AnyRTCVideoQualityMode {
    /**
     * 320*240 - 128kbps
     */
    AnyRTCVideoQuality_Low1(0),
    /**
     * 352*288 - 256kbps
     */
    AnyRTCVideoQuality_Low2(1),
    /**
     * 352*288 - 384kbps
     */
    AnyRTCVideoQuality_Low3(2),
    /**
     * 640*480 - 384kbps
     */
    AnyRTCVideoQuality_Medium1(3),
    /**
     * 640*480 - 512kbps
     */
    AnyRTCVideoQuality_Medium2(4),
    /**
     * 640*480 - 768kbps
     */
    AnyRTCVideoQuality_Medium3(5),
    /**
     * 960*540 - 1024kbps
     */
    AnyRTCVideoQuality_Height1(6),
    /**
     * 1280*720 - 1280kbps
     */
    AnyRTCVideoQuality_Height2(7),
    /**
     * 1920*1080 - 2048kbps
     */
    AnyRTCVideoQuality_Height3(8);

    public final int level;

    private AnyRTCVideoQualityMode(int level) {
        this.level = level;
    }
}

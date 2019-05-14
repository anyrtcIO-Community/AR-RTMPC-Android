package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public enum AnyRTCRTMPNetAdjustMode {
    /**
     * Normal
     */
    RTMP_NA_Nor(0),
    /**
     * When network is bad, we will drop some video frame.
     */
    RTMP_NA_Fast(1),
    /**
     * When network is bad, we will adjust video bitrate to match.
     */
    RTMP_NA_AutoBitrate(2);

    public final int level;

    AnyRTCRTMPNetAdjustMode(int level) {
        this.level = level;
    }
}

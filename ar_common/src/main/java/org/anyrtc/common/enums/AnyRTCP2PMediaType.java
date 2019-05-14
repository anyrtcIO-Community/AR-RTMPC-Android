package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/12.
 */
@Deprecated
public enum AnyRTCP2PMediaType {
    /**
     * Default - VIDEO
     */
    RT_P2P_CALL_VIDEO (0),
    /**
     *  VIDEO PRO
     */
    RT_P2P_CALL_VIDEO_PRO(1),
    /**
     * AUDIO
     */
    RT_P2P_CALL_AUDIO(2),
    /**
     * VIDEO MONITOR 视频监看模式，主叫可查阅被看的图像
     */
    RT_P2P_CALL_MONITOR(3);

    public final int level;
    AnyRTCP2PMediaType(int level) {
        this.level = level;
    }
}

package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public enum AnyRTCRTMPCVideoMode {

    RTMPC_Video_HH(0),
    RTMPC_Video_Low(1),
    RTMPC_Video_SD(2),
    RTMPC_Video_QHD(3),
    RTMPC_Video_HD(4),
    RTMPC_Video_720P(5),
    RTMPC_Video_1080P(6),
    RTMPC_Video_2K(7),
    RTMPC_Video_4K(8);

    public final int level;

    AnyRTCRTMPCVideoMode(int level) {
        this.level = level;
    }
}

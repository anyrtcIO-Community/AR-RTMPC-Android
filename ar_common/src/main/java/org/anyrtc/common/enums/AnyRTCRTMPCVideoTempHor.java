package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public enum AnyRTCRTMPCVideoTempHor {
    RTMPC_V_T_HOR_LEFT(0),
    RTMPC_V_T_HOR_CENTER(1),
    RTMPC_V_T_HOR_RIGHT(2);

    public final int level;

    AnyRTCRTMPCVideoTempHor(int level) {
        this.level = level;
    }
}

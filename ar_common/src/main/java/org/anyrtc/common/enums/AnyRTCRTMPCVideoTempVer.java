package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public enum AnyRTCRTMPCVideoTempVer {
    RTMPC_V_T_VER_TOP(0),
    RTMPC_V_T_VER_CENTER(1),
    RTMPC_V_T_VER_BOTTOM(2);

    public final int level;

    AnyRTCRTMPCVideoTempVer(int level) {
        this.level = level;
    }
}

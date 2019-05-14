package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public enum AnyRTCRTMPCVideoTempDir {
    RTMPC_V_T_DIR_HOR(0),
    RTMPC_V_T_DIR_VER(1);

    public final int level;

    AnyRTCRTMPCVideoTempDir(int level) {
        this.level = level;
    }
}

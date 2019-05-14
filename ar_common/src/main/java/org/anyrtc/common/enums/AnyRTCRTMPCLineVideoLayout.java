package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public enum AnyRTCRTMPCLineVideoLayout {
    /**
     * 主全屏,三小副
     */
    RTMPC_LINE_V_Fullscrn(0),
    /**
     * 主和副大小相同
     */
    RTMPC_LINE_V_1_equal_others(1),
    /**
     * 主大(不是全屏),三小副
     */
    RTMPC_LINE_V_1big_3small(2);

    public final int level;

    AnyRTCRTMPCLineVideoLayout(int level) {
        this.level = level;
    }
}

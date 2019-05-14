package org.ar.rtmpc_hybrid;

/**
 * Created by liuxiaozhong on 2019/1/17.
 */
public enum  ARRtmpcLineLayoutTemplate {
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

    public final int type;

    ARRtmpcLineLayoutTemplate(int type) {
        this.type = type;
    }
}

package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/12/20.
 */
@Deprecated
public enum AnyRTCRTMPCCtrlMxvType {
    /**
     * 默认，图像显示在右下角
     */
    RTMPC_MXV_NULL (0),
    /**
     * 图像显示在主屏
     */
    RTMPC_MXV_MAIN(1),
    /**
     * 图像显示在左侧
     */
    RTMPC_MXV_B_LEFT(2),
    /**
     * 图像显示在右侧（右下角）
     */
    RTMPC_MXV_B_RIGHT(3);

    public final int level;

    AnyRTCRTMPCCtrlMxvType(int level) {
        this.level = level;
    }
}

package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/10.
 */
@Deprecated
public enum AnyRTCScreenOrientation {
    /**
     * 横屏
     */
    AnyRTC_SCRN_Portrait(0),
    /**
     * 竖屏
     */
    AnyRTC_SCRN_Landscape(1);

    public final int level;

    private AnyRTCScreenOrientation(int level) {
        this.level = level;
    }
}

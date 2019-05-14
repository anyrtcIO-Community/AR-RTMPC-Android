package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/12.
 */
@Deprecated
public enum AnyRTCVideoLayout {
    AnyRTC_V_1X3(0),
    AnyRTC_V_3X3_auto(1);

    public final int level;

    private AnyRTCVideoLayout(int level) {
        this.level = level;
    }
}

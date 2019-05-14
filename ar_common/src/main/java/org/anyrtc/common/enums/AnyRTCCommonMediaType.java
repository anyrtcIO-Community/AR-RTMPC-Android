package org.anyrtc.common.enums;

/**
 * Created by Skyline on 2017/11/12.
 */
@Deprecated
public enum AnyRTCCommonMediaType {
    AnyRTC_M_Video(0),
    AnyRTC_M_Audio(1);

    public final int type;

    private AnyRTCCommonMediaType(int type) {
        this.type = type;
    }
}

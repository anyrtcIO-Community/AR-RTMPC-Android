package org.anyrtc.common.enums;

/**
 *
 * @author Skyline
 * @date 2018/4/23
 */
@Deprecated
public enum AnyRTCMeetMode {
    AnyRTC_Meet_Normal(0),
    AnyRTC_Meet_Host(1),
    AnyRTC_Meet_ZOOM(3);

    public final int type;

    private AnyRTCMeetMode(int type) {
        this.type = type;
    }
}

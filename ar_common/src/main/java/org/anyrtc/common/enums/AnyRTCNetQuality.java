package org.anyrtc.common.enums;
@Deprecated
public enum AnyRTCNetQuality {
    /**
     * 优
     */
    AnyRTCNetQualityExcellent(0),
    /**
     * 良好
     */
    AnyRTCNetQualityGood(1),
    /**
     * 中等
     */
    AnyRTCNetQualityAccepted(2),

    /**
     * 差
     */
    AnyRTCNetQualityBad(3),

    /**
     * 极差
     */
    AnyRTCNetQualityVBad(4);

    public final int type;

    private AnyRTCNetQuality(int type) {
        this.type = type;
    }
}
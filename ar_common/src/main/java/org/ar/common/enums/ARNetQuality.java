package org.ar.common.enums;

/**
 * 视频传输时网络质量  根据丢包率计算，packetsLost/totalPackets  0~1%优 1%~3%良好 3%~5%中等 5~10%差 >10%极差
 */
public enum ARNetQuality {
    /**
     * 优
     */
    ARNetQualityExcellent(0),
    /**
     * 良好
     */
    ARNetQualityGood(1),
    /**
     * 中等
     */
    ARNetQualityAccepted(2),

    /**
     * 差
     */
    ARNetQualityBad(3),

    /**
     * 极差
     */
    ARNetQualityVBad(4);

    public final int type;

    private ARNetQuality(int type) {
        this.type = type;
    }
}

package org.anyrtc.common.enums;

@Deprecated
public enum AnyRTCLogLevel {
    /**
     * 不打印日志
     */
    None(0),
    /**
     * 信息日志
     */
    Info(1),
    /**
     * 警告日志
     */
    Warning(2),

    /**
     * 错误日志
     */
    Error(3),

    /**
     * 所有日志
     */
    All(4);

    public final int type;

    private AnyRTCLogLevel(int type) {
        this.type = type;
    }
}

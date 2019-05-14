package org.ar.common.utils;

/**
 * anyRTC error code description.
 *
 * @author Skyline
 * @date 2017/9/19
 */
public class ARError {

    /**
     * anyRTC通用错误码
     */
    public enum AnyRTCErrorCode {
        /**
         * 正常
         */
        AnyRTC_OK(0),

        /**
         * 未知错误
         */
        AnyRTC_UNKNOW(1),

        /**
         * SDK调用异常
         */
        AnyRTC_EXCEPTION(2),

        /**
         * SDK未初始化
         */
        AnyRTC_EXP_UNINIT(3),

        /**
         * 参数非法
         */
        AnyRTC_EXP_PARAMS_INVALIDE(4),

        /**
         * 没有网络链接
         */
        AnyRTC_EXP_NO_NETWORK(5),

        /**
         * 没有找到摄像头设备
         */
        AnyRTC_EXP_NOT_FOUND_CAMERA(6),

        /**
         * 没有打开摄像头权限
         */
        AnyRTC_EXP_NO_CAMERA_PERMISSION(7),

        /**
         * 没有音频录音权限
         */
        AnyRTC_EXP_NO_AUDIO_PERMISSION(8),

        /**
         * 浏览器不支持原生的webrtc
         */
        AnyRTC_EXP_NOT_SUPPORT_WEBRTC(9),

        /**
         * 网络错误
         */
        AnyRTC_NET_ERR(100),

        /**
         * 网络断开
         */
        AnyRTC_NET_DISSCONNECT(101),

        /**
         * 直播出错
         */
        AnyRTC_LIVE_ERR(102),

        /**
         * 异常错误
         */
        AnyRTC_EXP_ERR(103),

        /**
         * 服务不支持的错误请求
         */
        AnyRTC_BAD_REQ(201),

        /**
         * 认证失败
         */
        AnyRTC_AUTH_FAIL(202),

        /**
         * 此开发者信息不存在
         */
        AnyRTC_NO_USER(203),

        /**
         * 服务器内部数据库错误
         */
        AnyRTC_SQL_ERR(204),

        /**
         * 账号欠费
         */
        AnyRTC_ARREARS(205),

        /**
         * 账号被锁定
         */
        AnyRTC_LOCKED(206),

        /**
         * 强制离开
         */
        AnyRTC_FORCE_EXIT(207),

        /**
         * anyRTC ID非法(仅会议和RTCP中检测)
         */
        AnyRTC_ID_INVALIDE(208),

        /**
         * 服务未开通
         */
        AnyRTC_SERVICE_CLOSED(209),

        /**
         * Bundle ID不匹配
         */
        AnyRTC_BUNDLE_ID_ERR(210),

        /**
         * 订阅的PubID已过期
         */
        AnyRTC_PUB_GONE(211),

        /**
         * 没有RTC服务器
         */
        AnyRTC_NO_RTC_SVR(212);

        public final int type;

        private AnyRTCErrorCode(int type) {
            this.type = type;
        }
    }

    /**
     * 互动直播错误码
     */
    public enum AnyRTCLiveErrorCode {
        /**
         * 正常
         */
        RTCLive_OK(0),

        /**
         * 直播未开始
         */
        RTCLive_NOT_START(600),

        /**
         * 主播拒绝连麦
         */
        RTCLive_HOSTER_REJECT(601),

        /**
         * 连麦已满
         */
        RTCLive_LINE_FULL(602),

        /**
         * 游客关闭错误，onRtmpPlayerClosed
         */
        RTCLive_CLOSE_ERR(603),

        /**
         * 直播已经开始，不能重复开启
         */
        RTCLive_HAS_OPENED(604),

        /**
         * 直播已结束
         */
        RTCLive_IS_STOP(605);

        public final int type;

        private AnyRTCLiveErrorCode(int type) {
            this.type = type;
        }
    }

    /**
     * 视频会议错误码
     */
    public enum AnyRTCMeetErrorCode {
        /**
         * 正常
         */
        RTCMeet_OK (0),
        /**
         *  会议未开始
         */
        RTCMeet_NOT_START(700),
        /**
         * 会议室已满
         */
        RTCMeet_IS_FULL(701),
        /**
         * 会议类型不匹配
         */
        RTCMeet_NOT_COMPARE(702);

        public final int type;

        private AnyRTCMeetErrorCode(int type) {
            this.type = type;
        }
    }

    /**
     * P2P呼叫错误码
     */
    public enum AnyRTCP2PErrorCode {
        /**
         * 正常
         */
        RTCCall_OK(0),

        /**
         * 对方正忙
         */
        RTCCall_PEER_BUSY(800),

        /**
         * 对方不在线
         */
        RTCCall_OFFLINE(801),

        /**
         * 不能呼叫自己
         */
        RTCCall_NOT_SELF(802),

        /**
         * 通话中对方意外掉线
         */
        RTCCall_EXP_OFFLINE(803),

        /**
         * 对方异常导致(如：重复登录帐号将此前的帐号踢出)
         */
        RTCCall_EXP_EXIT(804),

        /**
         * 呼叫超时(45秒)
         */
        RTCCall_TIMEOUT(805),

        /**
         * 不支持
         */
        RTCCall_NOT_SURPPORT(806);

        public final int type;

        private AnyRTCP2PErrorCode(int type) {
            this.type = type;
        }
    }

    /**
     * 对讲错误码
     */
    public enum AnyRTCTalkErrorCode {
        /**
         * 正常
         */
        RTCTalk_OK(0),

        /**
         * 申请麦但是服务器异常 (没有MCU服务器,暂停申请),
         */
        RTCTalk_APPLY_SVR_ERR(800),

        /**
         * 当前你正在忙
         */
        RTCTalk_APPLY_BUSY(801),

        /**
         * 当前麦被占用 (有人正在说话切你的权限不够)
         */
        RTCTalk_APPLY_NO_PRIO(802),

        /**
         * 正在初始化中 (自身的通道没有发布成功,不能申请)
         */
        RTCTalk_APPLY_INITING(803),

        /**
         * 等待上麦
         */
        RTCTalk_APPLY_ING(804),

        /**
         * 麦被抢掉了
         */
        RTCTalk_ROBBED(810),

        /**
         * 麦被释放了
         */
        RTCTalk_BREAKED(811),

        /**
         * 麦被释放了，因为要对讲
         */
        RTCTalk_RELEASED_BY_P2P(812),

        /**
         * 强插时，对方可能不在线了或异常离线
         */
        RTCTalk_P2P_OFFLINE(820),

        /**
         * 强插时，对方正忙
         */
        RTCTalk_P2P_BUSY(821),

        /**
         * 强插时，对方不在麦上
         */
        RTCTalk_P2P_NOT_TALK(822),

        /**
         * 视频监看时，对方不在线，或下线了
         */
        RTCTalk_V_MON_OFFLINE(830),

        /**
         * 视频监看被抢占了
         */
        RTCTalk_V_MON_GRABED(831),

        /**
         * 对方不在线或掉线了
         */
        RTCTalk_CALL_OFFLINE(840),

        /**
         * 发起呼叫时自己有其他业务再进行(资源被占用)
         */
        RTCTalk_CALL_NO_PRIO(841),

        /**
         * 会话不存在
         */
        RTCTalk_CALL_NOT_FOUND(842);

        public final int type;

        private AnyRTCTalkErrorCode(int type) {
            this.type = type;
        }
    }
}

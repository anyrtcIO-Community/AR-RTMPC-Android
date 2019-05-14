package org.ar.common.enums;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */
public class ARErrorCode {

    public enum ARRtcp {
        ARRtcp_OK(0),                     // 正常
        ARRtcp_UNKNOW(1),                     // 未知错误
        ARRtcp_EXCEPTION(2),                  // SDK调用异常
        ARRtcp_EXP_UNINIT(3),                 // SDK未初始化
        ARRtcp_EXP_PARAMS_INVALIDE(4),        // 参数非法
        ARRtcp_EXP_NO_NETWORK(5),             // 没有网络
        ARRtcp_EXP_NOT_FOUND_CAMERA(6),       // 没有找到摄像头设备
        ARRtcp_EXP_NO_CAMERA_PERMISSION(7),   // 没有打开摄像头权限:
        ARRtcp_EXP_NO_AUDIO_PERMISSION(8),    // 没有音频录音权限
        ARRtcp_EXP_NOT_SUPPOAR_WEBARC(9),     // 浏览器不支持原生的webARc

        ARRtcp_NET_ERR(100),              // 网络错误
        ARRtcp_NET_DISSCONNECT(101),      // 网络断开
        ARRtcp_LIVE_ERR(102),             // 直播出错
        ARRtcp_EXP_ERR(103),              // 异常错误
        ARRtcp_EXP_UNAUTHORIZED(104),     // 服务未授权

        ARRtcp_BAD_REQ(201),             // 服务不支持的错误请求
        ARRtcp_AUTH_FAIL(202),            // 认证失败
        ARRtcp_NO_USER(203),               // 此开发者信息不存在
        ARRtcp_SVR_ERR(204),              // 服务器内部错误
        ARRtcp_SQL_ERR(205),              // 服务器内部数据库错误
        ARRtcp_ARREARS(206),              // 账号欠费
        ARRtcp_LOCKED(207),               // 账号被锁定
        ARRtcp_SERVER_NOT_OPEN(208),      // 服务未开通
        ARRtcp_ALLOC_NO_RES(209),         // 没有服务资源
        ARRtcp_SERVER_NO_SURPPOAR(210),   // 不支持的服务
        ARRtcp_FORCE_EXIT(211),           // 强制离开
        ARRtcp_NOT_START(800);          // 会议未开始

        public final int code;

        private ARRtcp(int code) {
            this.code = code;
        }

    }

    public enum ARMeet {
        ARMeet_OK(0),// 正常
        ARMeet_UNKNOW(1),// 未知错误
        ARMeet_EXCEPTION(2),// SDK调用异常
        ARMeet_EXP_UNINIT(3),// SDK未初始化
        ARMeet_EXP_PARAMS_INVALIDE(4),//参数非法
        ARMeet_EXP_NO_NETWORK(5),// 没有网络
        ARMeet_EXP_NOT_FOUND_CAMERA(6),       // 没有找到摄像头设备
        ARMeet_EXP_NO_CAMERA_PERMISSION(7),   // 没有打开摄像头权限:
        ARMeet_EXP_NO_AUDIO_PERMISSION(8),    // 没有音频录音权限
        ARMeet_EXP_NOT_SUPPOAR_WEBARC(9),     // 浏览器不支持原生的webARc
        ARMeet_NET_ERR(100),              // 网络错误
        ARMeet_NET_DISSCONNECT(101),      // 网络断开
        ARMeet_LIVE_ERR(102),             // 直播出错
        ARMeet_EXP_ERR(103),              // 异常错误
        ARMeet_EXP_UNAUTHORIZED(104),     // 服务未授权

        ARMeet_BAD_REQ(201),             // 服务不支持的错误请求
        ARMeet_AUTH_FAIL(202),            // 认证失败
        ARMeet_NO_USER(203),               // 此开发者信息不存在
        ARMeet_SVR_ERR(204),              // 服务器内部错误
        ARMeet_SQL_ERR(205),              // 服务器内部数据库错误
        ARMeet_ARREARS(206),              // 账号欠费
        ARMeet_LOCKED(207),               // 账号被锁定
        ARMeet_SERVER_NOT_OPEN(208),      // 服务未开通
        ARMeet_ALLOC_NO_RES(209),         // 没有服务资源
        ARMeet_SERVER_NO_SURPPOAR(210),   // 不支持的服务
        ARMeet_FORCE_EXIT(211),           // 强制离开

        ARMeet_NOT_STAAR(700),            // 房间未开始
        ARMeet_IS_FULL(701),              // 房间人员已满
        ARMeet_NOT_COMPARE(702);        // 房间类型不匹配
        public final int code;

        private ARMeet(int code) {
            this.code = code;
        }

    }

    public enum ARP2P{

        ARP2P_OK(0),// 正常
        ARP2P_UNKNOW(1),// 未知错误
        ARP2P_EXCEPTION(2),// SDK调用异常
        ARP2P_EXP_UNINIT(3),// SDK未初始化
        ARP2P_EXP_PARAMS_INVALIDE(4),//参数非法
        ARP2P_EXP_NO_NETWORK(5),// 没有网络
        ARP2P_EXP_NOT_FOUND_CAMERA(6),       // 没有找到摄像头设备
        ARP2P_EXP_NO_CAMERA_PERMISSION(7),   // 没有打开摄像头权限:
        ARP2P_EXP_NO_AUDIO_PERMISSION(8),    // 没有音频录音权限
        ARP2P_EXP_NOT_SUPPOAR_WEBARC(9),     // 浏览器不支持原生的webARc
        ARP2P_NET_ERR(100),              // 网络错误
        ARP2P_NET_DISSCONNECT(101),      // 网络断开
        ARP2P_LIVE_ERR(102),             // 直播出错
        ARP2P_EXP_ERR(103),              // 异常错误
        ARP2P_EXP_UNAUTHORIZED(104),     // 服务未授权

        ARP2P_BAD_REQ(201),             // 服务不支持的错误请求
        ARP2P_AUTH_FAIL(202),            // 认证失败
        ARP2P_NO_USER(203),               // 此开发者信息不存在
        ARP2P_SVR_ERR(204),              // 服务器内部错误
        ARP2P_SQL_ERR(205),              // 服务器内部数据库错误
        ARP2P_ARREARS(206),              // 账号欠费
        ARP2P_LOCKED(207),               // 账号被锁定
        ARP2P_SERVER_NOT_OPEN(208),      // 服务未开通
        ARP2P_ALLOC_NO_RES(209),         // 没有服务资源
        ARP2P_SERVER_NO_SURPPOAR(210),   // 不支持的服务
        ARP2P_FORCE_EXIT(211),           // 强制离开
        ARP2P_PEER_BUSY (800),          // 对方正忙
        ARP2P_OFFLINE(801),                  // 对方不在线
        ARP2P_NOT_SELF(802),                 // 不能呼叫自己
        ARP2P_EXP_OFFLINE(803),              // 通话中对方意外掉线
        ARP2P_EXP_EXIT(804),                 // 对方异常导致(如：重复登录帐号将此前的帐号踢出)
        ARP2P_TIMEOUT(805),                  // 呼叫超时(45秒)
        ARP2P_NOT_SURPPORT(806);           // 不支持


        public final int code;

        private ARP2P(int code) {
            this.code = code;
        }
    }

    public enum ARRtmpc{

        ARRtmpc_OK(0),// 正常
        ARRtmpc_UNKNOW(1),// 未知错误
        ARRtmpc_EXCEPTION(2),// SDK调用异常
        ARRtmpc_EXP_UNINIT(3),// SDK未初始化
        ARRtmpc_EXP_PARAMS_INVALIDE(4),//参数非法
        ARRtmpc_EXP_NO_NETWORK(5),// 没有网络
        ARRtmpc_EXP_NOT_FOUND_CAMERA(6),       // 没有找到摄像头设备
        ARRtmpc_EXP_NO_CAMERA_PERMISSION(7),   // 没有打开摄像头权限:
        ARRtmpc_EXP_NO_AUDIO_PERMISSION(8),    // 没有音频录音权限
        ARRtmpc_EXP_NOT_SUPPOAR_WEBARC(9),     // 浏览器不支持原生的webARc
        ARRtmpc_NET_ERR(100),              // 网络错误
        ARRtmpc_NET_DISSCONNECT(101),      // 网络断开
        ARRtmpc_LIVE_ERR(102),             // 直播出错
        ARRtmpc_EXP_ERR(103),              // 异常错误
        ARRtmpc_EXP_UNAUTHORIZED(104),     // 服务未授权

        ARRtmpc_BAD_REQ(201),             // 服务不支持的错误请求
        ARRtmpc_AUTH_FAIL(202),            // 认证失败
        ARRtmpc_NO_USER(203),               // 此开发者信息不存在
        ARRtmpc_SVR_ERR(204),              // 服务器内部错误
        ARRtmpc_SQL_ERR(205),              // 服务器内部数据库错误
        ARRtmpc_ARREARS(206),              // 账号欠费
        ARRtmpc_LOCKED(207),               // 账号被锁定
        ARRtmpc_SERVER_NOT_OPEN(208),      // 服务未开通
        ARRtmpc_ALLOC_NO_RES(209),         // 没有服务资源
        ARRtmpc_SERVER_NO_SURPPOAR(210),   // 不支持的服务
        ARRtmpc_FORCE_EXIT(211),           // 强制离开

        ARRtmpc_NOT_START (600),             // 直播未开始
        ARRtmpc_HOSTER_REJECT (601),         // 主播拒绝连麦
        ARRtmpc_LINE_FULL (602),             // 连麦已满
        ARRtmpc_CLOSE_ERR (603),             // 游客关闭错误，onRtmpPlayerClosed
        ARRtmpc_HAS_OPENED (604),            // 直播已经开始，不能重复开启
        ARRtmpc_IS_STOP (605);               // 直播已结束


        public final int code;

        private ARRtmpc(int code) {
            this.code = code;
        }
    }
}

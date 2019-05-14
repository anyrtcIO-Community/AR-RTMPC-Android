package org.anyrtc.rtmpc_hybrid;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public class RTMPCAudioGuestKit {
    private static final String TAG = "RTMPCAudioGuestKit";

    private RTMPCGuestKit mGuestKit;

    /**
     * 实例化游客对象
     *
     * @param guestListener RTMPCGuestHelper 回调接口实现类
     */
    public RTMPCAudioGuestKit(final RTMPCAudioGuestEvent guestListener) {
        RTMPCGuestVideoOption option = new RTMPCGuestVideoOption();
        option.setmBAudio(true);
        mGuestKit = new RTMPCGuestKit(guestListener, option);
    }

    /**
     * 销毁游客端
     */
    public void clear() {
        mGuestKit.clear();
    }

    /**
     * 设置验证token
     *
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    public boolean setUserToken(final String strUserToken) {
        return mGuestKit.setUserToken(strUserToken);
    }

    /**
     * 打开或关闭本地音频
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setLocalAudioEnable(final boolean bEnable) {
        mGuestKit.setLocalAudioEnable(bEnable);
    }

    /**
     * 打开或关闭本地视频
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setLocalVideoEnable(final boolean bEnable) {
        mGuestKit.setLocalVideoEnable(bEnable);
    }


    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        mGuestKit.switchCamera();
    }


    /**
     * 开始播放rtmp流
     *
     * @param strUrl rtmp 流地址
     */
    public void startRtmpPlay(final String strUrl) {
        mGuestKit.startRtmpPlay(strUrl, 0);
    }

    /**
     * 停止RTMP播放
     */
    public void stopRtmpPlay() {
        mGuestKit.stopRtmpPlay();
    }

    /**
     * 游客加入RTC连接
     *
     * @param strAnyRTCId 主播对应的anyRTCid
     * @param strUserId   游客业务平台的用户id，可选。（若不设置， sendUserMsg和sendBarrage不能使用）
     * @param strUserData 游客业务平台自定义数据（json格式）, 最大值512字节
     * @return 返回结果。0：调用成功；4：参数非法；
     */
    public int joinRTCLine(final String strAnyRTCId, final String strUserId, final String strUserData) {
        return mGuestKit.joinRTCLine(strAnyRTCId, strUserId, strUserData);
    }

    /**
     * 申请连麦
     */
    public void applyRTCLine() {
        mGuestKit.applyRTCLine();
    }

    /**
     * 挂断连麦
     */
    public void hangupRTCLine() {
        mGuestKit.hangupRTCLine();
    }

    /**
     * 发送消息、弹幕等文本信息
     *
     * @param nType            消息类型:0:普通消息;1:弹幕消息
     * @param strUserName      消息发送者的业务平台昵称（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url（最大1024个字节）
     * @param strContent       消息内容（最大1024个字节）
     * @return 0：成功；1：失败；4：参数非法；如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    public int sendUserMessage(final int nType, final String strUserName, final String strUserHeaderUrl,
                               final String strContent) {
        return mGuestKit.sendUserMessage(nType, strUserName, strUserHeaderUrl, strContent);
    }


    /**
     * 关掉RTC连线
     */
    public void leaveRTCLine() {
        mGuestKit.leaveRTCLine();
    }

}

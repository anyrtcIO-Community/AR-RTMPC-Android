package org.anyrtc.rtmpc_hybrid;

/**
 * Created by Skyline on 2017/11/20.
 */
@Deprecated
public class RTMPCAudioHosterKit {
    private static final String TAG = "RTMPCAudioHosterKit";

    private RTMPCHosterKit mRTMPCHoster;

    /**
     * 实例化主播对象
     *
     * @param hosterListener RTMPCAudioHosterEvent 回调接口实现类
     */
    public RTMPCAudioHosterKit(final RTMPCAudioHosterEvent hosterListener, final boolean bAudioDetect) {
        RTMPCHybrid.Inst().setAudioModel(true, bAudioDetect);
        mRTMPCHoster = new RTMPCHosterKit(hosterListener, null);

    }

    /**
     * 设置验证token
     *
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    public boolean setUserToken(final String strUserToken) {
        return mRTMPCHoster.setUserToken(strUserToken);
    }

    /**
     * 销毁主播端
     */
    public void clear() {
        mRTMPCHoster.clear();
    }

    /**
     * 设置录像地址（地址为拉流地址）
     * 说明：设置Rtmp录制地址，需放在开始推流方法前.并且必须在平台上开启录像服务
     *
     * @param strUrl 拉流地址
     */
    private void setRtmpRecordUrl(final String strUrl) {
//        mRTMPCHoster.setRtmpRecordUrl(strUrl);
    }

    /**
     * 打开或关闭本地音频
     *
     * @param bEnabled true: 打开; false: 关闭
     */
    public void setLocalAudioEnable(final boolean bEnabled) {
        mRTMPCHoster.setLocalAudioEnable(bEnabled);
    }


    //***************************************Rtmp function for push rtmp stream*******************************

    /**
     * 开始推流
     *
     * @param strPushUrl 推流地址
     */
    public int startPushRtmpStream(final String strPushUrl) {
        return mRTMPCHoster.startPushRtmpStream(strPushUrl);
    }

    /**
     * 停止推流
     */
    public void stopRtmpStream() {
        mRTMPCHoster.stopRtmpStream();
    }

    //****************************************RTC function for line*******************************************

    /**
     * 建立RTC连接
     *
     * @param strAnyrtcId
     * @param strUserId
     * @param strUserData
     * @return
     */
    public boolean createRTCLine(final String strAnyrtcId, final String strUserId, final String strUserData, final String strLiveInfo) {
        return mRTMPCHoster.createRTCLine(strAnyrtcId, strUserId, strUserData, strLiveInfo);
    }

    /**
     * 同意游客连麦请求
     * 说明：调用此方法即可同意游客的连麦请求，然后将会回调显示连麦视频方法，具体操作
     * 可查看接口OnRTCApplyToLine回调-连麦视频显示说明
     *
     * @param strLivePeerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void acceptRTCLine(final String strLivePeerId) {
        mRTMPCHoster.acceptRTCLine(strLivePeerId);
    }

    /**
     * 挂断游客连麦
     * 说明：与游客连麦过程中，可调用此方法挂断与他的连麦
     *
     * @param strLivePeerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void hangupRTCLine(final String strLivePeerId) {
        mRTMPCHoster.hangupRTCLine(strLivePeerId);
    }

    /**
     * 拒绝游客连麦请求
     * 说明：当有游客请求连麦时，可调用此方法拒绝
     *
     * @param strLivePeerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void rejectRTCLine(final String strLivePeerId) {
        mRTMPCHoster.rejectRTCLine(strLivePeerId);
    }

    /**
     * 发送消息、弹幕等文本信息
     *
     * @param nType            消息类型:0:普通消息;1:弹幕消息
     * @param strUserName      消息发送者的业务平台昵称（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url（最大512个字节）
     * @param strContent       消息内容（最大256个字节）
     * @return 返回结果，0：成功；1：失败；4：参数非法；如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    public int sendUserMessage(final int nType, final String strUserName, final String strUserHeaderUrl,
                               final String strContent) {
        return mRTMPCHoster.sendUserMessage(nType, strUserName, strUserHeaderUrl, strContent);
    }


    /**
     * 关闭RTC
     * 说明:一般不调用，clear时已清除
     */
    public void closeRTCLine() {
        mRTMPCHoster.closeRTCLine();
    }
}

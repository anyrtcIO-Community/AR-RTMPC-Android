package org.anyrtc.rtmpc_hybrid;

/**
 * Created by Eric on 2016/7/25.
 */
@Deprecated
public interface RTMPCHosterHelper {

    //*********************************** RTMP Callback *******************************************

    /**
     * rtmp 服务器连接成功
     */
    public void OnRtmpStreamOK();

    /**
     * rtmp 服务器重连
     *
     * @param nTimes 当前已重连几次
     */
    public void OnRtmpStreamReconnecting(int nTimes);

    /**
     * rtmp 推流状态
     *
     * @param nDelayTime 推流延时时间。（单位：毫秒）
     * @param nBirate    推流码率。（单位：byte）
     */
    public void OnRtmpStreamStatus(int nDelayTime, int nBirate);

    /**
     * rtmp 推流失败
     *
     * @param nCode 响应码
     */
    public void OnRtmpStreamFailed(int nCode);

    /**
     * rtmp 推流关闭
     */
    public void OnRtmpStreamClosed();

    public void OnRtmpAudioLevel(String strLivePeerId, String strUserId, int nLevel);

    //*************************************** RTC Line callback *************************************

    /**
     * RTC服务连接结果
     *
     * @param nCode 响应码；当nCode=0时，连接RTC服务器成功；nCode为其他值时均为连接RTC服务器失败，具体原因查看响应码列表信息
     */
    public void OnRTCOpenLineResult(int nCode, String strReason);

    /**
     * 游客申请连麦
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param strUserId     游客在自己业务平台的userid
     * @param strUserData   游客加入RTC连接的自定义参数体（可查看游客端加入RTC连接方法）
     */
    public void OnRTCApplyToLine(String strLivePeerId, String strUserId, String strUserData);

    /**
     * 游客取消连麦申请；
     * nCode 正常取消0
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     */
    public void OnRTCCancelLine(int nCode, String strLivePeerId);

    /**
     * RTC服务关闭
     *
     * @param nCode 响应码
     */
    public void OnRTCLineClosed(int nCode, String strReason);

    /**
     * 游客视频连麦接通回调。 注：1.游客同样也在连麦中才会走该回调
     * 2：此时应调用设置其他连麦者视频显示方法（mGuestKit.SetRTCVideoRender()）显示连麦者图像
     * 具体使用请查看游客端主要方法-设置其他连麦者视频窗口
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param strPublishId  连麦者视频流id(用于标识连麦者发布的流)
     * @param strUserId     游客业务平台的用户id
     * @param strUserData   游客业务平台自定义数据（json格式）
     */
    public void OnRTCOpenVideoRender(String strLivePeerId, String strPublishId, String strUserId, String strUserData);

    /**
     * 游客视频连麦挂断回调。注：1.游客同样也在连麦中才会走该回调
     * 2.不论是其他游客主动挂断连麦还是主播挂断游客连麦均会走该回调。
     * 此时应及时调用mVideoView.OnrtcRemoveRemoteRender()方法移除其他游客连麦窗口。
     * 具体使用请查看游客端-主要方法-移除远程连麦窗口
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param strPublishId  连麦者视频流id(用于标识连麦者发布的流)
     * @param strUserId     游客业务平台的用户id
     */
    public void OnRTCCloseVideoRender(String strLivePeerId, String strPublishId, String strUserId);

    /**
     * 游客音频连麦接通回调（仅在音频直播时，游客申请音频连麦时有效）
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param strUserId     游客业务平台的用户id
     * @param strUserData   游客业务平台自定义数据（json格式）
     */
    public void OnRTCOpenAudioLine(String strLivePeerId, String strUserId, String strUserData);

    /**
     * 游客音频连麦挂断回调（仅在音频直播时，游客申请音频连麦时有效）
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param strUserId     游客业务平台的用户id
     */
    public void OnRTCCloseAudioLine(String strLivePeerId, String strUserId);

    /**
     * OnRTCAVStatus
     *
     * @param strLivePeerId
     * @param audio
     * @param video
     */
    public void OnRTCAVStatus(String strLivePeerId, boolean audio, boolean video);

    /**
     * 音频监测。说明：仅在RTMPCHybird中setAuidoModel(boolean bAudioOnly, boolean bAudioDetect)的bAudioOnly和AudioDetect均为true时可用
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param strUserID     游客业务平台的用户id，可选。（joinRTCLine时若不设置， 此处不会回调）
     * @param nTime         音频检测在nTime毫秒内不会再回调该方法（单位：毫秒）
     */
    public void OnRTCAudioActive(String strLivePeerId, String strUserID, int nTime);

    //************************************ RTC Other callback ******************************************

    /**
     * 接收到消息信息回调
     *
     * @param strUserId        消息发送者的业务平台的用户id，可选。（joinRTCLine时若不设置， 此处不会回调）
     * @param strUserName      消息发送者的业务平台昵称。（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url。（最大256个字节）
     * @param strMessage       消息内容。（最大1024个字节）
     */
    public void OnRTCUserMessage(String strUserId, String strUserName, String strUserHeaderUrl, String strMessage);

    /**
     * 接收到弹幕信息回调
     *
     * @param strUserId        消息发送者的业务平台的用户id，可选。（joinRTCLine时若不设置， 此处不会回调）
     * @param strUserName      消息发送者的业务平台昵称。（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url。（最大256个字节）
     * @param strBarrage       消息内容。（最大1024个字节）
     */
    public void OnRTCUserBarrage(String strUserId, String strUserName, String strUserHeaderUrl, String strBarrage);

    /**
     * 直播间实时在线人数变化通知
     *
     * @param strServerId  服务器id
     * @param strRoomId    房间ID
     * @param nTotalMember 总人数
     */
    public void OnRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember);

    /**
     * 局域网扫描接口回调
     *
     * @param strPeerScrnId 扫描得到视频id
     * @param strName       设备名字
     * @param strPlatform   平台
     */
    public void OnRTCLanScreenFound(String strPeerScrnId, String strName, String strPlatform);

    /**
     * 局域网屏幕关闭回调
     *
     * @param strPeerScrnId 视频id
     */
    public void OnRTCLanScreenClosed(String strPeerScrnId);

//    public void OnRTCMember(String strCustomID, String strUserData);
//    public void OnRTCMemberListUpdateDone();
}

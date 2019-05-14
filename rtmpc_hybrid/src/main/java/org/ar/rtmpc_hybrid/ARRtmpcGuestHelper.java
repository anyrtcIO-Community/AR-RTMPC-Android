package org.ar.rtmpc_hybrid;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */
public interface ARRtmpcGuestHelper {

    //**********************************For RTMPCGuesterEvent******************************************

    /**
     * rtmp 服务器连接成功，视频正在缓存，第一帧视频图像开始时会在onRtmpPlayerStart中回调。
     */
    public void OnRtmplayerOK();

    /**
     * 视频接收到第一帧图像，即将开始播放。
     */
    public void OnRtmplayerStart();

    /**
     * rtmp 当前播放状态
     *
     * @param nCacheTime rtmp 播放器缓冲区时间（单位：毫秒）
     * @param nBitrate   rtmp 当前播放视频的码率（单位：byte）
     */
    public void OnRtmplayerStatus(int nCacheTime, int nBitrate);

    /**
     * 弱网下rtmp播放出现卡顿时，当前缓冲进度
     *
     * @param nPercent rtmp 缓存加载百分比（0~100）；说明:当回调该方法时，nPercent为0时，页面可以进行缓冲提示；当为100时，缓冲提示去掉
     */
    public void OnRtmplayerLoading(int nPercent);

    /**
     * rtmp 播放器关闭回调信息
     *
     * @param nCode 状态码；
     */
    public void OnRtmplayerClosed(int nCode);

    public void OnRtmpAudioLevel(String strLivePeerId, String strUserId, int level);

    //*********************************RTC Line callback**********************************

    /**
     * 连接RTC服务器回调
     *
     * @param nCode 响应码；当nCode=0时，连接RTC服务器成功；nCode为其他值时均为连接RTC服务器失败，具体原因查看响应码列表信息
     */
    public void OnRTCJoinLineResult(int nCode, String strReason);

    /**
     * 申请连麦结果
     * <p>
     * 注：此时应调用设置本地视频采集方法[  mGuestKit.SetVideoCapturer() ] 打开本地连麦窗口。具体使用请查看主要方法-设置本地视频采集
     *
     * @param nCode 响应码；当nCode=0时，连麦成功；nCode为其他值时均为连麦失败，具体原因查看响应码列表信息
     */
    public void OnRTCApplyLineResult(int nCode);

    /**
     * 主播挂断游客连麦
     */
    public void OnRTCHangupLine();

    /**
     * 主播RTC连接已断开（该回调里面说明主播已关闭了直播间）
     *
     * @param nCode 状态码待定
     */
    public void OnRTCLineLeave(int nCode, String strReason);

    /**
     * 其他游客视频连麦接通回调。 注：1.游客同样也在连麦中才会走该回调
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
     * 其他游客视频连麦挂断回调。注：1.游客同样也在连麦中才会走该回调
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
     * 其他游客音频连麦接通回调（仅在音频直播时，游客申请音频连麦时有效）
     *
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param strUserId     游客业务平台的用户id
     * @param strUserData   游客业务平台自定义数据（json格式）
     */
    public void OnRTCOpenAudioLine(String strLivePeerId, String strUserId, String strUserData);

    /**
     * 其他游客音频连麦挂断回调（仅在音频直播时，游客申请音频连麦时有效）
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
     * @param strUserId     游客业务平台的用户id，可选。（joinRTCLine时若不设置， 此处不会回调）
     * @param nTime         音频检测在nTime毫秒内不会再回调该方法（单位：毫秒）
     */
    public void OnRTCAudioActive(String strLivePeerId, String strUserId, int nTime);

    //*********************************RTC Other callback**********************************

    /**
     * 说明：
     * 直播在线功能开启后首次进入直播间，该方法会在Rtmp开始播放后回调。
     * OnRtmplayerStart  ->OnRTCLiveStart
     * 在看直播过程中，主播暂时离开再回来也将回调此方法，开发者可在此将暂时离开的状态移除，恢复正常观看直播
     * OnRTCLiveStart->OnRtmplayerStart  
     * 注：直播在线功能开启后才会有这个回调（可在www.anyrtc.io 应用管理中心开通）
     */
    public void OnRTCLiveStart();

    /**
     * 主播只要退出RTC服务，就会走该回调。开发者可在此做一些友好提示，如主播暂时离开。
     *  
     * 注：直播在线功能开启后才会有这个回调（可在www.anyrtc.io 应用管理中心开通）
     *     未开启的时候不会走该回调，若主播退出RTC服务，会走onRTCLeaveLine()方法
     */
    public void OnRTCLiveStop();

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
     * @param strUserID        消息发送者的业务平台的用户id，可选。（joinRTCLine时若不设置， 此处不会回调）
     * @param strUserName      消息发送者的业务平台昵称。（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url。（最大256个字节）
     * @param strBarrage       消息内容。（最大1024个字节）
     */
    public void OnRTCUserBarrage(String strUserID, String strUserName, String strUserHeaderUrl, String strBarrage);

    /**
     * 直播间实时在线人数变化通知
     *
     * @param nTotalMember 总人数
     */
    public void OnRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember);

    public void OnRTCUserShareOpen(int nType, String strUSInfo, String strUserId, String strUserData);

    public void OnRTCUserShareClose();
}

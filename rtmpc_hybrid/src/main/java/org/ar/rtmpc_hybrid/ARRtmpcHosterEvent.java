package org.ar.rtmpc_hybrid;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */
public abstract class ARRtmpcHosterEvent{

    //* RTMP Callback
    public abstract void onRtmpStreamOk();

    public abstract void onRtmpStreamReconnecting(int times);

    public abstract void onRtmpStreamStatus(int delayTime, int netBand);

    public abstract void onRtmpStreamFailed(int code);

    public abstract void onRtmpStreamClosed();

    //* RTC Line callback
    public abstract void onRTCCreateLineResult(int code, String reason);

    public abstract void onRTCApplyToLine(String peerId, String userId, String userData);

    //    public abstract void onRTCLineFull(String peerId, String userId, String userData);
    public abstract void onRTCCancelLine(int code, String peerId);

//    public abstract void onRtmpAudioLevel(String peerId, String userId, int level);

//    public abstract void onRTCOpenLineResult(int code, String reason);

    public abstract void onRTCLineClosed(int code,String reason);

    public abstract void onRTCOpenRemoteVideoRender(String peerId, String strPublishId, String userId, String userData);

    public abstract void onRTCCloseRemoteVideoRender(String peerId, String strPublishId, String userId);

    public abstract void onRTCOpenRemoteAudioLine(String peerId, String userId, String userData);

    public abstract void onRTCCloseRemoteAudioLine(String peerId, String userId);

    public abstract void onRTLocalAudioActive(int nTime);

    public abstract void onRTCRemoteAudioActive(String peerId, String userId, int nTime);

    public abstract void onRTCRemoteAVStatus(String peerId, boolean audio, boolean video);

    //* RTC Other callback
    public abstract void onRTCUserMessage(int type, String userId, String userName, String headerUrl, String message);

    public abstract void onRTCMemberNotify(String serverId, String roomId, int totalMember);

//    public abstract void onRTCLanScreenFound(String strPeerScrnId, String name, String strPlatform);
//
//    public abstract void onRTCLanScreenClosed(String strPeerScrnId);
}

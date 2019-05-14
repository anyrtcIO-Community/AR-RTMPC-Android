package org.ar.rtmpc_hybrid;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */
public abstract class ARRtmpcGuestEvent  {

    //* For RTMPCGuesterEvent
    public abstract void onRtmpPlayerOk();

    public abstract void onRtmpPlayerStart();

    public abstract void onRtmpPlayerStatus(int cacheTime, int bitrate);

    public abstract void onRtmpPlayerLoading(int percent);

    public abstract void onRtmpPlayerClosed(int code);

    //* RTC Line callback
    public abstract void onRTCJoinLineResult(int code, String reason);

    public abstract void onRTCApplyLineResult(int code);

    public abstract void onRTCHangupLine();

    public abstract void onRTCLineLeave(int code,String reason);

    public abstract void onRTCOpenRemoteVideoRender(String peerId, String publishId, String userId, String userData);

    public abstract void onRTCCloseRemoteVideoRender(String peerId, String publishId, String userId);

    public abstract void onRTCOpenRemoteAudioLine(String publishId, String userId, String userData);

    public abstract void onRTCCloseRemoteAudioLine(String publishId, String userId);

    public abstract void onRTCLocalAudioActive(int nTime);

    public abstract void onRTCHosterAudioActive(int nTime);

    public abstract void onRTCRemoteAudioActive(String peerId, String userId, int nTime);

    public abstract void onRTCRemoteAVStatus(String peerId, boolean audio, boolean video);

    //* RTC Other callback
//    public abstract void onRTCLiveStart();
//    public abstract void onRTCLiveStop();
    public abstract void onRTCUserMessage(int type, String userId, String userName, String headerUrl, String message);

    public abstract void onRTCMemberNotify(String serverId, String roomId, int totalMember);

//    public abstract void onRTCUserShareOpen(int nType, String strUSInfo, String userId, String userData);
//    public abstract void onRTCUserShareClose();
}

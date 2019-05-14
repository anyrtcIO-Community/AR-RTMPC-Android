package org.anyrtc.rtmpc_hybrid;

/**
 * Created by Skyline on 2016/8/1.
 */
@Deprecated
public abstract class RTMPCVideoGuestEvent implements RTMPCGuestHelper {
    @Override
    public void OnRtmplayerOK() {
        onRtmpPlayerOk();
    }

    @Override
    public void OnRtmplayerStart() {
        onRtmpPlayerStart();
    }

    @Override
    public void OnRtmplayerStatus(int nCacheTime, int nBitrate) {
        onRtmpPlayerStatus(nCacheTime, nBitrate);
    }

    @Override
    public void OnRtmplayerLoading(int nPercent) {
        onRtmpPlayerLoading(nPercent);
    }

    @Override
    public void OnRtmplayerClosed(int nCode) {
        onRtmpPlayerClosed(nCode);
    }

    @Override
    public void OnRtmpAudioLevel(String strLivePeerId, String strUserId, int nLevel) {
        if (nLevel > 0) {
            onRTCAudioActive(strLivePeerId, strUserId, 360);
        }
    }

    @Override
    public void OnRTCAudioActive(String strLivePeerId, String strUserId, int nTime) {
        onRTCAudioActive(strLivePeerId, strUserId, nTime);
    }

    @Override
    public void OnRTCAVStatus(String strLivePeerId, boolean bAudio, boolean bVideo) {
        onRTCAVStatus(strLivePeerId, bAudio, bVideo);
    }

    @Override
    public void OnRTCLiveStart() {
//        onRTCLiveStart();
    }

    @Override
    public void OnRTCLiveStop() {
//        onRTCLiveStop();
    }

    @Override
    public void OnRTCJoinLineResult(int nCode, String strReason) {
        onRTCJoinLineResult(nCode, strReason);
    }

    @Override
    public void OnRTCApplyLineResult(int nCode) {
        onRTCApplyLineResult(nCode);
    }

    @Override
    public void OnRTCHangupLine() {
        onRTCHangupLine();
    }

    @Override
    public void OnRTCLineLeave(int nCode, String strReason) {
        onRTCLineLeave(nCode);
    }

    @Override
    public void OnRTCOpenVideoRender(String strLivePeerId, String strPublishId, String strUserId, String strUserData) {
        onRTCOpenVideoRender(strLivePeerId, strPublishId, strUserId, strUserData);
    }

    @Override
    public void OnRTCCloseVideoRender(String strLivePeerId, String strPublishId, String strUserId) {
        onRTCCloseVideoRender(strLivePeerId, strPublishId, strUserId);
    }

    @Override
    public void OnRTCOpenAudioLine(String strLivePeerId, String strUserId, String strUserData) {
        onRTCOpenAudioLine(strLivePeerId, strUserId, strUserData);
    }

    @Override
    public void OnRTCCloseAudioLine(String strLivePeerId, String strUserId) {
        onRTCCloseAudioLine(strLivePeerId, strLivePeerId);
    }

    @Override
    public void OnRTCUserMessage(String strUserId, String strUserName, String strUserHeaderUrl, String strMessage) {
        onRTCUserMessage(0, strUserId, strUserName, strUserHeaderUrl, strMessage);
    }

    @Override
    public void OnRTCUserBarrage(String strCustomID, String strCustomName, String strCustomHeader, String strBarrage) {
        onRTCUserMessage(1, strCustomID, strCustomName, strCustomHeader, strBarrage);
    }

    @Override
    public void OnRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember) {
        onRTCMemberNotify(strServerId, strRoomId, nTotalMember);
    }

    @Override
    public void OnRTCUserShareOpen(int nType, String strUSInfo, String strUserId, String strUserData) {
        onRTCUserShareOpen(nType, strUSInfo, strUserId, strUserData);
    }

    @Override
    public void OnRTCUserShareClose() {
        onRTCUserShareClose();
    }

    //* For RTMPCGuesterEvent
    public abstract void onRtmpPlayerOk();

    public abstract void onRtmpPlayerStart();

    public abstract void onRtmpPlayerStatus(int nCacheTime, int nBitrate);

    public abstract void onRtmpPlayerLoading(int nPercent);

    public abstract void onRtmpPlayerClosed(int nCode);

    //* RTC Line callback
    public abstract void onRTCJoinLineResult(int nCode, String strReason);

    public abstract void onRTCApplyLineResult(int nCode);

    public abstract void onRTCHangupLine();

    public abstract void onRTCLineLeave(int nCode);

    public abstract void onRTCOpenVideoRender(String strLivePeerId, String strPublishId, String strUserId, String strUserData);

    public abstract void onRTCCloseVideoRender(String strLivePeerId, String strPublishId, String strUserId);

    public abstract void onRTCOpenAudioLine(String strLivePeerId, String strUserId, String strUserData);

    public abstract void onRTCCloseAudioLine(String strLivePeerId, String strUserId);

    public abstract void onRTCAudioActive(String strLivePeerId, String strUserId, int nTime);

    public abstract void onRTCAVStatus(String strLivePeerId, boolean audio, boolean video);

    //* RTC Other callback
//    public abstract void onRTCLiveStart();
//    public abstract void onRTCLiveStop();
    public abstract void onRTCUserMessage(int nType, String strCustomID, String strCustomName, String strCustomHeader, String strMessage);

    //    public abstract void onRTCUserBarrageCallback(int nType, String strCustomID, String strCustomName, String strCustomHeader, String strBarrage);
    public abstract void onRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember);
//    public abstract void onRTCMemberListWillUpdateCallback(int totalMembers);
//    public abstract void onRTCMemberCallback(String strCustomID, String strUserData);
//    public abstract void onRTCMemberListUpdateDoneCallback();

    public abstract void onRTCUserShareOpen(int nType, String strUSInfo, String strUserId, String strUserData);
    public abstract void onRTCUserShareClose();
}

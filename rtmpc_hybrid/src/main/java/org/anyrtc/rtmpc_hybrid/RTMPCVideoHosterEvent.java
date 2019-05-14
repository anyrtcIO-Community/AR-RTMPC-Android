package org.anyrtc.rtmpc_hybrid;

/**
 * Created by Skyline on 2016/8/1.
 */
@Deprecated
public abstract class RTMPCVideoHosterEvent implements RTMPCHosterHelper {

    @Override
    public void OnRTCApplyToLine(String strLivePeerId, String strUserId, String strUserData) {
        onRTCApplyToLine(strLivePeerId, strUserId, strUserData);
    }

    @Override
    public void OnRTCCancelLine(int nCode, String strLivePeerId) {
        onRTCCancelLine(nCode, strLivePeerId);
    }

    @Override
    public void OnRTCLineClosed(int nCode, String strReason) {
        onRTCLineClosed(nCode);
    }

    @Override
    public void OnRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember) {
        onRTCMemberNotify(strServerId, strRoomId, nTotalMember);
    }

    @Override
    public void OnRTCOpenLineResult(int nCode, String strReason) {
        onRTCCreateLineResult(nCode, strReason);
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
        onRTCCloseAudioLine(strLivePeerId, strUserId);
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
    public void OnRTCUserBarrage(String strUserId, String strUserName, String strUserHeader, String strBarrage) {
        onRTCUserMessage(1, strUserId, strUserName, strUserHeader, strBarrage);
    }

    @Override
    public void OnRTCUserMessage(String strUserId, String strUserName, String strUserHeader, String strMessage) {
        onRTCUserMessage(0, strUserId, strUserName, strUserHeader, strMessage);
    }

    @Override
    public void OnRtmpStreamClosed() {
        onRtmpStreamClosed();
    }

    @Override
    public void OnRtmpStreamFailed(int nCode) {
        onRtmpStreamFailed(nCode);
    }

    @Override
    public void OnRtmpStreamOK() {
        onRtmpStreamOk();
    }

    @Override
    public void OnRtmpStreamReconnecting(int times) {
        onRtmpStreamReconnecting(times);
    }

    @Override
    public void OnRtmpStreamStatus(int delayMs, int netBand) {
        onRtmpStreamStatus(delayMs, netBand);
    }

    @Override
    public void OnRtmpAudioLevel(String strLivePeerId, String strUserId, int nLevel) {
        if (nLevel > 0) {
            onRTCAudioActive(strLivePeerId, strUserId, 360);
        }
    }

    @Override
    public void OnRTCLanScreenFound(String strPeerScrnId, String strName, String strPlatform) {
        onRTCLanScreenFound(strPeerScrnId, strName, strPlatform);
    }

    @Override
    public void OnRTCLanScreenClosed(String strPeerScrnId) {
        onRTCLanScreenClosed(strPeerScrnId);
    }

    //* RTMP Callback
    public abstract void onRtmpStreamOk();

    public abstract void onRtmpStreamReconnecting(int nTimes);

    public abstract void onRtmpStreamStatus(int nDelayTime, int nNetBand);

    public abstract void onRtmpStreamFailed(int nCode);

    public abstract void onRtmpStreamClosed();

    //* RTC Line callback
    public abstract void onRTCCreateLineResult(int nCode, String strReason);

    public abstract void onRTCApplyToLine(String strLivePeerId, String strUserId, String strUserData);

    //    public abstract void onRTCLineFull(String strLivePeerId, String strUserId, String strUserData);
    public abstract void onRTCCancelLine(int nCode, String strLivePeerId);

    public abstract void onRTCLineClosed(int nCode);

    public abstract void onRTCOpenVideoRender(String strLivePeerId, String strPublishId, String strUserId, String strUserData);

    public abstract void onRTCCloseVideoRender(String strLivePeerId, String strPublishId, String strUserId);

    public abstract void onRTCOpenAudioLine(String strLivePeerId, String strUserId, String strUserData);

    public abstract void onRTCCloseAudioLine(String strLivePeerId, String strUserId);

    public abstract void onRTCAudioActive(String strLivePeerId, String strUserId, int nTime);

    public abstract void onRTCAVStatus(String strLivePeerId, boolean bAudio, boolean bVideo);

    //* RTC Other callback
    public abstract void onRTCUserMessage(int nType, String strUserId, String strUserName, String strUserHeaderUrl, String strMessage);

    //    public abstract void onRTCUserBarrageCallback(String strUserId, String strUserName, String strUserHeaderUrl, String strBarrage);
    public abstract void onRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember);
//    public abstract void OnRTCMemberListWillUpdateCallback(int totelMembers);
//    public abstract void OnRTCMemberCallback(String strUserId, String strUserData);
//    public abstract void OnRTCMemberListUpdateDoneCallback();

    public abstract void onRTCLanScreenFound(String strPeerScrnId, String strName, String strPlatform);

    public abstract void onRTCLanScreenClosed(String strPeerScrnId);
}

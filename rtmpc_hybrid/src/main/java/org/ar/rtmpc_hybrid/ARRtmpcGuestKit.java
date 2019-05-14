package org.ar.rtmpc_hybrid;

import android.content.pm.PackageManager;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import org.ar.common.enums.ARVideoCommon;
import org.anyrtc.common.utils.AnyRTCUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

import java.util.concurrent.Exchanger;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */
public class ARRtmpcGuestKit {
    private static final String TAG = "ARRtmpcGuestKit";
    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;
    private String mUserData;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;
    private ARRtmpcGuestEvent guestEvent;

    /**
     * 实例化游客对象
     *
     * @param guestEvent ARRtmpcGuestEvent 回调接口实现类
     */
    public ARRtmpcGuestKit(final ARRtmpcGuestEvent guestEvent) {
        AnyRTCUtils.assertIsTrue(guestEvent != null);
        this.guestEvent=guestEvent;
        mExecutor = ARRtmpcEngine.Inst().executor();
        mEglBase = ARRtmpcEngine.Inst().egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(guestHelper);
                if (ARRtmpcEngine.Inst().getGuestOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                    nativeSetLiveToAudioOnly(true, true);
                } else {
                    nativeSetLiveToAudioOnly(false, true);
                }
                if (ARRtmpcEngine.Inst().getGuestOption().getVideoOrientation() == ARVideoCommon.ARVideoOrientation.Portrait) {
                    nativeSetScreenToPortrait();
                } else {
                    nativeSetScreenToLandscape();
                }
            }
        });
    }

    /**
     * 设置验证token
     *
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    private boolean setUserToken(final String strUserToken) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                if (null == strUserToken || strUserToken.equals("")) {
                    ret = false;
                } else {
                    nativeSetUserToken(strUserToken);
                    ret = true;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 开始播放rtmp流
     *
     * @param pullUrl  rtmp 流地址
     * @param render 视频显示对象
     */
    public void startRtmpPlay(final String pullUrl, final long render) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeStartRtmpPlay(pullUrl, render);
                ARRtmpcEngine.Inst().checkSdk(10001015);
            }
        });
    }

    /**
     * 停止RTMP播放
     */
    public void stopRtmpPlay() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeStopRtmpPlay();
            }
        });
    }

    /**
     * 加载本地摄像头
     *
     * @param render 底层图像地址
     * @return 打开本地预览返回值：0/1/2：没有相机权限/打开成功/打开相机失败
     */
    public int setLocalVideoCapturer(final long render) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(ARRtmpcEngine.Inst().getContext(), CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    if (mVideoCapturer == null) {
                        mCameraId = 0;
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                        String frontCameraDeviceName =
                                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                        if (numberOfCameras > 1 && frontCameraDeviceName != null && ARRtmpcEngine.Inst().getGuestOption().isDefaultFrontCamera()) {
                            cameraDeviceName = frontCameraDeviceName;
                            mCameraId = 1;
                        }
                        Log.d(TAG, "Opening camera: " + cameraDeviceName);
                        mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                        if (mVideoCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 2);
                        }
                        /**
                         * 设置相机预览
                         */
                        nativeSetVideoCapturer(mVideoCapturer, render);
                        LooperExecutor.exchange(result, 1);
                    } else {
                        LooperExecutor.exchange(result, 3);
                    }
                } else {
                    LooperExecutor.exchange(result, 0);
                }
            }
        });

        return LooperExecutor.exchange(result, 0);
    }
    /**
     * 设置ARCamera视频回调数据
     * @param capturerObserver
     */
    public void setARCameraCaptureObserver(final VideoCapturer.ARCameraCapturerObserver capturerObserver) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setARCameraObserver(capturerObserver);
                }
            }
        });
    }

    /**
     *  设置是否采用ARCamera，默认使用ARCamera， 如果设置为false，必须调用setByteBufferFrameCaptured才能本地显示
     * @param usedARCamera true：使用ARCamera，false：不使用ARCamera采集的数据
     */
    public void setUsedARCamera(final boolean usedARCamera) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setUsedARCamera(usedARCamera);
                }
            }
        });
    }

    /**
     * 设置本地显示的视频数据
     * @param data 相机采集数据
     * @param width 宽
     * @param height 高
     * @param rotation 旋转角度
     * @param timeStamp 时间戳
     */
    public void setByteBufferFrameCaptured(final byte[] data, final int width, final int height, final int rotation, final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setByteBufferFrameCaptured(data, width, height, rotation, timeStamp);
                }
            }
        });
    }
    /**
     * 打开或关闭本地音频
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setLocalAudioEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioEnable(!bEnable);
            }
        });
    }

    /**
     * 打开或关闭本地视频
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setLocalVideoEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoEnable(bEnable);
            }
        });
    }

    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(null);
                }
            }
        });
    }


    /**
     * 游客加入RTC连接
     *
     * @param anyRTCId 主播对应的anyRTCid
     * @param userId   游客业务平台的用户id，可选。（若不设置， sendUserMsg和sendBarrage不能使用）
     * @param userData 游客业务平台自定义数据（json格式）, 最大值512字节
     * @return 返回结果。0：调用成功；4：参数非法；
     */
    public int joinRTCLine(final String token,final String anyRTCId, final String userId, final String userData) {
        if (userData.getBytes().length > 512) {
            return 4;
        }
        mUserData = userData;
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null!=token&&!token.equals("")) {
                    nativeSetUserToken(token);
                }
                nativeSetDeviceInfo(ARRtmpcEngine.Inst().getDeviceInfo());
                nativeJoinRTCLine(anyRTCId, userId, userData);
                ARRtmpcEngine.Inst().checkSdk(10001017);
                LooperExecutor.exchange(result, 0);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 申请连麦
     *
     * @return 0/1：失败（没有录音权限）/成功
     */
    public int applyRTCLine() {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(ARRtmpcEngine.Inst().getContext(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    nativeApplyRTCLine(mUserData == null ? "" : mUserData);
                    ARRtmpcEngine.Inst().checkSdk(10001016);
                    ret = 1;
                } else {
                    ret = 0;
                }

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 挂断连麦
     */
    public void hangupRTCLine() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                    }
                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
                nativeHangupRTCLine();
            }
        });
    }

    /**
     * 设置其他连麦者的视频窗口
     *
     * @param publishId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param render       视频显示对象
     */
    public void setRTCRemoteVideoRender(final String publishId, final long render) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRender(publishId, render);
            }
        });
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
    public int sendMessage(final int nType, final String strUserName, final String strUserHeaderUrl,
                               final String strContent) {
        if (strUserName.getBytes().length > 384) {
            return 4;
        }
        if (strUserHeaderUrl.getBytes().length > 1536) {
            return 4;
        }
        if (strContent.getBytes().length > 1536) {
            return 4;
        }

        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String headUrl = strUserHeaderUrl;
                if (headUrl.length() == 0) {
                    headUrl = "strCustomHeaderUrl can't be empty string";
                }
                boolean ret = false;
                if (nType == 0) {
                    ret = nativeSendUserMsg(strUserName, headUrl, strContent);
                } else if (nType == 1) {
                    ret = nativeSendBarrage(strUserName, headUrl, strContent);
                }
                LooperExecutor.exchange(result, ret ? 0 : 1);
            }
        });
        return LooperExecutor.exchange(result, 1);
    }


    /**
     * 更新经验值
     *
     * @param nExp 该直播间里的经验值。说明:用户在直播间里的经验值更新，调用该方法后，直播间里的人员列表排名将会更新。
     */
    private void updateExp(final int nExp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeUpdateExp(nExp);
            }
        });
    }

    /**
     * 关掉RTC连线
     */
    public void leaveRTCLine() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeLeaveRTCLine();
            }
        });
    }

    /**
     * 设置视频横屏模式
     */
    public void setScreenToLandscape() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToLandscape();
            }
        });
    }

    /**
     * 设置视频竖屏模式
     */
    public void setScreenToPortrait() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToPortrait();
            }
        });
    }

    /**
     * 是否打开音频检测
     *
     * @param open true：打开，false：关闭
     */
    public void setAudioActiveCheck(final boolean open) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (ARRtmpcEngine.Inst().getGuestOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                    nativeSetLiveToAudioOnly(true, open);
                } else {
                    nativeSetLiveToAudioOnly(false, open);
                }
            }
        });
    }

    /**
     * 打开或关闭前置摄像头镜面
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetCameraMirror(bEnable);
            }
        });
    }

    /**
     * 销毁游客端
     */
    public void clean() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                    }
                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
                nativeStopRtmpPlay();
                nativeDestroy();
                LooperExecutor.exchange(result, true);
            }
        });
        LooperExecutor.exchange(result, false);
    }

    private ARRtmpcGuestHelper guestHelper = new ARRtmpcGuestHelper() {
        @Override
        public void OnRtmplayerOK() {
            if (guestEvent!=null){
                guestEvent.onRtmpPlayerOk();
            }
        }

        @Override
        public void OnRtmplayerStart() {
            if (guestEvent!=null){
                guestEvent.onRtmpPlayerStart();
            }
        }

        @Override
        public void OnRtmplayerStatus(int nCacheTime, int nBitrate) {
            if (guestEvent!=null){
                guestEvent.onRtmpPlayerStatus(nCacheTime,nBitrate);
            }
        }

        @Override
        public void OnRtmplayerLoading(int nPercent) {
            if (guestEvent!=null){
                guestEvent.onRtmpPlayerLoading(nPercent);
            }
        }

        @Override
        public void OnRtmplayerClosed(int nCode) {
            if (guestEvent!=null){
                guestEvent.onRtmpPlayerClosed(nCode);
            }
        }

        @Override
        public void OnRtmpAudioLevel(String strLivePeerId, String userId, int level) {
        }

        @Override
        public void OnRTCJoinLineResult(int nCode, String strReason) {
            if (guestEvent!=null){
                guestEvent.onRTCJoinLineResult(nCode,strReason);
            }
        }

        @Override
        public void OnRTCApplyLineResult(int nCode) {
            if (guestEvent!=null){
                guestEvent.onRTCApplyLineResult(nCode);
            }
        }

        @Override
        public void OnRTCHangupLine() {
            if (guestEvent!=null){
                guestEvent.onRTCHangupLine();
            }
        }

        @Override
        public void OnRTCLineLeave(int nCode, String strReason) {
            if (guestEvent!=null){
                guestEvent.onRTCLineLeave(nCode,strReason);
            }
        }

        @Override
        public void OnRTCOpenVideoRender(String strLivePeerId, String strPublishId, String userId, String userData) {
            if (guestEvent!=null){
                guestEvent.onRTCOpenRemoteVideoRender(strLivePeerId,strPublishId,userId,userData);
            }
        }

        @Override
        public void OnRTCCloseVideoRender(String strLivePeerId, String strPublishId, String userId) {
            if (guestEvent!=null){
                guestEvent.onRTCCloseRemoteVideoRender(strLivePeerId,strPublishId,userId);
            }
        }

        @Override
        public void OnRTCOpenAudioLine(String strLivePeerId, String userId, String userData) {
            if (guestEvent!=null){
                guestEvent.onRTCOpenRemoteAudioLine(strLivePeerId,userId,userData);
            }
        }

        @Override
        public void OnRTCCloseAudioLine(String strLivePeerId, String userId) {
            if (guestEvent!=null){
                guestEvent.onRTCCloseRemoteAudioLine(strLivePeerId,userId);
            }
        }

        @Override
        public void OnRTCAVStatus(String strLivePeerId, boolean audio, boolean video) {
            if (guestEvent!=null){
                guestEvent.onRTCRemoteAVStatus(strLivePeerId,audio,video);
            }
        }

        @Override
        public void OnRTCAudioActive(String strLivePeerId, String userId, int nTime) {
            if (guestEvent!=null){
                if (strLivePeerId.equals("RTMPC_Hoster")){
                    guestEvent.onRTCLocalAudioActive(nTime);
                }else if (strLivePeerId.equals("RTMPC_Line_Hoster")){
                    guestEvent.onRTCHosterAudioActive(nTime);
                }else {
                    guestEvent.onRTCRemoteAudioActive(strLivePeerId, userId, nTime);
                }
            }
        }

        @Override
        public void OnRTCLiveStart() {

        }

        @Override
        public void OnRTCLiveStop() {

        }

        @Override
        public void OnRTCUserMessage(String userId, String strUserName, String strUserHeaderUrl, String strMessage) {
            if (guestEvent!=null){
                guestEvent.onRTCUserMessage(0,userId,strUserName,strUserHeaderUrl,strMessage);
            }
        }

        @Override
        public void OnRTCUserBarrage(String userId, String strUserName, String strUserHeaderUrl, String strBarrage) {
            if (guestEvent!=null){
                guestEvent.onRTCUserMessage(1,userId,strUserName,strUserHeaderUrl,strBarrage);
            }
        }

        @Override
        public void OnRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember) {
            if (guestEvent!=null){
                guestEvent.onRTCMemberNotify(strServerId,strRoomId,nTotalMember);
            }
        }

        @Override
        public void OnRTCUserShareOpen(int nType, String strUSInfo, String userId, String userData) {

        }

        @Override
        public void OnRTCUserShareClose() {

        }
    };

    /**
     * Native function
     */
    private native long nativeCreate(Object obj);

    private native void nativeSetUserToken(String strUserToken);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private native void nativeSetAudioEnable(boolean bEnable);

    private native void nativeSetVideoEnable(boolean bEnable);

    private native void nativeSetVideoCapturer(VideoCapturer capturer, long renderer);

    private native void nativeStartRtmpPlay(final String pullUrl, final long render);

    private native void nativeStopRtmpPlay();

    private native void nativeJoinRTCLine(String anyRTCId, String userId, String userData);

    private native void nativeApplyRTCLine(String userData);

    private native void nativeHangupRTCLine();

    private native void nativeSetRTCVideoRender(String strLivePeerId, final long render);

    private native boolean nativeSendUserMsg(String strUserName, String strUserHeaderUrl, String strContent);

    private native boolean nativeSendBarrage(String strUserName, String strUserHeaderUrl, String strContent);

    private native void nativeUpdateExp(int nExp);

    private native void nativeLeaveRTCLine();

    private native void nativeDestroy();

    private native void nativeSetScreenToLandscape();

    private native void nativeSetScreenToPortrait();

    private native void nativeSetLiveToAudioOnly(boolean bEnable, boolean bAudioDetect);

    private native void nativeSetCameraMirror(boolean bEnable);
}

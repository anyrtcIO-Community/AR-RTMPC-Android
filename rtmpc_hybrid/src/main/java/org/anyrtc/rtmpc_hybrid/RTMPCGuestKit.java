package org.anyrtc.rtmpc_hybrid;

import android.content.pm.PackageManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.anyrtc.common.enums.AnyRTCScreenOrientation;
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
 * Created by Eric on 2016/7/28.
 */
@Deprecated
public class RTMPCGuestKit {
    private static final String TAG = "RTMPCGuestKit";
    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;
    private String mUserData;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;

    private boolean bFront = true;
    private boolean bAudio = false;
    private AnyRTCScreenOrientation anyRTCScreenOrientation = AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait;

    /**
     * 实例化游客对象
     *
     * @param guestListener RTMPCGuestHelper 回调接口实现类
     */
    public RTMPCGuestKit(final RTMPCGuestHelper guestListener, RTMPCGuestVideoOption option) {
        AnyRTCUtils.assertIsTrue(guestListener != null);

        if (null != option) {
            bFront = option.ismBFront();
            bAudio = option.ismBAudio();
            anyRTCScreenOrientation = option.getmScreenOriention();
        }

        //如果使用音频连麦
        if (bAudio) {
            RTMPCHybrid.Inst().setAudioModel(true, true);
        } else {
            RTMPCHybrid.Inst().setAudioModel(false, true);
        }

        mExecutor = RTMPCHybrid.Inst().executor();
        mEglBase = RTMPCHybrid.Inst().egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(guestListener);
            }
        });
    }

    /**
     * 设置验证token
     *
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    public boolean setUserToken(final String strUserToken) {
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
     * 销毁游客端
     */
    public void clear() {
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

    /**
     * 加载本地摄像头
     *
     * @param lRender 底层图像地址
     * @return 打开本地预览返回值：0/1/2：没有相机权限/打开成功/打开相机失败
     */
    public int setLocalVideoCapturer(final long lRender) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ActivityCompat.checkSelfPermission(RTMPCHybrid.Inst().getContext(), CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    if (mVideoCapturer == null) {
                        mCameraId = 0;
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                        String frontCameraDeviceName =
                                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                        if (numberOfCameras > 1 && frontCameraDeviceName != null && bFront) {
                            cameraDeviceName = frontCameraDeviceName;
                            mCameraId = 1;
                        }
                        Log.d(TAG, "Opening camera: " + cameraDeviceName);
                        mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                        if (mVideoCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 2);
                        }
                    } else {
                        LooperExecutor.exchange(result, 0);
                    }
                    ret = 1;
                } else {
                    ret = 0;
                }

                /**
                 * 设置横竖屏
                 */
                if (anyRTCScreenOrientation == AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait) {
                    RTMPCHybrid.Inst().setThreadScreenToPortrait();
                } else {
                    RTMPCHybrid.Inst().setThreadScreenToLandscape();
                }

                /**
                 * 设置相机预览
                 */
                nativeSetVideoCapturer(mVideoCapturer, lRender);
                LooperExecutor.exchange(result, ret);
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
     * 开始播放rtmp流
     *
     * @param strUrl  rtmp 流地址
     * @param lRender 视频显示对象
     */
    public void startRtmpPlay(final String strUrl, final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeStartRtmpPlay(strUrl, lRender);
                RTMPCHybrid.Inst().checkSdk(10001015);
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
     * 游客加入RTC连接
     *
     * @param strAnyRTCId 主播对应的anyRTCid
     * @param strUserId   游客业务平台的用户id，可选。（若不设置， sendUserMsg和sendBarrage不能使用）
     * @param strUserData 游客业务平台自定义数据（json格式）, 最大值512字节
     * @return 返回结果。0：调用成功；4：参数非法；
     */
    public int joinRTCLine(final String strAnyRTCId, final String strUserId, final String strUserData) {
        if (strUserData.getBytes().length > 512) {
            return 4;
        }
        mUserData = strUserData;
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetDeviceInfo(RTMPCHybrid.Inst().getDeviceInfo());
                nativeJoinRTCLine(strAnyRTCId, strUserId, strUserData);
                RTMPCHybrid.Inst().checkSdk(10001017);
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
                int permission = ActivityCompat.checkSelfPermission(RTMPCHybrid.Inst().getContext(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    nativeApplyRTCLine(mUserData == null ? "" : mUserData);
                    RTMPCHybrid.Inst().checkSdk(10001016);
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
     * @param strLivePeerId 连麦者标识id（用于标识连麦用户，每次连麦随机生成）
     * @param lRender       视频显示对象
     */
    public void setRTCVideoRender(final String strLivePeerId, final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRender(strLivePeerId, lRender);
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
    public int sendUserMessage(final int nType, final String strUserName, final String strUserHeaderUrl,
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
     * 发送弹幕信息
     *
     * @param strCustomName      消息发送者的业务平台昵称（最大256个字节）
     * @param strCustomHeaderUrl 消息发送者的业务平台的头像url（最大256个字节）
     * @param strContent         消息内容（最大256个字节）
     * @return 如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    private boolean sendUserBarrage(final String strCustomName, final String strCustomHeaderUrl, final String strContent) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String headUrl = strCustomHeaderUrl;
                if (headUrl.length() == 0) {
                    headUrl = "strCustomHeaderUrl can't be empty string";
                }
                boolean ret = nativeSendBarrage(strCustomName, headUrl, strContent);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
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
     * Native function
     */
    private native long nativeCreate(Object obj);

    private native void nativeSetUserToken(String strUserToken);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private native void nativeSetAudioEnable(boolean bEnable);

    private native void nativeSetVideoEnable(boolean bEnable);

    private native void nativeSetVideoCapturer(VideoCapturer capturer, long lRenderer);

    private native void nativeStartRtmpPlay(final String strUrl, final long lRender);

    private native void nativeStopRtmpPlay();

    private native void nativeJoinRTCLine(String strAnyrtcId, String strUserId, String strUserData);

    private native void nativeApplyRTCLine(String strUserData);

    private native void nativeHangupRTCLine();

    private native void nativeSetRTCVideoRender(String strLivePeerId, final long lRender);

    private native boolean nativeSendUserMsg(String strUserName, String strUserHeaderUrl, String strContent);

    private native boolean nativeSendBarrage(String strUserName, String strUserHeaderUrl, String strContent);

    private native void nativeUpdateExp(int nExp);

    private native void nativeLeaveRTCLine();

    private native void nativeDestroy();
}

package org.ar.rtmpc_hybrid;

import android.content.pm.PackageManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import org.ar.common.enums.ARVideoCommon;
import org.anyrtc.common.enums.AnyRTCRTMPCCtrlMxvType;
import org.anyrtc.common.utils.AnyRTCUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

import java.io.File;
import java.util.concurrent.Exchanger;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by liuxiaozhong on 2019/1/16.
 */
public class ARRtmpcHosterKit {
    private static final String TAG = "ARRtmpcHosterKit";

    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;
    private ARRtmpcHosterEvent hosterEvent;

    /**
     * 实例化主播对象
     *
     */
    public ARRtmpcHosterKit(final ARRtmpcHosterEvent hosterEvent) {
        AnyRTCUtils.assertIsTrue(hosterEvent != null);
        this.hosterEvent = hosterEvent;
        mExecutor = ARRtmpcEngine.Inst().executor();
        mEglBase = ARRtmpcEngine.Inst().egl();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(hosterHelper);
                if (ARRtmpcEngine.Inst().getHosterOption().getVideoOrientation() == ARVideoCommon.ARVideoOrientation.Portrait) {
                    nativeSetScreenToPortrait();
                } else {
                    nativeSetScreenToLandscape();
                }
                if (ARRtmpcEngine.Inst().getHosterOption().getMediaType() == ARVideoCommon.ARMediaType.Video) {
                    nativeSetLiveToAudioOnly(false, true);
                } else {
                    nativeSetLiveToAudioOnly(true, true);
                }
                nativeSetVideoProfileMode(ARRtmpcEngine.Inst().getHosterOption().getVideoProfile().level);
                nativeSetVideoFpsProfile(ARRtmpcEngine.Inst().getHosterOption().getVideoFps().level);
                nativeSetVideoFullScreen(ARRtmpcEngine.Inst().getHosterOption().getLineLayoutTemplate().type);
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
                int permission = PermissionChecker.checkSelfPermission(ARRtmpcEngine.Inst().getContext(), CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    if (mVideoCapturer == null) {
                        mCameraId = 0;
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                        String frontCameraDeviceName =
                                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                        if (numberOfCameras > 1 && frontCameraDeviceName != null && ARRtmpcEngine.Inst().getHosterOption().isDefaultFrontCamera()) {
                            cameraDeviceName = frontCameraDeviceName;
                            mCameraId = 1;
                        }
                        Log.d(TAG, "Opening camera: " + cameraDeviceName);
                        mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                        if (mVideoCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 2);
                        }
                        nativeSetVideoCapturer(mVideoCapturer, lRender);
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
     * 销毁主播端
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
                        e.printStackTrace();
                    }

                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
                nativeSetScreenCtrlEnable(false);
                nativeDestroy();
                LooperExecutor.exchange(result, true);
            }
        });
        LooperExecutor.exchange(result, false);
    }


    /**
     * 设置合成流连麦视频窗口位置
     *
     * @param eHor        横向位置：
     *                    AnyRTCRTMPCVideoTempHor.RTMPC_V_T_HOR_LEFT 横向居左
     *                    AnyRTCRTMPCVideoTempHor.RTMPC_V_T_HOR_CENTER 横向居中
     *                    AnyRTCRTMPCVideoTempHor.RTMPC_V_T_HOR_RIGHT 横向居右
     * @param eVer        竖向位置：
     *                    AnyRTCRTMPCVideoTempVer.RTMPC_V_T_VER_TOP：竖向顶部
     *                    AnyRTCRTMPCVideoTempVer.RTMPC_V_T_VER_CENTER：竖向居中
     *                    AnyRTCRTMPCVideoTempVer.RTMPC_V_T_VER_BOTTOM：竖向底部
     * @param eDir        排布方向：
     *                    AnyRTCRTMPCVideoTempDir.RTMPC_V_T_DIR_HOR: 横向排布
     *                    AnyRTCRTMPCVideoTempDir.RTMPC_V_T_DIR_VER：竖向排布
     * @param ePadhor     横向的间距（左右间距：最左边或者最后边的视频离边框的距离）
     * @param ePadver     竖向的间距（上下间距：最上面或者最下面离边框的距离）
     * @param nWLineWidth 合成小视频白边宽度（上下间距：最上面或者最下面离边框的距离）
     */
    public void setVideoTemplate(final ARRtmpcVideoHorizontal eHor, final ARRtmpcVideoVertical eVer,
                                 final ARRtmpcVideoDirection eDir, final int ePadhor, final int ePadver, final int nWLineWidth) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoTemplate(eHor.gravity, eVer.gravity, eDir.direction, ePadhor, ePadver, nWLineWidth);
            }
        });
    }

    /**
     * 设置合成视频显示模板
     *
     * @param layoutTemplate 布局样式; RTMPC_LINE_V_Fullscrn	// 主全屏,三小副
     *                       RTMPC_LINE_V_1_equal_others	// 主和福大小相同
     *                       RTMPC_LINE_V_1big_3small	// 主大(不是全屏),三小副
     */
    public void setMixVideoModel(final ARRtmpcLineLayoutTemplate layoutTemplate) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoFullScreen(layoutTemplate.type);
            }
        });
    }

    /**
     * 设置录像地址（地址为拉流地址）
     * 说明：设置Rtmp录制地址，需放在开始推流方法前.并且必须在平台上开启录像服务
     *
     * @param url 拉流地址
     */
    public void setRtmpRecordUrl(final String url) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRtmpRecordUrl(url);
            }
        });
    }

    /**
     * 设置视频的默认背景图片
     * 说明：仅支持jpg和png的图片格式（仅支持640*640分辨率以内）
     *
     * @param filePath 图片的路径
     * @return 0/1/2:没有读取文件权限/打开设置成功/文件不存在
     */
    public int setVideoSubBackground(final String filePath) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;

                int permission = PermissionChecker.checkSelfPermission(ARRtmpcEngine.Inst().getContext(), WRITE_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    File file = new File(filePath);
                    if (file.exists()) {
                        nativeSetVideoSubBackground(filePath);
                        ret = 1;
                    } else {
                        ret = 2;
                    }
                } else {
                    ret = 0;
                }
                LooperExecutor.exchange(result, ret);
            }
        });

        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 打开或关闭本地音频
     *
     * @param enabled true: 打开; false: 关闭
     */
    public void setLocalAudioEnable(final boolean enabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioEnable(enabled);
            }
        });
    }

    /**
     * 打开或关闭本地视频
     *
     * @param enabled true: 打开; false: 关闭
     */
    public void setLocalVideoEnable(final boolean enabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoEnable(enabled);
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
     * 设置相机焦距
     *
     * @param distance 相机支持范围内的焦距
     */
    public void setCameraZoom(final int distance) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVideoCapturer.setZoom(distance);
            }
        });
    }

    /**
     * 获取相机的最大焦距
     *
     * @return
     */
    public int getCameraMaxZoom() {
        return mVideoCapturer.getMaxZoom();
    }

    /**
     * 获取相机的当前焦距
     *
     * @return
     */
    public int getCameraZoom() {
        return mVideoCapturer.getCameraZoom();
    }

    /**
     * 是否支持平滑变焦
     *
     * @return
     */
    public boolean isSmoothZoomSupported() {
        return mVideoCapturer.isSmoothZoomSupported();
    }

    /**
     * 是否支持变焦
     *
     * @return
     */
    public boolean isZoomSupported() {
        return mVideoCapturer.isZoomSupported();
    }

    private void SetBeautyEnable(boolean enabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    /**
     * 打开关闭摄像头闪光灯 true 打开  false关闭
     *
     * @param open
     */
    public void openCameraTorchMode(final boolean open) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVideoCapturer.openCameraTorchMode(open);
            }
        });
    }

    private String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public int setVideoLogo(final String path, final int x, final int y) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(ARRtmpcEngine.Inst().getContext(), READ_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    File file = new File(path);
                    if (file.exists()) {
                        if (getExtensionName(file.getName()).equalsIgnoreCase("jpg")) {
                            nativeSetVideoLogo(path, x, y);
                            ret = 1;
                        } else {
                            ret = 0;
                        }
                    } else {
                        ret = 0;
                    }

                } else {
                    ret = 0;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 设置右侧logo水印仅支持jpg图片
     *
     * @param path
     * @param x
     * @param y
     */
    public int setVideoTopRightLogo(final String path, final int x, final int y) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(ARRtmpcEngine.Inst().getContext(), READ_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    File file = new File(path);
                    if (file.exists()) {
                        if (getExtensionName(file.getName()).equalsIgnoreCase("jpg")) {
                            nativeSetVideoTopRightLogo(path, x, y);
                            ret = 1;
                        } else {
                            ret = 0;
                        }
                    } else {
                        ret = 0;
                    }

                } else {
                    ret = 0;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    //***************************************Rtmp function for push rtmp stream*******************************

    /**
     * 开始推流
     *
     * @param pushUrl 推流地址
     * @return 推流结果：0/1：失败(没有录音权限）/成功
     */
    public int startPushRtmpStream(final String pushUrl) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(ARRtmpcEngine.Inst().getContext(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    nativeStartPushRtmpStream(pushUrl);
                    ARRtmpcEngine.Inst().checkSdk(10001001);
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
     * 停止推流
     */
    public void stopRtmpStream() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeStopRtmpStream();
            }
        });
    }

    //****************************************RTC function for line*******************************************

    /**
     * 建立RTC连接
     *
     * @param anyrtcId
     * @param userId
     * @param userData
     * @return
     */
    public boolean createRTCLine(final String token,final String anyrtcId, final String userId, final String userData, final String liveInfo) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (null!=token&&!token.equals("")) {
                    nativeSetUserToken(token);
                }
                nativeSetDeviceInfo(ARRtmpcEngine.Inst().getDeviceInfo());
                boolean ret = nativeOpenRTCLine(anyrtcId, userId, userData, liveInfo);
                ARRtmpcEngine.Inst().checkSdk(10001003);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 同意游客连麦请求
     * 说明：调用此方法即可同意游客的连麦请求，然后将会回调显示连麦视频方法，具体操作
     * 可查看接口OnRTCApplyToLine回调-连麦视频显示说明
     *
     * @param peerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void acceptRTCLine(final String peerId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeAcceptRTCLine(peerId);
                ARRtmpcEngine.Inst().checkSdk(10001004);
            }
        });
    }

    /**
     * 挂断游客连麦
     * 说明：与游客连麦过程中，可调用此方法挂断与他的连麦
     *
     * @param peerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void hangupRTCLine(final String peerId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeHangupRTCLine(peerId);
                ARRtmpcEngine.Inst().checkSdk(10001005);
            }
        });
    }

    /**
     * 拒绝游客连麦请求
     * 说明：当有游客请求连麦时，可调用此方法拒绝
     *
     * @param peerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void rejectRTCLine(final String peerId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeRejectRTCLine(peerId, false);
                ARRtmpcEngine.Inst().checkSdk(10001006);
            }
        });
    }

    /**
     * 允许链接WiFi摄像机
     */
    private void enablePeerScreen() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeEnablePeerScreen();
            }
        });
    }

    /**
     * 连接WiFi摄像机视频
     *
     * @param strPeerScrnId 视频的id
     * @return
     */
    private boolean connectPeerScreen(final String strPeerScrnId) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeConnectPeerScreen(strPeerScrnId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 开启导播功能
     *
     * @param enable 导播功能是否打开；true/false:打开/关闭
     */
    private void setScreenCtrlEnable(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenCtrlEnable(enable);
            }
        });
    }

    /**
     * 设置视频上屏
     *
     * @param strVid  视频id(参考导播台)
     * @param mxvType AnyRTCRTMPCCtrlMxvType 视频显示位置（RTMPC_MXV_NULL：默认（右侧），RTMPC_MXV_MAIN：主屏， RTMPC_MXV_B_LEFT：左侧（暂时无效），RTMPC_MXV_B_RIGHT：右侧）
     * @return
     */
    private boolean addVideoCapturerToScreen(final String strVid, final AnyRTCRTMPCCtrlMxvType mxvType) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeAddVideoCapturerToScreen(strVid, mxvType.level);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 从屏幕去除视频
     *
     * @param strVid 视频id(参考导播台)
     * @return
     */
    private boolean removeVideoCapturerToScreen(final String strVid) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeRemoveVideoCapturerToScreen(strVid);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 导播切换视频图像位置
     *
     * @param strVid_main 主屏幕的视频id
     * @param strVid_sub  小屏幕的视频id
     * @return
     */
    private boolean switchVideoCapturerInScreen(final String strVid_main, final String strVid_sub) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeSwitchVideoCapturerInScreen(strVid_main, strVid_sub);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 设置连麦者视频窗口
     * 该方法用于游客申请连麦接通后，游客连麦图像打开回调中（OnRTCOpenVideoRender）使用
     *  
     * 示例：VideoRenderer render = mVideoView.OnRtcOpenRemoteRender("strRtcPeerId", RendererCommon.ScalingType.SCALE_ASPECT_FIT);
     *       mHosterKit.SetRTCVideoRender(strRtcPeerId,render.GetRenderPointer());
     *
     * @param publishId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     * @param lRender   SDK底层视频显示对象  （通过VideoRenderer对象获取，参考连麦窗口管理对象-添加连麦窗口渲染器方法）
     */
    public void setRTCRemoteVideoRender(final String publishId, final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRender(publishId, lRender);
            }
        });
    }

    /**
     * 发送消息、弹幕等文本信息
     *
     * @param type      消息类型:0:普通消息;1:弹幕消息
     * @param userName  消息发送者的业务平台昵称（最大256个字节）
     * @param headerUrl 消息发送者的业务平台的头像url（最大512个字节）
     * @param content   消息内容（最大256个字节）
     * @return 返回结果，0：成功；1：失败；4：参数非法；如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    public int sendMessage(final int type, final String userName, final String headerUrl,
                           final String content) {
        if (userName.getBytes().length > 384) {
            return 4;
        }
        if (headerUrl.getBytes().length > 1536) {
            return 4;
        }
        if (content.getBytes().length > 1536) {
            return 4;
        }

        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String headUrl = headerUrl;
                if (headUrl.length() == 0) {
                    headUrl = "strCustomHeaderUrl can't be empty string";
                }
                boolean ret = false;
                if (type == 0) {
                    ret = nativeSendUserMsg(userName, headUrl, content);
                } else if (type == 1) {
                    ret = nativeSendBarrage(userName, headUrl, content);
                }
                LooperExecutor.exchange(result, ret ? 0 : 1);
            }
        });
        return LooperExecutor.exchange(result, 1);
    }


    /**
     * 关闭RTC
     * 说明:一般不调用，clear时已清除
     */
    public void closeRTCLine() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeCloseRTCLine();
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
                if (ARRtmpcEngine.Inst().getHosterOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
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
     * @param enable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetCameraMirror(enable);
            }
        });
    }

    private ARRtmpcHosterHelper hosterHelper = new ARRtmpcHosterHelper() {
        @Override
        public void OnRtmpStreamOK() {
            if (hosterEvent != null) {
                hosterEvent.onRtmpStreamOk();
            }
        }

        @Override
        public void OnRtmpStreamReconnecting(int nTimes) {
            if (hosterEvent != null) {
                hosterEvent.onRtmpStreamReconnecting(nTimes);
            }
        }

        @Override
        public void OnRtmpStreamStatus(int nDelayTime, int nBirate) {
            if (hosterEvent != null) {
                hosterEvent.onRtmpStreamStatus(nDelayTime, nBirate);
            }
        }

        @Override
        public void OnRtmpStreamFailed(int nCode) {
            if (hosterEvent != null) {
                hosterEvent.onRtmpStreamFailed(nCode);
            }
        }

        @Override
        public void OnRtmpStreamClosed() {
            if (hosterEvent != null) {
                hosterEvent.onRtmpStreamClosed();
            }
        }

        @Override
        public void OnRtmpAudioLevel(String peerId, String userId, int nLevel) {
        }

        @Override
        public void OnRTCOpenLineResult(int nCode, String strReason) {
            if (hosterEvent != null) {
                hosterEvent.onRTCCreateLineResult(nCode, strReason);
            }
        }

        @Override
        public void OnRTCApplyToLine(String peerId, String userId, String userData) {
            if (hosterEvent != null) {
                hosterEvent.onRTCApplyToLine(peerId, userId, userData);
            }
        }

        @Override
        public void OnRTCCancelLine(int nCode, String peerId) {
            if (hosterEvent != null) {
                hosterEvent.onRTCCancelLine(nCode, peerId);
            }
        }

        @Override
        public void OnRTCLineClosed(int nCode, String strReason) {
            if (hosterEvent != null) {
                hosterEvent.onRTCLineClosed(nCode, strReason);
            }
        }

        @Override
        public void OnRTCOpenVideoRender(String peerId, String strPublishId, String userId, String userData) {
            if (hosterEvent != null) {
                hosterEvent.onRTCOpenRemoteVideoRender(peerId, strPublishId, userId, userData);
            }
        }

        @Override
        public void OnRTCCloseVideoRender(String peerId, String strPublishId, String userId) {
            if (hosterEvent != null) {
                hosterEvent.onRTCCloseRemoteVideoRender(peerId, strPublishId, userId);
            }
        }

        @Override
        public void OnRTCOpenAudioLine(String peerId, String userId, String userData) {
            if (hosterEvent != null) {
                hosterEvent.onRTCOpenRemoteAudioLine(peerId, userId, userData);
            }
        }

        @Override
        public void OnRTCCloseAudioLine(String peerId, String userId) {
            if (hosterEvent != null) {
                hosterEvent.onRTCCloseRemoteAudioLine(peerId, userId);
            }
        }

        @Override
        public void OnRTCAVStatus(String peerId, boolean audio, boolean video) {
            if (hosterEvent != null) {
                hosterEvent.onRTCRemoteAVStatus(peerId, audio, video);
            }
        }

        @Override
        public void OnRTCAudioActive(String peerId, String userId, int nTime) {
            if (hosterEvent != null) {
                if (peerId.equals("RTMPC_Hoster")) {
                    hosterEvent.onRTLocalAudioActive(nTime);
                } else {
                    hosterEvent.onRTCRemoteAudioActive(peerId, userId, nTime);
                }
            }
        }

        @Override
        public void OnRTCUserMessage(String userId, String userName, String headerUrl, String strMessage) {
            if (hosterEvent != null) {
                hosterEvent.onRTCUserMessage(0, userId, userName, headerUrl, strMessage);
            }
        }

        @Override
        public void OnRTCUserBarrage(String userId, String userName, String headerUrl, String strBarrage) {
            if (hosterEvent != null) {
                hosterEvent.onRTCUserMessage(1, userId, userName, headerUrl, strBarrage);
            }
        }

        @Override
        public void OnRTCMemberNotify(String strServerId, String strRoomId, int nTotalMember) {
            if (hosterEvent != null) {
                hosterEvent.onRTCMemberNotify(strServerId, strRoomId, nTotalMember);
            }
        }

        @Override
        public void OnRTCLanScreenFound(String strPeerScrnId, String strName, String strPlatform) {
        }

        @Override
        public void OnRTCLanScreenClosed(String strPeerScrnId) {
        }
    };


    /**
     * Native function
     */
    private native long nativeCreate(Object obj);

    private native void nativeSetUserToken(String strUserToken);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private native void nativeSetAudioEnable(boolean bEnable);

    private native void nativeSetVideoEnable(boolean enable);

    private native void nativeSetNetAdjustMode(int nMode);

    private native void nativeSetVideoCapturer(VideoCapturer capturer, long lRenderer);

    private native void nativeSetBeautyEnable(boolean enable);

    private native void nativeSetVideoLogo(String path, int nOff_x, int nOff_y);

    private native void nativeSetVideoTopRightLogo(String path, int nOff_x, int nOff_y);

    private native void nativeSetVideoMode(int nVideoMode);

    private native void nativeSetVideoModeExcessive(int nVideoMode);

    private native void nativeSetVideoProfileMode(int nVideoMode);

    private native void nativeSetVideoFpsProfile(int nFpsMode);

    private native void nativeSetVideoTemplate(int nHor, int nVer, int nDir, int nPadhor, int nPadver, int nWLineWidth);

    private native void nativeSetVideoFullScreen(int nLayoutModel);

    private native void nativeStartPushRtmpStream(String url);

    private native void nativeSetRtmpRecordUrl(String url);

    private native void nativeSetVideoSubBackground(String path);

    private native void nativeStopRtmpStream();

    private native boolean nativeOpenRTCLine(String anyrtcId, String strCustomId, String userData, String liveInfo);

    private native void nativeAcceptRTCLine(String peerId);

    private native void nativeHangupRTCLine(String peerId);

    private native void nativeRejectRTCLine(String peerId, boolean bBanToApply);

    private native void nativeEnablePeerScreen();

    private native boolean nativeConnectPeerScreen(String strPeerScrnId);

    private native void nativeSetScreenCtrlEnable(boolean enable);

    private native boolean nativeAddVideoCapturerToScreen(String strVid, int type);

    private native boolean nativeRemoveVideoCapturerToScreen(String strVid);

    private native boolean nativeSwitchVideoCapturerInScreen(String strVid_main, String strVid_sub);

    private native void nativeSetRTCVideoRender(String strRtcPubId, long nativeRenderer);

    private native boolean nativeSendUserMsg(String userName, String headerUrl, String content);

    private native boolean nativeSendBarrage(String userName, String headerUrl, String content);

    private native void nativeCloseRTCLine();

    private native void nativeDestroy();

    private native void nativeSetScreenToLandscape();

    private native void nativeSetScreenToPortrait();

    private native void nativeSetLiveToAudioOnly(boolean bEnable, boolean bAudioDetect);

    private native void nativeSetCameraMirror(boolean enable);
}

package org.anyrtc.rtmpc_hybrid;

import android.content.pm.PackageManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.anyrtc.common.enums.AnyRTCRTMPCCtrlMxvType;
import org.anyrtc.common.enums.AnyRTCRTMPCLineVideoLayout;
import org.anyrtc.common.enums.AnyRTCRTMPCVideoMode;
import org.anyrtc.common.enums.AnyRTCRTMPCVideoTempDir;
import org.anyrtc.common.enums.AnyRTCRTMPCVideoTempHor;
import org.anyrtc.common.enums.AnyRTCRTMPCVideoTempVer;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;
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
 * Created by Eric on 2016/7/25.
 */
@Deprecated
public class RTMPCHosterKit {
    private static final String TAG = "RTMPCHosterKit";

    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;

    private boolean bFront = true;
    private AnyRTCVideoQualityMode rtmpcVideoMode = AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium1;
    private AnyRTCScreenOrientation anyRTCScreenOrientation = AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait;
    private AnyRTCRTMPCLineVideoLayout mLineVideoLayout = AnyRTCRTMPCLineVideoLayout.RTMPC_LINE_V_1big_3small;

    /**
     * 实例化主播对象
     *
     * @param hosterListener RTMPCHosterHelper 回调接口实现类
     * @param option         视频直播配置类
     */
    public RTMPCHosterKit(final RTMPCHosterHelper hosterListener, RTMPCHosterVideoOption option) {
        AnyRTCUtils.assertIsTrue(hosterListener != null);

        if (null != option) {
            bFront = option.ismBFront();
            rtmpcVideoMode = option.getmVideoMode();
            anyRTCScreenOrientation = option.getmScreenOriention();
            mLineVideoLayout = option.getmLineVideoLayout();
        }
        mExecutor = RTMPCHybrid.Inst().executor();
        mEglBase = RTMPCHybrid.Inst().egl();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(hosterListener);
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
     * 销毁主播端
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

                RTMPCHybrid.Inst().setAudioModel(false, true);

                /**
                 * 设置视频分辨率
                 */
                nativeSetVideoModeExcessive(rtmpcVideoMode.level);
                /**
                 * 设置连麦窗口的样式
                 */
                nativeSetVideoFullScreen(mLineVideoLayout.level);
                /**
                 * 设置本地录像
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
     * 设置推流视频质量
     *
     * @param videoMode RTMPC_Video_Low（640x480/384）,
     *                  RTMPC_Video_SD（640x480/512）,
     *                  RTMPC_Video_QHD（640x480/768）,
     *                  RTMPC_Video_HD（960x540/1024）,
     *                  RTMPC_Video_720P（1280x720/1280）,
     *                  RTMPC_Video_1080P（1920x1280/2048）
     */
    private void setVideoMode(final AnyRTCRTMPCVideoMode videoMode) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoMode(videoMode.level);
            }
        });
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
    public void setVideoTemplate(final AnyRTCRTMPCVideoTempHor eHor, final AnyRTCRTMPCVideoTempVer eVer,
                                 final AnyRTCRTMPCVideoTempDir eDir, final int ePadhor, final int ePadver, final int nWLineWidth) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoTemplate(eHor.level, eVer.level, eDir.level, ePadhor, ePadver, nWLineWidth);
            }
        });
    }

    /**
     * 设置合成视频显示模板
     *
     * @param eLayoutModel 布局样式; RTMPC_LINE_V_Fullscrn	// 主全屏,三小副
     *                     RTMPC_LINE_V_1_equal_others	// 主和福大小相同
     *                     RTMPC_LINE_V_1big_3small	// 主大(不是全屏),三小副
     */
    public void setMixVideoModel(final AnyRTCRTMPCLineVideoLayout eLayoutModel) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoFullScreen(eLayoutModel.level);
            }
        });
    }

    /**
     * 设置录像地址（地址为拉流地址）
     * 说明：设置Rtmp录制地址，需放在开始推流方法前.并且必须在平台上开启录像服务
     *
     * @param strUrl 拉流地址
     */
    private void setRtmpRecordUrl(final String strUrl) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRtmpRecordUrl(strUrl);
            }
        });
    }

    /**
     * 设置视频的默认背景图片
     * 说明：仅支持jpg和png的图片格式（仅支持640*640分辨率以内）
     *
     * @param strPath 图片的路径
     * @return 0/1/2:没有读取文件权限/打开设置成功/文件不存在
     */
    public int setVideoSubBackground(final String strPath) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;

                int permission = ActivityCompat.checkSelfPermission(RTMPCHybrid.Inst().getContext(), WRITE_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    File file = new File(strPath);
                    if (file.exists()) {
                        nativeSetVideoSubBackground(strPath);
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
     * @param bEnabled true: 打开; false: 关闭
     */
    public void setLocalAudioEnable(final boolean bEnabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioEnable(bEnabled);
            }
        });
    }

    /**
     * 打开或关闭本地视频
     *
     * @param bEnabled true: 打开; false: 关闭
     */
    public void setLocalVideoEnable(final boolean bEnabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoEnable(bEnabled);
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
     * @param nDistance 相机支持范围内的焦距
     */
    public void setCameraZoom(final int nDistance) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVideoCapturer.setZoom(nDistance);
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
     * @param BOpen
     */
    public void openCameraTorchMode(final boolean BOpen) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVideoCapturer.openCameraTorchMode(BOpen);
            }
        });
    }

    public String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public int setVideoLogo(final String logoFilePath, final int off_x, final int off_y) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ActivityCompat.checkSelfPermission(RTMPCHybrid.Inst().getContext(), READ_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    File file = new File(logoFilePath);
                    if (file.exists()) {
                        if (getExtensionName(file.getName()).equalsIgnoreCase("jpg")) {
                            nativeSetVideoLogo(logoFilePath, off_x, off_y);
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
     * @param logoFilePath
     * @param off_x
     * @param off_y
     */
    public int setVideoTopRightLogo(final String logoFilePath, final int off_x, final int off_y) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ActivityCompat.checkSelfPermission(RTMPCHybrid.Inst().getContext(), READ_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    File file = new File(logoFilePath);
                    if (file.exists()) {
                        if (getExtensionName(file.getName()).equalsIgnoreCase("jpg")) {
                            nativeSetVideoTopRightLogo(logoFilePath, off_x, off_y);
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
     * @param strPushUrl 推流地址
     * @return 推流结果：0/1：失败(没有录音权限）/成功
     */
    public int startPushRtmpStream(final String strPushUrl) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ActivityCompat.checkSelfPermission(RTMPCHybrid.Inst().getContext(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We have permission granted to the user
                    nativeStartPushRtmpStream(strPushUrl);
                    RTMPCHybrid.Inst().checkSdk(10001001);
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
     * @param strAnyrtcId
     * @param strUserId
     * @param strUserData
     * @return
     */
    public boolean createRTCLine(final String strAnyrtcId, final String strUserId, final String strUserData, final String strLiveInfo) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetDeviceInfo(RTMPCHybrid.Inst().getDeviceInfo());
                boolean ret = nativeOpenRTCLine(strAnyrtcId, strUserId, strUserData, strLiveInfo);
                RTMPCHybrid.Inst().checkSdk(10001003);
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
     * @param strLivePeerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void acceptRTCLine(final String strLivePeerId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeAcceptRTCLine(strLivePeerId);
                RTMPCHybrid.Inst().checkSdk(10001004);
            }
        });
    }

    /**
     * 挂断游客连麦
     * 说明：与游客连麦过程中，可调用此方法挂断与他的连麦
     *
     * @param strLivePeerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void hangupRTCLine(final String strLivePeerId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeHangupRTCLine(strLivePeerId);
                RTMPCHybrid.Inst().checkSdk(10001005);
            }
        });
    }

    /**
     * 拒绝游客连麦请求
     * 说明：当有游客请求连麦时，可调用此方法拒绝
     *
     * @param strLivePeerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     */
    public void rejectRTCLine(final String strLivePeerId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeRejectRTCLine(strLivePeerId, false);
                RTMPCHybrid.Inst().checkSdk(10001006);
            }
        });
    }

    /**
     * 允许链接WiFi摄像机
     */
    public void enablePeerScreen() {
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
    public boolean connectPeerScreen(final String strPeerScrnId) {
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
    public void setScreenCtrlEnable(final boolean enable) {
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
    public boolean addVideoCapturerToScreen(final String strVid, final AnyRTCRTMPCCtrlMxvType mxvType) {
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
    public boolean removeVideoCapturerToScreen(final String strVid) {
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
    public boolean switchVideoCapturerInScreen(final String strVid_main, final String strVid_sub) {
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
     * @param strLivePeerId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     * @param lRender       SDK底层视频显示对象  （通过VideoRenderer对象获取，参考连麦窗口管理对象-添加连麦窗口渲染器方法）
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
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url（最大512个字节）
     * @param strContent       消息内容（最大256个字节）
     * @return 返回结果，0：成功；1：失败；4：参数非法；如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
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
     * @param strUserName      消息发送者的业务平台昵称（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url（最大1024个字节）
     * @param strContent       消息内容（最大1024个字节）
     * @return 如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    private boolean sendUserBarrage(final String strUserName, final String strUserHeaderUrl, final String strContent) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String headUrl = strUserHeaderUrl;
                if (headUrl.length() == 0) {
                    headUrl = "strCustomHeaderUrl can't be empty string";
                }
                boolean ret = nativeSendBarrage(strUserName, headUrl, strContent);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
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
     * Native function
     */
    private native long nativeCreate(Object obj);

    private native void nativeSetUserToken(String strUserToken);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private native void nativeSetAudioEnable(boolean bEnable);

    private native void nativeSetVideoEnable(boolean bEnable);

    private native void nativeSetNetAdjustMode(int nMode);

    private native void nativeSetVideoCapturer(VideoCapturer capturer, long lRenderer);

    private native void nativeSetBeautyEnable(boolean bEnable);

    private native void nativeSetVideoLogo(String logoFilePath, int nOff_x, int nOff_y);

    private native void nativeSetVideoTopRightLogo(String logoFilePath, int nOff_x, int nOff_y);

    private native void nativeSetVideoMode(int nVideoMode);

    private native void nativeSetVideoModeExcessive(int nVideoMode);

    private native void nativeSetVideoTemplate(int nHor, int nVer, int nDir, int nPadhor, int nPadver, int nWLineWidth);

    private native void nativeSetVideoFullScreen(int nLayoutModel);

    private native void nativeStartPushRtmpStream(String strUrl);

    private native void nativeSetRtmpRecordUrl(String strUrl);

    private native void nativeSetVideoSubBackground(String path);

    private native void nativeStopRtmpStream();

    private native boolean nativeOpenRTCLine(String strAnyrtcId, String strCustomId, String strUserData, String strLiveInfo);

    private native void nativeAcceptRTCLine(String strLivePeerId);

    private native void nativeHangupRTCLine(String strLivePeerId);

    private native void nativeRejectRTCLine(String strLivePeerId, boolean bBanToApply);

    private native void nativeEnablePeerScreen();

    private native boolean nativeConnectPeerScreen(String strPeerScrnId);

    private native void nativeSetScreenCtrlEnable(boolean enable);

    private native boolean nativeAddVideoCapturerToScreen(String strVid, int type);

    private native boolean nativeRemoveVideoCapturerToScreen(String strVid);

    private native boolean nativeSwitchVideoCapturerInScreen(String strVid_main, String strVid_sub);

    private native void nativeSetRTCVideoRender(String strRtcPubId, long nativeRenderer);

    private native boolean nativeSendUserMsg(String strUserName, String strUserHeaderUrl, String strContent);

    private native boolean nativeSendBarrage(String strUserName, String strUserHeaderUrl, String strContent);

    private native void nativeCloseRTCLine();

    private native void nativeDestroy();
}

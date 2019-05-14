package org.ar.hoster;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.ar.BaseActivity;
import org.ar.ARApplication;
import org.ar.adapter.LiveMessageAdapter;
import org.ar.adapter.LogAdapter;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.ar.rtmpc.R;
import org.ar.model.LineBean;
import org.ar.model.MessageBean;
import org.ar.utils.AnyRTCUtils;
import org.ar.utils.DisplayUtils;
import org.ar.utils.ToastUtil;
import org.ar.widgets.ARVideoView;
import org.ar.widgets.CustomDialog;
import org.ar.widgets.KeyboardDialogFragment;
import org.ar.common.enums.ARVideoCommon;
import org.ar.rtmpc_hybrid.ARRtmpcEngine;
import org.ar.rtmpc_hybrid.ARRtmpcHosterEvent;
import org.ar.rtmpc_hybrid.ARRtmpcHosterKit;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoRenderer;

import java.net.URLDecoder;



/**
 * 视频主播页面
 */
public class HosterActivity extends BaseActivity {
    RelativeLayout rlRtmpcVideos,rl_log_layout;
    TextView tvTitle,tvRtmpOk,tvRtmpStatus,tvRtcOk,tvMemberNum;
    RecyclerView rvMsgList,rvLog;
    View viewSpace;
    ImageButton tvLineList;
    private ARRtmpcHosterKit mHosterKit;
    private ARVideoView mVideoView;
    private AnyRTCAudioManager mRtmpAudioManager;
    private LiveMessageAdapter mAdapter;
    private LogAdapter logAdapter;
    private CustomDialog line_dialog;
    private LineFragment lineFragment;
    private boolean isShowLineList = false;
    private LineListener lineListener;

    private String pushURL = "";
    private String liveId= ARApplication.LIVE_ID;
    private String nickname= ARApplication.getNickName();
    private String userId="host"+(int)((Math.random()*9+1)*100000)+"";

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ShowExitDialog();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (mHosterKit != null) {
            mVideoView.removeLocalVideoRender();
            mHosterKit.clean();
        }
        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_hoster;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        //设置屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         rlRtmpcVideos=findViewById(R.id.rl_rtmpc_videos);
        rl_log_layout=findViewById(R.id.rl_log_layout);
        rvLog=findViewById(R.id.rv_log);
         tvTitle = findViewById(R.id.tv_title);
         tvRtmpOk = findViewById(R.id.tv_rtmp_ok);
         tvRtmpStatus = findViewById(R.id.tv_rtmp_status);
         tvRtcOk = findViewById(R.id.tv_rtc_ok);
         rvMsgList = findViewById(R.id.rv_msg_list);
         viewSpace=findViewById(R.id.view_space);
        mImmersionBar.titleBar(viewSpace).init();
         tvMemberNum=findViewById(R.id.tv_member_num);
         tvLineList=findViewById(R.id.tv_line_list);

        pushURL = getIntent().getStringExtra("pushURL");
        initLineFragment();
        mAdapter = new LiveMessageAdapter();
        rvMsgList.setLayoutManager(new LinearLayoutManager(this));
        rvMsgList.setAdapter(mAdapter);

        logAdapter = new LogAdapter();
        rvLog.setLayoutManager(new LinearLayoutManager(this));
        logAdapter.bindToRecyclerView(rvLog);

        //设置视频质量 参数对应清晰度，可查看API文档
        ARRtmpcEngine.Inst().getHosterOption().setVideoProfile(ARVideoCommon.ARVideoProfile.ARVideoProfile480x640);
        tvTitle.setText("房间ID:" + liveId);
        ARRtmpcEngine.Inst().getHosterOption().setVideoOrientation(ARVideoCommon.ARVideoOrientation.Portrait);
        //音频管理对象  当靠近听筒时将会减小音量
        mRtmpAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });
        mRtmpAudioManager.init();
        ARRtmpcEngine.Inst().getHosterOption().setMediaType(ARVideoCommon.ARMediaType.Video);
        //实例化主播对象

        mHosterKit = new ARRtmpcHosterKit(mHosterListener);
        mHosterKit.setAudioActiveCheck(true);
        //实例化连麦窗口对象
        mVideoView = new ARVideoView(rlRtmpcVideos, ARRtmpcEngine.Inst().egl(), this,false,true);
        mVideoView.setVideoViewLayout(false,Gravity.RIGHT,LinearLayout.VERTICAL);
        mVideoView.setVideoLayoutOnclickEvent(mBtnVideoCloseEvent);
        //设置本地视频采集
        VideoRenderer render = mVideoView.openLocalVideoRender();
        mHosterKit.setLocalVideoCapturer(render.GetRenderPointer());
        //开始推流
        mHosterKit.startPushRtmpStream(pushURL);
        //创建RTC连接，必须放在开始推流之后
        mHosterKit.createRTCLine("", liveId, userId, getUserData(), getLiveInfo(pushURL,pushURL));
        //设置音频连麦直播，默认视频



        //=====================================视频数据相关===============================
//        /**
//         *  设置是否采用ARCamera，默认使用ARCamera， 如果设置为false，必须调用setByteBufferFrameCaptured才能本地显示
//         * @param usedARCamera true：使用ARCamera，false：不使用ARCamera采集的数据
//         */
//        mHosterKit.setUsedARCamera(true);
//        /**
//         * 设置本地显示的视频数据
//         * @param data 相机采集数据
//         * @param width 宽
//         * @param height 高
//         * @param rotation 旋转角度
//         * @param timeStamp 时间戳
//         */
//        mHosterKit.setByteBufferFrameCaptured();
       //设置ARCamera视频回调数据
//        mHosterKit.setARCameraCaptureObserver(new VideoCapturer.ARCameraCapturerObserver() {
//            @Override
//            public void onByteBufferFrameCaptured(byte[] data, int width, int height, int rotation, long timeStamp) {
//            }
//        });
        //=====================================视频数据相关===============================
    }

    public String getLiveInfo(String pullUrl,String hlsUrl) {
        JSONObject liveInfo = new JSONObject();

        try {
            liveInfo.put("hosterId", userId);
            liveInfo.put("rtmpUrl", pullUrl);
            liveInfo.put("hlsUrl", hlsUrl);
            liveInfo.put("liveTopic", ARApplication.LIVE_ID);
            liveInfo.put("anyrtcId", ARApplication.LIVE_ID);
            liveInfo.put("isAudioLive", 0);
            liveInfo.put("hosterName", ARApplication.getNickName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return liveInfo.toString();
    }

    public String getUserData() {
        JSONObject user = new JSONObject();
        try {
            user.put("isHost", 1);
            user.put("userId", userId);
            user.put("nickName", ARApplication.getNickName());
            user.put("headUrl", "www.baidu.com");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user.toString();
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }


    /**
     * 连线时小图标的关闭按钮
     */
    private ARVideoView.VideoLayoutOnclickEvent mBtnVideoCloseEvent = new ARVideoView.VideoLayoutOnclickEvent() {
        @Override
        public void onCloseVideoRender(View view, String strPeerId) {
            mHosterKit.hangupRTCLine(strPeerId);

        }
    };

    /**
     * 更细列表
     *
     * @param chatMessageBean
     */
    private void addChatMessageList(MessageBean chatMessageBean) {
        // 150 条 修改；

        if (chatMessageBean == null) {
            return;
        }

        if (mAdapter.getData().size() < 150) {
            mAdapter.addData(chatMessageBean);
        } else {
            mAdapter.remove(0);
            mAdapter.addData(chatMessageBean);
        }
        rvMsgList.smoothScrollToPosition(mAdapter.getData().size() - 1);
    }


    private void showChatLayout() {
        KeyboardDialogFragment keyboardDialogFragment = new KeyboardDialogFragment();
        keyboardDialogFragment.show(getSupportFragmentManager(), "KeyboardDialogFragment");
        keyboardDialogFragment.setEdittextListener(new KeyboardDialogFragment.EdittextListener() {
            @Override
            public void setTextStr(String text) {
                addChatMessageList(new MessageBean(MessageBean.VIDEO, nickname, text));
                mHosterKit.sendMessage(0, nickname, "", text);
            }

            @Override
            public void dismiss(DialogFragment dialogFragment) {
            }
        });
    }

    private void ShowExitDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle(R.string.str_exit);
        build.setMessage(R.string.str_live_stop);
        build.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (mHosterKit != null) {
                    mHosterKit.stopRtmpStream();
                }
                finishAnimActivity();
            }
        });
        build.setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        build.show();
    }


    public void printLog(String log){
        Log.d("RTMPC", log);
        logAdapter.addData(log);
    }
    /**
     * 主播回调信息接口
     */
    private ARRtmpcHosterEvent mHosterListener = new ARRtmpcHosterEvent() {
        /**
         * rtmp连接成功
         */
        @Override
        public void onRtmpStreamOk() {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamOk");
                    if (tvRtmpOk != null) {
                        tvRtmpOk.setText("Rtmp连接成功");
                    }
                }
            });
        }

        /**
         * rtmp 重连次数
         * @param times 重连次数
         */
        @Override
        public void onRtmpStreamReconnecting(final int times) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamReconnecting times:" + times);
                    if (tvRtmpStatus != null) {
                        tvRtmpStatus.setText(String.format(getString(R.string.str_reconnect_times), times));
                    }
                }
            });
        }

        /**
         * rtmp 推流状态
         * @param delayMs 推流延时
         * @param netBand 推流码流
         */
        @Override
        public void onRtmpStreamStatus(final int delayMs, final int netBand) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamStatus delayMs:" + delayMs + "netBand:" + netBand);
                    if (tvRtmpStatus != null) {
                        tvRtmpStatus.setText(String.format(getString(R.string.str_rtmp_status), delayMs + "ms", netBand / 1024 / 8 + "kb/s"));
                    }
                }
            });
        }

        /**
         * rtmp推流失败回调
         * @param code
         */
        @Override
        public void onRtmpStreamFailed(final int code) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamFailed code:" + code);
                    if (tvRtcOk != null) {
                        tvRtcOk.setText(R.string.str_rtmp_connect_failed);
                    }
                }
            });
        }

        /**
         * rtmp 推流关闭回调
         */
        @Override
        public void onRtmpStreamClosed() {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamClosed ");
                    finish();
                }
            });

        }


        /**
         * RTC 连接回调
         * @param code 0： 连接成功
         */
        @Override
        public void onRTCCreateLineResult(final int code, String s) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调： onRTCCreateLineResult  code:" + code);
                    if (tvRtcOk != null) {
                        if (code == 0) {
                            tvRtcOk.setText(R.string.str_rtc_connect_success);
                        } else {
                            tvRtcOk.setText(AnyRTCUtils.getErrString(code));
                        }
                    }
                }
            });
        }


        /**
         * 游客有申请连线回调
         *
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void onRTCApplyToLine(final String strLivePeerID, final String strCustomID, final String strUserData) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCApplyToLine  strLivePeerID:" + strLivePeerID + " strCustomID:" + strCustomID + " strUserData:" + strUserData);
                    try {
                        String userdata = URLDecoder.decode(strUserData);
                        JSONObject jsonObject = new JSONObject(userdata);
                        if (line_dialog != null && lineListener != null && mHosterKit != null && tvLineList != null) {
                            lineListener.AddGuest(new LineBean(strLivePeerID, jsonObject.getString("nickName"), false), mHosterKit);
                            tvLineList.setSelected(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }


        /**
         * 游客挂断连线回调
         * @param strLivePeerID
         */
        @Override
        public void onRTCCancelLine(final int nCode, final String strLivePeerID) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCCancelLine  strLivePeerID:" + strLivePeerID + "nCode:" + nCode);
                    if (nCode == 0) {
                        if (line_dialog != null && lineListener != null) {
                            lineListener.RemoveGuest(strLivePeerID);
                        }
                    }

                    if (nCode == 602) {
                        ToastUtil.show("连麦人数已满");
                    }
                }
            });
        }


        @Override
        public void onRTCOpenRemoteVideoRender(final String strLivePeerId, final String strPublishId, final String strUserId, final String strUserData) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCOpenVideoRender  strPublishId:" + strPublishId + " strUserId:" + strUserId + " strUserData:" + strUserData);
                    final VideoRenderer render = mVideoView.openRemoteVideoRender(strLivePeerId);
                    if (null != render) {
                        mHosterKit.setRTCRemoteVideoRender(strPublishId, render.GetRenderPointer());
                    }
                }
            });
        }

        @Override
        public void onRTCCloseRemoteVideoRender(final String strLivePeerId, final String strPublishId, final String strUserId) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCCloseVideoRender  strPublishId:" + strPublishId + " strUserId:" + strUserId);
                    mHosterKit.setRTCRemoteVideoRender(strPublishId, 0);
                    mVideoView.removeRemoteRender(strLivePeerId);
                    if (line_dialog != null && lineListener != null) {
                        lineListener.RemoveGuest(strLivePeerId);
                    }
                }
            });
        }

        @Override
        public void onRTCOpenRemoteAudioLine(String s, String s1, String s2) {

        }

        @Override
        public void onRTCCloseRemoteAudioLine(String s, String s1) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRTLocalAudioActive(int i) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTLocalAudioActive ");
                }
            });
        }

        @Override
        public void onRTCRemoteAudioActive(String s, String s1, int i) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCRemoteAudioActive ");
                }
            });
        }

        @Override
        public void onRTCRemoteAVStatus(final String s, boolean b, boolean b1) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCRemoteAVStatus peerID:"+s);
                }
            });
        }


        /**
         * RTC 连接关闭回调
         * @param code 207：请去AnyRTC官网申请账号,如有疑问请联系客服!
         */
        @Override
        public void onRTCLineClosed(final int code, String s) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCLineClosedLine  code:" + code);
                    if (code == 207) {
                        Toast.makeText(HosterActivity.this, getString(R.string.str_apply_anyrtc_account), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }

        /**
         * 连线接通时的视频图像回调；
         */


        /**
         * 连线关闭时的视频图像回调；
         */


        /**
         * 消息回调
         * @param strCustomID 消息的发送者id
         * @param strCustomName 消息的发送者昵称
         * @param strCustomHeader 消息的发送者头像url
         * @param strMessage 消息内容
         */
        @Override
        public void onRTCUserMessage(final int nType, final String strCustomID, final String strCustomName, final String strCustomHeader, final String strMessage) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCUserMessage  nType:" + nType + " strUserId:" + strCustomID + " strCustomName:" + strCustomName + " strCustomHeader:" + strCustomHeader + " strMessage:" + strMessage);
                    addChatMessageList(new MessageBean(MessageBean.VIDEO, strCustomName, strMessage));
                }
            });
        }

        /**
         * 观看直播的总人数回调
         * @param totalMembers 观看直播的总人数
         */
        @Override
        public void onRTCMemberNotify(final String strServerId, final String strRoomId, final int totalMembers) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCMemberNotify strServerId:" + strServerId + "strRoomId:" + strRoomId + "totalMembers:" + totalMembers);
                    tvMemberNum.setText("在线人数：" + totalMembers + "");

                }
            });
        }

    };


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camare:
                if (mHosterKit == null) {
                    return;
                }
                mHosterKit.switchCamera();
                break;
            case R.id.btn_close:
                ShowExitDialog();
                break;
            case R.id.iv_message:
                showChatLayout();
                break;
            case R.id.tv_line_list:
                if (isShowLineList) {
                    if (line_dialog != null) {
                        line_dialog.hide();
                        isShowLineList = false;
                    }
                } else {
                    if (line_dialog != null) {
                        line_dialog.show();
                        tvLineList.setSelected(false);
                        isShowLineList = true;
                    }
                }
                break;
            case R.id.btn_log:
                rl_log_layout.setVisibility(View.VISIBLE);
                break;
            case R.id.ibtn_close_log:
                rl_log_layout.setVisibility(View.GONE);
                break;
        }
    }


    private void initLineFragment() {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.item_line_list)
                .setAnimId(R.style.AnimBottom)
                .setGravity(Gravity.BOTTOM)
                .setLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, DisplayUtils.getScreenHeightPixels(this) / 3)
                .setBackgroundDrawable(true)
                .build();
        line_dialog = builder.show(new CustomDialog.Builder.onInitListener() {
            @Override
            public void init(CustomDialog view) {
                if (lineFragment == null) {
                    lineFragment = new LineFragment();
                }
            }
        });
        line_dialog.hide();
        line_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isShowLineList = false;
            }
        });

    }




    public interface LineListener {
        void AddAudioGuest(LineBean lineBean, ARRtmpcHosterKit hosterKit);//添加音频游客的申请到列表

        void AddGuest(LineBean lineBean, ARRtmpcHosterKit hosterKit);//添加游客的申请到列表

        void RemoveGuest(String peerid);//从列表移除游客
    }

    public void SetLineListener(LineListener mLineListener) {
        this.lineListener = mLineListener;
    }

    public void closeLineDialog() {
        if (line_dialog != null) {
            line_dialog.hide();
        }
    }

}

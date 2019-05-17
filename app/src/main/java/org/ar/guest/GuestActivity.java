package org.ar.guest;

import android.annotation.SuppressLint;
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
import org.ar.model.MessageBean;
import org.ar.rtmpc.R;
import org.ar.utils.ARUtils;
import org.ar.utils.ToastUtil;
import org.ar.widgets.ARVideoView;
import org.ar.widgets.KeyboardDialogFragment;
import org.ar.common.enums.ARVideoCommon;
import org.ar.rtmpc_hybrid.ARRtmpcEngine;
import org.ar.rtmpc_hybrid.ARRtmpcGuestEvent;
import org.ar.rtmpc_hybrid.ARRtmpcGuestKit;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoRenderer;

/**
 * 视频游客页面
 */
public class GuestActivity extends BaseActivity {
    RelativeLayout rlRtmpcVideos,rl_log_layout;
    TextView tvTitle;
    TextView tvRtmpOk;
    TextView tvRtmpStatus;
    TextView tvRtcOk;
    RecyclerView rvMsgList,rvLog;
    TextView tvApplyLine;
    View viewSpace;
    TextView tvMemberNum;
    ImageButton ibtnCamera;
    private ARRtmpcGuestKit mGuestKit;
    private ARVideoView mVideoView;
    private AnyRTCAudioManager mRtmpAudioManager = null;
    private LiveMessageAdapter mAdapter;
    private LogAdapter logAdapter;
    private boolean isApplyLine = false;//是否申请连麦
    private boolean isLining = false;//是否正在连麦
    private String liveId = "";
    private String userId="guest"+(int)((Math.random()*9+1)*100000)+"";
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isApplyLine) {
                ShowExitDialog();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }

        /**
         * 销毁rtmp播放器
         */
        if (mGuestKit != null) {
            mGuestKit.clean();
            mVideoView.removeLocalVideoRender();
            mGuestKit = null;
        }
    }

    @Override
    public int getLayoutId() {
        return org.ar.rtmpc.R.layout.activity_guest;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //保持屏幕常亮
        super.onCreate(savedInstanceState);
    }


    @Override
    public void initView(Bundle savedInstanceState) {
        viewSpace=findViewById(org.ar.rtmpc.R.id.view_space);
        mImmersionBar.titleBar(viewSpace).init();
        ibtnCamera=findViewById(org.ar.rtmpc.R.id.btn_camare);
        rlRtmpcVideos=findViewById(org.ar.rtmpc.R.id.rl_rtmpc_videos);
        rl_log_layout=findViewById(org.ar.rtmpc.R.id.rl_log_layout);
        rvLog=findViewById(org.ar.rtmpc.R.id.rv_log);
        tvTitle=findViewById(org.ar.rtmpc.R.id.tv_title);
        tvRtmpOk=findViewById(org.ar.rtmpc.R.id.tv_rtmp_ok);
        tvRtmpStatus=findViewById(org.ar.rtmpc.R.id.tv_rtmp_status);
        tvRtcOk=findViewById(org.ar.rtmpc.R.id.tv_rtc_ok);
        rvMsgList=findViewById(org.ar.rtmpc.R.id.rv_msg_list);
        tvApplyLine=findViewById(org.ar.rtmpc.R.id.tv_apply_line);
        tvMemberNum=findViewById(org.ar.rtmpc.R.id.tv_member_num);

        logAdapter = new LogAdapter();
        rvLog.setLayoutManager(new LinearLayoutManager(this));
        logAdapter.bindToRecyclerView(rvLog);
        rvMsgList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new LiveMessageAdapter();
        rvMsgList.setAdapter(mAdapter);
        ARRtmpcEngine.Inst().getGuestOption().setDefaultFrontCamera(true);
        ARRtmpcEngine.Inst().getGuestOption().setMediaType(ARVideoCommon.ARMediaType.Video);
        String pullUrl = getIntent().getStringExtra("pullURL");
        liveId = getIntent().getStringExtra("liveId");
        tvTitle.setText("房间ID:" +liveId);
        mVideoView = new ARVideoView( rlRtmpcVideos, ARRtmpcEngine.Inst().egl(), this,false, false);
        mVideoView.setVideoViewLayout(false, Gravity.RIGHT, LinearLayout.VERTICAL);
        mVideoView.setVideoLayoutOnclickEvent(new ARVideoView.VideoLayoutOnclickEvent() {
            @Override
            public void onCloseVideoRender(View view, String strPeerId) {
                /**
                 * 挂断连线
                 */
                mGuestKit.hangupRTCLine();
                mVideoView.removeRemoteRender("LocalCameraRender");
                tvApplyLine.setText(org.ar.rtmpc.R.string.str_connect_hoster);
                tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_apply_line);
                isApplyLine = false;
                isLining=false;
            }
        });
        mRtmpAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
            // This method will be called each time the audio state (number
            // and
            // type of devices) has been changed.
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });
        mRtmpAudioManager.init();
        ARRtmpcEngine.Inst().getGuestOption().setMediaType(ARVideoCommon.ARMediaType.Video);
        mGuestKit = new ARRtmpcGuestKit(mGuestListener);
        mGuestKit.setAudioActiveCheck(true);
        VideoRenderer render = mVideoView.openLocalVideoRender();
        mGuestKit.startRtmpPlay(pullUrl, render.GetRenderPointer());
        mGuestKit.joinRTCLine("", liveId, userId, getUserData());
    }


    public String getUserData() {
        JSONObject user = new JSONObject();
        try {
            user.put("isHost", 0);
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

    private void ShowExitDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle(org.ar.rtmpc.R.string.str_exit);
        build.setMessage(org.ar.rtmpc.R.string.str_line_hangup);
        build.setPositiveButton(org.ar.rtmpc.R.string.str_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                mGuestKit.hangupRTCLine();
                mVideoView.removeRemoteRender("LocalCameraRender");
                finishAnimActivity();
            }
        });
        build.setNegativeButton(org.ar.rtmpc.R.string.str_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });

        build.show();
    }


    /**
     * 更新列表
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

    public void printLog(String log){
        Log.d("RTMPC", log);
        logAdapter.addData(log);
    }

    /**
     * 观看直播回调信息接口
     */
    private ARRtmpcGuestEvent mGuestListener = new ARRtmpcGuestEvent() {

        /**
         * rtmp 连接成功 视频即将播放；视频播放前的操作可以在此接口中进行操作
         */
        @Override
        public void onRtmpPlayerOk() {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpPlayerOk");
                    if (tvRtmpOk != null) {
                        tvRtmpOk.setText("Rtmp连接成功");
                    }
                }
            });
        }

        /**
         * rtmp 开始播放 视频开始播放
         */
        @Override
        public void onRtmpPlayerStart() {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpPlayerStart");
                }
            });
        }

        /**
         * rtmp 当前播放状态
         * @param cacheTime 当前缓存时间
         * @param curBitrate 当前播放器码流
         */
        @Override
        public void onRtmpPlayerStatus(final int cacheTime, final int curBitrate) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpPlayerStatus cacheTime:" + cacheTime + " curBitrate:" + curBitrate);
                    if (tvRtmpStatus != null) {
                        tvRtmpStatus.setText("当前缓存时间：" + cacheTime + " ms" + "\n当前码流：" + curBitrate / 10024 / 8 + "kb/s");
                    }

                }
            });
        }

        /**
         * rtmp 播放缓冲区时长
         * @param nPercent 缓冲时间
         */
        @Override
        public void onRtmpPlayerLoading(final int nPercent) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpPlayerCache  nPercent:" + nPercent);
                }
            });
        }

        /**
         * rtmp 播放器关闭
         * @param nCode
         */
        @Override
        public void onRtmpPlayerClosed(final int nCode) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpPlayerClosed  nCode:" + nCode);
                }
            });
        }


        /**
         * 游客RTC 状态回调
         * @param nCode 回调响应码：0：正常；101：主播未开启直播；
         */
        @Override
        public void onRTCJoinLineResult(final int nCode, String s) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCJoinLineResult  nCode:" + nCode);
                    if (tvRtcOk != null) {
                        if (nCode == 0) {
                            tvRtcOk.setText(org.ar.rtmpc.R.string.str_rtc_connect_success);
                        } else if (nCode == 101) {
                            Toast.makeText(GuestActivity.this, org.ar.rtmpc.R.string.str_hoster_not_live, Toast.LENGTH_LONG).show();
                            tvRtcOk.setText(org.ar.rtmpc.R.string.str_rtc_connect_success);
                        } else {
                            tvRtcOk.setText(ARUtils.getErrString(nCode));
                        }
                    }
                }
            });
        }

        /**
         * 游客申请连线回调
         */
        @Override
        public void onRTCApplyLineResult(final int nCode) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    printLog("回调：onRTCApplyLineResult  nCode:" + nCode);
                    if (nCode == 0) {
                        ibtnCamera.setVisibility(View.VISIBLE);
                        isApplyLine = true;
                        isLining = true;
                        tvApplyLine.setText("挂断");
                        tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_hang_up_line);
                        VideoRenderer render = mVideoView.openRemoteVideoRender("LocalCameraRender");
                        mGuestKit.setLocalVideoCapturer(render.GetRenderPointer());
                    } else if (nCode == 601) {
                        Toast.makeText(GuestActivity.this, org.ar.rtmpc.R.string.str_hoster_refused, Toast.LENGTH_LONG).show();
                        isApplyLine = false;
                        ibtnCamera.setVisibility(View.GONE);
                        tvApplyLine.setText("连麦");
                        tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_apply_line);
                    }
                }
            });
        }


        /**
         * 挂断连线回调
         */
        @Override
        public void onRTCHangupLine() {
            //主播连线断开
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCHangupLine ");
                    mGuestKit.hangupRTCLine();
                    ibtnCamera.setVisibility(View.GONE);
                    mVideoView.removeRemoteRender("LocalCameraRender");
                    tvApplyLine.setText(org.ar.rtmpc.R.string.str_connect_hoster);
                    tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_apply_line);
                    isApplyLine = false;
                    isLining = false;
                }
            });
        }


        @Override
        public void onRTCOpenRemoteVideoRender(final String strLivePeerId, final String strPublishId, final String strUserId, final String strUserData) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCOpenVideoRenderLeave strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId + " strUserData:" + strUserData);
                    final VideoRenderer render = mVideoView.openRemoteVideoRender(strLivePeerId);
                    mGuestKit.setRTCRemoteVideoRender(strPublishId, render.GetRenderPointer());
                }
            });
        }

        @Override
        public void onRTCCloseRemoteVideoRender(final String strLivePeerId, final String strPublishId, final String strUserId) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCloseVideoRender strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId);
                    if (mGuestKit != null && mVideoView != null ) {
                        mGuestKit.setRTCRemoteVideoRender(strPublishId, 0);
                        mVideoView.removeRemoteRender(strLivePeerId);
                    }
                }
            });
        }

        @Override
        public void onRTCOpenRemoteAudioLine(String s, String s1, String s2) {

        }

        @Override
        public void onRTCCloseRemoteAudioLine(String s, String s1) {

        }

        @Override
        public void onRTCLocalAudioActive(int i) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCLocalAudioActive  ");
                }
            });
        }

        @Override
        public void onRTCHosterAudioActive(int i) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCHosterAudioActive  ");
                }
            });
        }


        @Override
        public void onRTCRemoteAudioActive(final String s, String s1, int i) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCRemoteAudioActive  peerID:" + s);
                }
            });
        }

        @Override
        public void onRTCRemoteAVStatus(final String s, boolean b, boolean b1) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCRemoteAVStatus  peerID:" + s);
                }
            });
        }

        /**
         * 主播已离开回调

         */
        @Override
        public void onRTCLineLeave(final int nCode, String s) {
            //主播关闭直播
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCLineLeave nCode:" + nCode);
                    if (mGuestKit != null) {
                        mGuestKit.stopRtmpPlay();
                    }
                    if (nCode == 0) {
                        ToastUtil.show("主播已离开");
                    } else if (nCode == 100) {
                        ToastUtil.show("网络已断开");
                    }
                    finishAnimActivity();
                }
            });
        }

        /**
         * 连线接通后回调
         * @param strLivePeerId
         */


        /**
         * 连线关闭后图像回调
         * @param strLivePeerId
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
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCUserMessage nType:" + nType + "strCustomID:" + strCustomID + "strCustomName:" + strCustomName + "strCustomHeader:" + strCustomHeader + "strMessage:" + strMessage);
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
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCMemberNotify strServerId:" + strServerId + "strRoomId:" + strRoomId + "totalMembers:" + totalMembers);
                    if (tvMemberNum != null) {
                        tvMemberNum.setText("在线人数" + totalMembers + "");
                    }


                }
            });
        }

    };

    public void onClick(View view) {
        switch (view.getId()) {
            case org.ar.rtmpc.R.id.btn_close:
                if (isLining) {
                    ShowExitDialog();
                } else {
                    finishAnimActivity();
                }
                break;
            case org.ar.rtmpc.R.id.iv_message:
                showChatLayout();
                break;
            case org.ar.rtmpc.R.id.tv_apply_line:
                if (isApplyLine) {
                    if (mGuestKit != null) {
                        mGuestKit.hangupRTCLine();
                        tvApplyLine.setText("连麦");
                        ibtnCamera.setVisibility(View.GONE);
                        tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_apply_line);
                        mVideoView.removeRemoteRender("LocalCameraRender");
                        isApplyLine = false;
                    }
                } else {
                    if (mGuestKit != null) {
                        mGuestKit.applyRTCLine();
                        tvApplyLine.setText("挂断");
                        tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_hang_up_line);
                        isApplyLine = true;
                    }
                }
                break;
            case org.ar.rtmpc.R.id.btn_log:
                rl_log_layout.setVisibility(View.VISIBLE);
                break;
            case org.ar.rtmpc.R.id.ibtn_close_log:
                rl_log_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_camare:
                mGuestKit.switchCamera();
                break;
        }
    }

    private void showChatLayout() {
        KeyboardDialogFragment keyboardDialogFragment = new KeyboardDialogFragment();
        keyboardDialogFragment.show(getSupportFragmentManager(), "KeyboardDialogFragment");
        keyboardDialogFragment.setEdittextListener(new KeyboardDialogFragment.EdittextListener() {
            @Override
            public void setTextStr(String text) {
                addChatMessageList(new MessageBean(MessageBean.VIDEO, ARApplication.getNickName(), text));
                mGuestKit.sendMessage(0, ARApplication.getNickName(), "", text);
            }

            @Override
            public void dismiss(DialogFragment dialogFragment) {
            }
        });
    }

}

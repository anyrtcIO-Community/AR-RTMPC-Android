package org.anyrtc.guest;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.anyrtc.BaseActivity;
import org.anyrtc.HybirdApplication;
import org.anyrtc.adapter.LiveMessageAdapter;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LiveBean;
import org.anyrtc.model.MessageBean;
import org.anyrtc.rtmpc_hybrid.RTMPCGuestKit;
import org.anyrtc.rtmpc_hybrid.RTMPCGuestVideoOption;
import org.anyrtc.rtmpc_hybrid.RTMPCHybrid;
import org.anyrtc.rtmpc_hybrid.RTMPCVideoGuestEvent;
import org.anyrtc.utils.AnyRTCUtils;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.ToastUtil;
import org.anyrtc.widgets.CustomDialog;
import org.anyrtc.widgets.KeyboardDialogFragment;
import org.anyrtc.widgets.MemberListDialog;
import org.anyrtc.widgets.RTMPCVideoView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 视频游客页面
 */
public class GuestActivity extends BaseActivity {
    @BindView(R.id.rl_rtmpc_videos)
    RelativeLayout rlRtmpcVideos;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_jingxiang)
    ImageButton btnJingxiang;
    @BindView(R.id.btn_audio)
    ImageButton btnAudio;
    @BindView(R.id.btn_video)
    ImageButton btnVideo;
    @BindView(R.id.btn_close)
    ImageButton btnClose;
    @BindView(R.id.tv_rtmp_ok)
    TextView tvRtmpOk;
    @BindView(R.id.tv_rtmp_status)
    TextView tvRtmpStatus;
    @BindView(R.id.tv_rtc_ok)
    TextView tvRtcOk;
    @BindView(R.id.rv_msg_list)
    RecyclerView rvMsgList;
    @BindView(R.id.iv_message)
    ImageView ivMessage;
    @BindView(R.id.tv_apply_line)
    TextView tvApplyLine;
    @BindView(R.id.view_space)
    View viewSpace;
    @BindView(R.id.llayout_guest_tools)
    RelativeLayout llayoutGuestTools;
    @BindView(R.id.tv_room_id)
    TextView tvRoomId;
    @BindView(R.id.tv_member_num)
    TextView tvMemberNum;
    @BindView(R.id.ll_line_futures)
    LinearLayout llLineFutures;
    private RTMPCGuestKit mGuestKit;
    private RTMPCVideoView mVideoView;
    private AnyRTCAudioManager mRtmpAudioManager = null;
    private LiveMessageAdapter mAdapter;
    LiveBean liveBean;
    private boolean isApplyLine = false;//是否申请连麦
    private boolean isLining = false;//是否正在连麦
    private CustomDialog member_dialog;
    MemberListDialog memberListDialog;

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
            mGuestKit.clear();
            mVideoView.OnRtcRemoveLocalRender();
            mGuestKit = null;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_guest;
    }


    @Override
    public void initView(Bundle savedInstanceState) {
        mImmersionBar.titleBar(viewSpace).init();
        memberListDialog = new MemberListDialog();
        rvMsgList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new LiveMessageAdapter();
        rvMsgList.setAdapter(mAdapter);
        Bundle bundle = getIntent().getExtras();
        RTMPCGuestVideoOption option=new RTMPCGuestVideoOption();
        option.setmBFront(true);
        if (bundle != null) {
            liveBean = (LiveBean) bundle.getSerializable(Constans.LIVEBEAN);
            if (liveBean != null) {
                tvTitle.setText(liveBean.getmLiveTopic());
                tvRoomId.setText("房间ID:" + liveBean.getmAnyrtcId());
                if (liveBean.getIsLiveLandscape() == 0) {
                    option.setmScreenOriention(AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    option.setmScreenOriention(AnyRTCScreenOrientation.AnyRTC_SCRN_Landscape);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                mVideoView = new RTMPCVideoView(this,rlRtmpcVideos, RTMPCHybrid.Inst().egl(), false, RTMPCVideoView.RTMPCVideoLayout.RTMPC_V_1X3);
                mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);
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
                mGuestKit = new RTMPCGuestKit(mGuestListener, option);
                VideoRenderer render = mVideoView.OnRtcOpenLocalRender(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                mGuestKit.startRtmpPlay(liveBean.getmRtmpPullUrl(), render.GetRenderPointer());
                mGuestKit.joinRTCLine(liveBean.getmAnyrtcId(), "guest", getUserData());
            }
        }
    }


    public String getUserData() {
        JSONObject user = new JSONObject();
        try {
            user.put("isHost", 0);
            user.put("userId", "guest");
            user.put("nickName", HybirdApplication.getNickName());
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
        build.setTitle(R.string.str_exit);
        build.setMessage(R.string.str_line_hangup);
        build.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                mGuestKit.hangupRTCLine();
                mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
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


    /**
     * 连线时小图标的关闭按钮连接
     */
    private RTMPCVideoView.BtnVideoCloseEvent mBtnVideoCloseEvent = new RTMPCVideoView.BtnVideoCloseEvent() {

        @Override
        public void CloseVideoRender(View view, String strPeerId) {
            /**
             * 挂断连线
             */
            mGuestKit.hangupRTCLine();
            mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
            tvApplyLine.setText(R.string.str_connect_hoster);
            llLineFutures.setVisibility(View.GONE);
            isApplyLine = false;
        }

        @Override
        public void OnSwitchCamera(View view) {
            /**
             * 连线时切换游客摄像头
             */
            mGuestKit.switchCamera();
        }
    };

    /**
     * 观看直播回调信息接口
     */
    private RTMPCVideoGuestEvent mGuestListener = new RTMPCVideoGuestEvent() {

        /**
         * rtmp 连接成功 视频即将播放；视频播放前的操作可以在此接口中进行操作
         */
        @Override
        public void onRtmpPlayerOk() {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRtmpPlayerOk");
                    if (tvRtmpOk!=null) {
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
                    Log.d("RTMPC", "onRtmpPlayerStart");
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
                    Log.d("RTMPC", "onRtmpPlayerStatus cacheTime:" + cacheTime + " curBitrate:" + curBitrate);
                    if (tvRtmpStatus!=null) {
                        tvRtmpStatus.setText("当前缓存时间：" + cacheTime+" ms" + "\n当前码流：" + curBitrate/10024/8+"kb/s");
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
                    Log.d("RTMPC", "onRtmpPlayerCache  nPercent:" + nPercent);
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
                    Log.d("RTMPC", "onRtmpPlayerClosed  nCode:" + nCode);
                }
            });
        }

        /**
         * 游客RTC 状态回调
         * @param nCode 回调响应码：0：正常；101：主播未开启直播；
         */
        @Override
        public void onRTCJoinLineResult(final int nCode) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCJoinLineResult  nCode:" + nCode);
                    if (tvRtcOk!=null) {
                        if (nCode == 0) {
                            tvRtcOk.setText(R.string.str_rtc_connect_success);
                        } else if (nCode == 101) {
                            Toast.makeText(GuestActivity.this, R.string.str_hoster_not_live, Toast.LENGTH_LONG).show();
                            tvRtcOk.setText(R.string.str_rtc_connect_success);
                        } else {
                            tvRtcOk.setText(AnyRTCUtils.getErrString(nCode));
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
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCApplyLineResult  nCode:" + nCode);
                    if (nCode == 0) {
                        isApplyLine = true;
                        isLining = true;
                        tvApplyLine.setText("挂断");
                        llLineFutures.setVisibility(View.VISIBLE);
                        VideoRenderer render = mVideoView.OnRtcOpenRemoteRender("LocalCameraRender", RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                        mGuestKit.setLocalVideoCapturer(render.GetRenderPointer());
                    } else if (nCode == 601) {
                        Toast.makeText(GuestActivity.this, R.string.str_hoster_refused, Toast.LENGTH_LONG).show();
                        isApplyLine = false;
                        tvApplyLine.setText("连麦");
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
                    Log.d("RTMPC", "onRTCHangupLine ");
                    mGuestKit.hangupRTCLine();
                    mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                    tvApplyLine.setText(R.string.str_connect_hoster);
                    llLineFutures.setVisibility(View.GONE);
                    isApplyLine = false;
                    isLining = false;
                }
            });
        }

        /**
         * 主播已离开回调

         */
        @Override
        public void onRTCLineLeave(final int nCode) {
            //主播关闭直播
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCLineLeave nCode:" + nCode);
                    if (mGuestKit!=null){
                        mGuestKit.stopRtmpPlay();
                    }
                    if (nCode==0) {
                        ToastUtil.show("主播已离开");
                    }else if (nCode==100){
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
        @Override
        public void onRTCOpenVideoRender(final String strLivePeerId, final String strUserId, final String strUserData) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCOpenVideoRenderLeave strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId + " strUserData:" + strUserData);
                    final VideoRenderer render = mVideoView.OnRtcOpenRemoteRender(strLivePeerId, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                    mGuestKit.setRTCVideoRender(strLivePeerId, render.GetRenderPointer());
                }
            });
        }

        /**
         * 连线关闭后图像回调
         * @param strLivePeerId
         */
        @Override
        public void onRTCCloseVideoRender(final String strLivePeerId, final String strUserId) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCloseVideoRender strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId);
                    if (mGuestKit != null && mVideoView != null && llLineFutures != null) {
                        mGuestKit.setRTCVideoRender(strLivePeerId, 0);
                        mVideoView.OnRtcRemoveRemoteRender(strLivePeerId);
                    }
                }
            });
        }


        @Override
        public void onRTCAudioActive(final String strLivePeerId, final String strUserId, final int nTime) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCAudioActive strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId + " nTime:" + nTime);
                }
            });
        }


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
                    Log.d("RTMPC", "onRTCUserMessage nType:" + nType + "strCustomID:" + strCustomID + "strCustomName:" + strCustomName + "strCustomHeader:" + strCustomHeader + "strMessage:" + strMessage);
                    addChatMessageList(new MessageBean(0, strCustomName, strMessage, strCustomHeader));
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
                    Log.d("RTMPC", "onRTCMemberNotify strServerId:" + strServerId + "strRoomId:" + strRoomId + "totalMembers:" + totalMembers);
                    if (tvMemberNum != null) {
                        tvMemberNum.setText("在线观看人数" + totalMembers + "");
                    }
                    if (memberListDialog != null) {
                        memberListDialog.setParams(liveBean.getmAnyrtcId(), strServerId, strRoomId);
                    }


                }
            });
        }
    };

    @OnClick({R.id.btn_jingxiang, R.id.btn_audio, R.id.btn_video, R.id.btn_close, R.id.iv_message, R.id.tv_apply_line, R.id.rl_member})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_jingxiang:
                if (btnJingxiang.isSelected()) {
                    btnJingxiang.setSelected(false);
                    RTMPCHybrid.Inst().setFrontCameraMirrorEnable(false);
                } else {
                    RTMPCHybrid.Inst().setFrontCameraMirrorEnable(true);
                    btnJingxiang.setSelected(true);
                }
                break;
            case R.id.btn_audio:
                if (btnAudio.isSelected()) {
                    btnAudio.setSelected(false);
                    mGuestKit.setLocalAudioEnable(true);
                } else {
                    mGuestKit.setLocalAudioEnable(false);
                    btnAudio.setSelected(true);
                }
                break;
            case R.id.btn_video:
                if (btnVideo.isSelected()) {
                    btnVideo.setSelected(false);
                    mGuestKit.setLocalVideoEnable(true);
                } else {
                    mGuestKit.setLocalVideoEnable(false);
                    btnVideo.setSelected(true);
                }
                break;
            case R.id.btn_close:
                if (isLining) {
                    ShowExitDialog();
                } else {
                    finishAnimActivity();
                }
                break;
            case R.id.iv_message:
                showChatLayout();
                break;
            case R.id.tv_apply_line:
                if (isApplyLine) {
                    if (mGuestKit != null) {
                        mGuestKit.hangupRTCLine();
                        tvApplyLine.setText("连麦");
                        llLineFutures.setVisibility(View.GONE);
                        mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                        isApplyLine = false;
                    }
                } else {
                    if (mGuestKit != null) {
                        mGuestKit.applyRTCLine();
                        tvApplyLine.setText("挂断");
                        isApplyLine = true;
                    }
                }
                break;
            case R.id.rl_member:
                if (memberListDialog != null) {
                    memberListDialog.show(getSupportFragmentManager(), "tag");
                }

                break;
        }
    }

    private void showChatLayout() {
        KeyboardDialogFragment keyboardDialogFragment = new KeyboardDialogFragment();
        keyboardDialogFragment.show(getSupportFragmentManager(), "KeyboardDialogFragment");
        ivMessage.setVisibility(View.GONE);
        keyboardDialogFragment.setEdittextListener(new KeyboardDialogFragment.EdittextListener() {
            @Override
            public void setTextStr(String text) {
                addChatMessageList(new MessageBean(1, HybirdApplication.getNickName(), text, ""));
                mGuestKit.sendUserMessage(0, HybirdApplication.getNickName(), "", text);
            }

            @Override
            public void dismiss(DialogFragment dialogFragment) {
                ivMessage.setVisibility(View.VISIBLE);
            }
        });
    }

}

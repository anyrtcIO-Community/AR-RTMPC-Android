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

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.anyrtc.BaseActivity;
import org.anyrtc.HybirdApplication;
import org.anyrtc.adapter.AudioLineAdapter;
import org.anyrtc.adapter.LiveMessageAdapter;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LineBean;
import org.anyrtc.model.LiveBean;
import org.anyrtc.model.MessageBean;
import org.anyrtc.rtmpc_hybrid.RTMPCAudioGuestEvent;
import org.anyrtc.rtmpc_hybrid.RTMPCAudioGuestKit;
import org.anyrtc.utils.AnyRTCUtils;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.ToastUtil;
import org.anyrtc.widgets.CircleImageView;
import org.anyrtc.widgets.KeyboardDialogFragment;
import org.anyrtc.widgets.MemberListDialog;
import org.anyrtc.widgets.MultiCircleDrawable;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 音频游客界面
 */
public class AudioGuestActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {
    @BindView(R.id.rl_rtmpc_videos)
    RelativeLayout rlRtmpcVideos;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_close)
    ImageButton btnClose;
    @BindView(R.id.rv_msg_list)
    RecyclerView rvMsgList;
    @BindView(R.id.iv_message)
    ImageView ivMessage;
    @BindView(R.id.tv_apply_line)
    TextView tvApplyLine;
    @BindView(R.id.view_space)
    View viewSpace;
    @BindView(R.id.iv_icon)
    CircleImageView ivIcon;
    @BindView(R.id.tv_room_id)
    TextView tvRoomId;
    @BindView(R.id.tv_member_num)
    TextView tvMemberNum;
    @BindView(R.id.ci_host)
    CircleImageView ciHost;
    @BindView(R.id.ci_host_h)
    CircleImageView ciHostH;
    @BindView(R.id.ll_v_host)
    LinearLayout llVHost;
    @BindView(R.id.ll_hor_host)
    LinearLayout llHorHost;
    @BindView(R.id.rv_line_list)
    RecyclerView rvLineList;
    @BindView(R.id.iv_anim_host_v)
    ImageView ivAnimHostV;
    @BindView(R.id.iv_anim_host_h)
    ImageView ivAnimHostH;
    @BindView(R.id.rl_bg)
    RelativeLayout rlBg;
    @BindView(R.id.tv_host_v)
    TextView tvHostV;
    @BindView(R.id.tv_host_h)
    TextView tvHostH;
    @BindView(R.id.btn_audio)
    ImageButton btnAudio;
    @BindView(R.id.tv_rtmp_ok)
    TextView tvRtmpOk;
    @BindView(R.id.tv_rtmp_status)
    TextView tvRtmpStatus;
    @BindView(R.id.tv_rtc_ok)
    TextView tvRtcOk;
    private RTMPCAudioGuestKit mGuestKit;
    private AnyRTCAudioManager mRtmpAudioManager = null;
    private LiveMessageAdapter mAdapter;
    LiveBean liveBean;
    private boolean isApplyLine = false;//是否在连麦、申请连麦
    private boolean isLinling = false;
    private boolean isFullScreen = false;//是否是横屏
    private MemberListDialog memberListDialog;
    private AudioLineAdapter audioLineAdapter;

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
        /**
         * 销毁rtmp播放器
         */
        if (mGuestKit != null) {
            mGuestKit.clear();
            mGuestKit = null;
        }
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_guest;
    }

    @Override
    public void initView(Bundle savedInstanceState) {


        mImmersionBar.titleBar(viewSpace).init();
        rvMsgList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new LiveMessageAdapter();
        audioLineAdapter = new AudioLineAdapter(false);
        audioLineAdapter.setOnItemChildClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvLineList.setLayoutManager(linearLayoutManager);
        rvLineList.setAdapter(audioLineAdapter);
        rvLineList.setItemAnimator(null);
        rvMsgList.setAdapter(mAdapter);
        memberListDialog = new MemberListDialog();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            liveBean = (LiveBean) bundle.getSerializable(Constans.LIVEBEAN);
            if (liveBean != null) {
                tvTitle.setText(liveBean.getmLiveTopic());
                tvRoomId.setText("房间ID：" + liveBean.getmAnyrtcId());
                tvHostH.setText(liveBean.getmHostName());
                tvHostV.setText(liveBean.getmHostName());
                if (liveBean.getIsLiveLandscape() == 0) {
                    isFullScreen = false;
                    llVHost.setVisibility(View.VISIBLE);
                    rlBg.setBackgroundResource(R.drawable.audio_bg_v);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    isFullScreen = true;
                    llHorHost.setVisibility(View.VISIBLE);
                    rlBg.setBackgroundResource(R.drawable.bg_audio);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
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
                mGuestKit = new RTMPCAudioGuestKit(mGuestListener);
                mGuestKit.startRtmpPlay(liveBean.getmRtmpPullUrl());
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
                isApplyLine = false;
                isLinling = false;
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
     * 观看直播回调信息接口
     */
    private RTMPCAudioGuestEvent mGuestListener = new RTMPCAudioGuestEvent() {

        /**
         * rtmp 连接成功 视频即将播放；视频播放前的操作可以在此接口中进行操作
         */
        @Override
        public void onRtmpPlayerOk() {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCJoinLineResult  nCode:" + nCode);
                    if (nCode == 0) {
                        tvRtcOk.setText(R.string.str_rtc_connect_success);
                    } else if (nCode == 101) {
                        Toast.makeText(AudioGuestActivity.this, R.string.str_hoster_not_live, Toast.LENGTH_LONG).show();
                        tvRtcOk.setText(R.string.str_rtc_connect_success);
                    } else {
                        tvRtcOk.setText(AnyRTCUtils.getErrString(nCode));
                    }
                }
            });
        }

        /**
         * 游客申请连线结果
         * @param nCode 0：申请连线成功；-1：主播拒绝连线
         */
        @Override
        public void onRTCApplyLineResult(final int nCode) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCApplyLineResult  nCode:" + nCode);
                    if (nCode == 0) {
                        isApplyLine = true;
                        tvApplyLine.setText("挂断");
                        isLinling = true;
                        btnAudio.setVisibility(View.VISIBLE);
                    } else if (nCode == 601) {
                        Toast.makeText(AudioGuestActivity.this, R.string.str_hoster_refused, Toast.LENGTH_LONG).show();
                        isApplyLine = false;
                        isLinling = false;
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCHangupLine ");
                    mGuestKit.hangupRTCLine();
                    tvApplyLine.setText(R.string.str_connect_hoster);
                    btnAudio.setVisibility(View.GONE);
                    isApplyLine = false;
                    isLinling = false;
                }
            });
        }

        /**
         * 主播已离开回调
         * @param nCode
         */
        @Override
        public void onRTCLineLeave(final int nCode) {
            //主播关闭直播
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCLineLeave nCode:" + nCode);
                    if (mGuestKit != null) {
                        mGuestKit.stopRtmpPlay();
                    }
                    finishAnimActivity();
                    ToastUtil.show("主播已离开");
                }
            });
        }



        /**
         * 连线接通后回调
         * @param
         */
        @Override
        public void onRTCOpenAudioLine(final String strLivePeerId, final String strUserId, final String strUserData) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCOpenAudioLine strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId + " strUserData:" + strUserData);
                    try {
                        JSONObject jsonObject = new JSONObject(strUserData);
                        if (strLivePeerId.equals("RTMPC_Line_Hoster")) {
                            audioLineAdapter.addData(new LineBean(strLivePeerId, jsonObject.getString("nickName"), true));
                        } else {
                            audioLineAdapter.addData(new LineBean(strLivePeerId, jsonObject.getString("nickName"), false));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onRTCCloseAudioLine(final String strLivePeerId, final String strUserId) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCloseAudioLine strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId);
                    int index = 9;
                    for (int i = 0; i < audioLineAdapter.getData().size(); i++) {
                        if (audioLineAdapter.getItem(i).peerId.equals(strLivePeerId)) {
                            index = i;
                        }
                    }
                    if (index != 9 && index <= audioLineAdapter.getData().size()) {
                        audioLineAdapter.remove(index);
                    }
                }
            });
        }

        @Override
        public void onRTCAudioActive(final String strLivePeerId, final String strUserId, final int nTime) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCAudioActive strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId + " nTime:" + nTime);
                    if (strLivePeerId.equals("RTMPC_Hoster")) {//主播
                        if (!isFullScreen) {
                            ivAnimHostV.setImageDrawable(new MultiCircleDrawable());
                        } else {
                            ivAnimHostH.setImageDrawable(new MultiCircleDrawable());
                        }
                    } else {
                        for (int i = 0; i < audioLineAdapter.getData().size(); i++) {
                            if (strLivePeerId.equals(audioLineAdapter.getData().get(i).peerId)) {
                                audioLineAdapter.getItem(i).setStartAnim(true);
                                audioLineAdapter.notifyItemChanged(i);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void onRTCAVStatus(String strLivePeerId, boolean bAudio, boolean bVideo) {

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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCMemberNotify strServerId:" + strServerId + "strRoomId:" + strRoomId + "totalMembers:" + totalMembers);
                    tvMemberNum.setText("在线观看人数" + totalMembers + "");
                    if (memberListDialog != null) {
                        memberListDialog.setParams(liveBean.getmAnyrtcId(), strServerId, strRoomId);
                    }
                }
            });
        }

        /**
         * 主播打开分享内容
         * @param nType 自定义分享类型
         * @param strUSInfo 自定义分享数据
         * @param strUserId 自定义用户ID
         * @param strUserData 自定义用户数据
         */
        @Override
        public void onRTCUserShareOpen(int nType, String strUSInfo, String strUserId, String strUserData) {

        }

        /**
         * 主播关闭分享
         */
        @Override
        public void onRTCUserShareClose() {

        }

    };

    @OnClick({R.id.btn_close, R.id.iv_message, R.id.tv_apply_line, R.id.rl_member, R.id.btn_audio})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                if (isLinling) {
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
                        isApplyLine = false;
                        isLinling = false;
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
            case R.id.btn_audio:
                if (btnAudio.isSelected()) {
                    btnAudio.setSelected(false);
                    mGuestKit.setLocalAudioEnable(true);
                } else {
                    mGuestKit.setLocalAudioEnable(false);
                    btnAudio.setSelected(true);
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


    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        switch (view.getId()) {
            case R.id.tv_hangup:
                if (mGuestKit != null) {
                    mGuestKit.hangupRTCLine();
                    btnAudio.setVisibility(View.GONE);
                    tvApplyLine.setText("连麦");
                    isApplyLine = false;
                    isLinling = false;
                }
                break;
        }
    }

}

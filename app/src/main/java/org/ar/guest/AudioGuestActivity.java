package org.ar.guest;

import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.ar.BaseActivity;
import org.ar.ARApplication;
import org.ar.adapter.AudioLineAdapter;
import org.ar.adapter.LiveMessageAdapter;
import org.ar.adapter.LogAdapter;
import org.ar.common.utils.ARAudioManager;
import org.ar.model.LineBean;
import org.ar.model.MessageBean;
import org.ar.utils.ARUtils;
import org.ar.utils.ToastUtil;
import org.ar.widgets.KeyboardDialogFragment;
import org.ar.common.enums.ARVideoCommon;
import org.ar.rtmpc_hybrid.ARRtmpcEngine;
import org.ar.rtmpc_hybrid.ARRtmpcGuestEvent;
import org.ar.rtmpc_hybrid.ARRtmpcGuestKit;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * 音频游客界面
 */
public class AudioGuestActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {
    TextView tvTitle;
    RecyclerView rvMsgList,rvLog;
    TextView tvApplyLine;
    View viewSpace;
    TextView tvMemberNum;
    RecyclerView rvLineList;
    TextView tvRtmpOk;
    TextView tvRtmpStatus;
    TextView tvRtcOk;
    TextView tvHostName;
    ImageView ivLineAnim;
    RelativeLayout rl_log_layout;
    private ARRtmpcGuestKit mGuestKit;
    private ARAudioManager mRtmpAudioManager = null;
    private LiveMessageAdapter mAdapter;
    private LogAdapter logAdapter;
    private boolean isApplyLine = false;//是否在连麦、申请连麦
    private boolean isLinling = false;
    private AnimationDrawable hostAnimation;
    private AudioLineAdapter audioLineAdapter;
    private String liveId = "";
    private String userID="guest"+(int)((Math.random()*9+1)*100000)+"";

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
            mGuestKit.clean();
            mGuestKit = null;
        }
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.stop();
            mRtmpAudioManager = null;
        }

    }


    @Override
    public int getLayoutId() {
        return org.ar.rtmpc.R.layout.activity_audio_guest;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //保持屏幕常亮
        viewSpace = findViewById(org.ar.rtmpc.R.id.view_space);
        mImmersionBar.titleBar(viewSpace).init();
        tvTitle = findViewById(org.ar.rtmpc.R.id.tv_title);
        rl_log_layout=findViewById(org.ar.rtmpc.R.id.rl_log_layout);
        rvLog=findViewById(org.ar.rtmpc.R.id.rv_log);
        rvMsgList = findViewById(org.ar.rtmpc.R.id.rv_msg_list);
        tvApplyLine = findViewById(org.ar.rtmpc.R.id.tv_apply_line);
        tvMemberNum = findViewById(org.ar.rtmpc.R.id.tv_member_num);
        rvLineList = findViewById(org.ar.rtmpc.R.id.rv_line_list);
        tvRtmpOk = findViewById(org.ar.rtmpc.R.id.tv_rtmp_ok);
        tvRtmpStatus = findViewById(org.ar.rtmpc.R.id.tv_rtmp_status);
        tvRtcOk = findViewById(org.ar.rtmpc.R.id.tv_rtc_ok);
        tvHostName = findViewById(org.ar.rtmpc.R.id.tv_host_name);
        ivLineAnim = findViewById(org.ar.rtmpc.R.id.iv_line_anim);
        hostAnimation = (AnimationDrawable) ivLineAnim.getBackground();
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
        logAdapter = new LogAdapter();
        rvLog.setLayoutManager(new LinearLayoutManager(this));
        logAdapter.bindToRecyclerView(rvLog);
        String pullUrl = getIntent().getStringExtra("pullURL");
        String hostName=getIntent().getStringExtra("hostName");
        tvHostName.setText(hostName);
        liveId = getIntent().getStringExtra("liveId");
        tvTitle.setText("房间ID：" + liveId);
        mRtmpAudioManager = ARAudioManager.create(this);
        mRtmpAudioManager.start(new ARAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(ARAudioManager.AudioDevice audioDevice, Set<ARAudioManager.AudioDevice> set) {

            }
        });
        ARRtmpcEngine.Inst().getGuestOption().setMediaType(ARVideoCommon.ARMediaType.Audio);
        mGuestKit = new ARRtmpcGuestKit(mGuestListener);
        mGuestKit.startRtmpPlay(pullUrl, 0);
        mGuestKit.joinRTCLine("", liveId, userID, getUserData());
    }

    public String getUserData() {
        JSONObject user = new JSONObject();
        try {
            user.put("isHost", 0);
            user.put("userId", userID);
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
                isApplyLine = false;
                isLinling = false;
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCJoinLineResult  nCode:" + nCode);
                    if (nCode == 0) {
                        if (tvRtcOk != null) {
                            tvRtcOk.setText(org.ar.rtmpc.R.string.str_rtc_connect_success);
                        }
                    } else if (nCode == 101) {
                        Toast.makeText(AudioGuestActivity.this, org.ar.rtmpc.R.string.str_hoster_not_live, Toast.LENGTH_LONG).show();
                        if (tvRtcOk != null) {
                            tvRtcOk.setText(org.ar.rtmpc.R.string.str_rtc_connect_success);
                        }
                    } else {
                        if (tvRtcOk != null) {
                            tvRtcOk.setText(ARUtils.getErrString(nCode));
                        }
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
                    printLog("回调：onRTCApplyLineResult  nCode:" + nCode);
                    if (nCode == 0) {
                        isApplyLine = true;
                        tvApplyLine.setText("挂断");
                        audioLineAdapter.addData(0, new LineBean("self", "自己", true));
                        isLinling = true;
                        tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_hang_up_line);
                    } else if (nCode == 601) {
                        Toast.makeText(AudioGuestActivity.this, org.ar.rtmpc.R.string.str_hoster_refused, Toast.LENGTH_LONG).show();
                        isApplyLine = false;
                        isLinling = false;
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCHangupLine ");
                    mGuestKit.hangupRTCLine();
                    audioLineAdapter.remove(0);
                    tvApplyLine.setText(org.ar.rtmpc.R.string.str_connect_hoster);
                    tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_apply_line);
                    isApplyLine = false;
                    isLinling = false;
                }
            });
        }


        @Override
        public void onRTCOpenRemoteVideoRender(String s, String s1, String s2, String s3) {

        }

        @Override
        public void onRTCCloseRemoteVideoRender(String s, String s1, String s2) {

        }

        @Override
        public void onRTCOpenRemoteAudioLine(final String strLivePeerId, final String strUserId, final String strUserData) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCOpenAudioLine strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId + " strUserData:" + strUserData);
                    try {
                        JSONObject jsonObject = new JSONObject(strUserData);
                        if (strLivePeerId.equals("RTMPC_Line_Hoster")) {
//                            audioLineAdapter.addData(new LineBean(strLivePeerId, jsonObject.getString("nickName"), true));
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
        public void onRTCCloseRemoteAudioLine(final String strLivePeerId, final String strUserId) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCCloseAudioLine strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId);
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
        public void onRTCLocalAudioActive(int i) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCLocalAudioActive");
                }
            });
        }

        @Override
        public void onRTCHosterAudioActive(int i) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCHosterAudioActive");
                }
            });
        }


        @Override
        public void onRTCRemoteAudioActive(final String strLivePeerId, final String strUserId, final int nTime) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCAudioActive strLivePeerId:" + strLivePeerId + "strUserId:" + strUserId + " nTime:" + nTime);
                    if (strLivePeerId.equals("RTMPC_Hoster")) {//主播
                        ivLineAnim.setVisibility(View.VISIBLE);
                        hostAnimation.start();
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
        public void onRTCRemoteAVStatus(final String s, boolean b, boolean b1) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCRemoteAVStatus  peerID:" + s);
                }
            });
        }

        /**
         * 主播已离开回调
         * @param nCode
         */
        @Override
        public void onRTCLineLeave(final int nCode, String s) {
            //主播关闭直播
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCLineLeave nCode:" + nCode);
                    if (mGuestKit != null) {
                        mGuestKit.stopRtmpPlay();
                    }
                    finishAnimActivity();
                    ToastUtil.show("主播已离开");
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCUserMessage nType:" + nType + "strCustomID:" + strCustomID + "strCustomName:" + strCustomName + "strCustomHeader:" + strCustomHeader + "strMessage:" + strMessage);
                    addChatMessageList(new MessageBean(MessageBean.AUDIO, strCustomName, strMessage));
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
                    printLog("回调：onRTCMemberNotify strServerId:" + strServerId + "strRoomId:" + strRoomId + "totalMembers:" + totalMembers);
                    tvMemberNum.setText("在线观看人数" + totalMembers + "");
                }
            });
        }


    };

    public void onClick(View view) {
        switch (view.getId()) {
            case org.ar.rtmpc.R.id.btn_close:
                if (isLinling) {
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
                        if (isLinling){
                            audioLineAdapter.remove(0);
                        }
                        tvApplyLine.setText("连麦");
                        tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_apply_line);
                        isApplyLine = false;
                        isLinling = false;
                    }
                } else {
                    if (mGuestKit != null) {
                        mGuestKit.applyRTCLine();
                        tvApplyLine.setText("挂断");
                        tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_hang_up_line);
                        isApplyLine = true;
                        isLinling = false;
                    }
                }
                break;
            case org.ar.rtmpc.R.id.btn_log:
                rl_log_layout.setVisibility(View.VISIBLE);
                break;
            case org.ar.rtmpc.R.id.ibtn_close_log:
                rl_log_layout.setVisibility(View.GONE);
                break;
        }
    }

    private void showChatLayout() {
        KeyboardDialogFragment keyboardDialogFragment = new KeyboardDialogFragment();
        keyboardDialogFragment.show(getSupportFragmentManager(), "KeyboardDialogFragment");
        keyboardDialogFragment.setEdittextListener(new KeyboardDialogFragment.EdittextListener() {
            @Override
            public void setTextStr(String text) {
                addChatMessageList(new MessageBean(MessageBean.AUDIO, ARApplication.getNickName(), text));
                mGuestKit.sendMessage(0, ARApplication.getNickName(), "", text);
            }

            @Override
            public void dismiss(DialogFragment dialogFragment) {
            }
        });
    }


    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        switch (view.getId()) {
            case org.ar.rtmpc.R.id.tv_hangup:
                if (mGuestKit != null) {

                    mGuestKit.hangupRTCLine();
                    tvApplyLine.setBackgroundResource(org.ar.rtmpc.R.drawable.shape_room_apply_line);
                    tvApplyLine.setText("连麦");
                    audioLineAdapter.remove(0);
                    isApplyLine = false;
                    isLinling = false;
                }
                break;
        }
    }

}

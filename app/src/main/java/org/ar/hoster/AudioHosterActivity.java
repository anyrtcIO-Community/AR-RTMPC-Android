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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.ar.BaseActivity;
import org.ar.ARApplication;
import org.ar.adapter.AudioLineAdapter;
import org.ar.adapter.LiveMessageAdapter;
import org.ar.adapter.LogAdapter;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.ar.rtmpc.R;
import org.ar.model.LineBean;
import org.ar.model.MessageBean;
import org.ar.utils.ARUtils;
import org.ar.utils.DisplayUtils;
import org.ar.utils.ToastUtil;
import org.ar.widgets.CustomDialog;
import org.ar.widgets.KeyboardDialogFragment;
import org.ar.common.enums.ARVideoCommon;
import org.ar.rtmpc_hybrid.ARRtmpcEngine;
import org.ar.rtmpc_hybrid.ARRtmpcHosterEvent;
import org.ar.rtmpc_hybrid.ARRtmpcHosterKit;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频主播页面
 */
public class AudioHosterActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener {

    TextView tvTitle, tvRtmpOk, tvRtmpStatus, tvRtcOk, tvMemberNum, tv_host_name;
    RecyclerView rvMsgList;
    View viewSpace;
    ImageButton tvLineList;
    RecyclerView rvLineList,rvLog;
    RelativeLayout rl_log_layout;
    private LogAdapter logAdapter;
    private AudioLineAdapter audioLineAdapter;
    private ARRtmpcHosterKit mHosterKit;
    private AnyRTCAudioManager mRtmpAudioManager = null;
    private LiveMessageAdapter mAdapter;
    private String nickname;
    private CustomDialog line_dialog;
    private LineFragment lineFragment;
    private boolean isShowLineList = false;
    HosterActivity.LineListener lineListener;
    private String pushURL = "",pullURL="", liveId = ARApplication.LIVE_ID,userId="host"+(int)((Math.random()*9+1)*100000)+"";
    private List<String> applyLineList=new ArrayList<>();//申请连麦的人  这个用于判断小红点显示隐藏

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

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
            mHosterKit.clean();
            mHosterKit = null;
        }

        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }


    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_hoster;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tvTitle = findViewById(R.id.tv_title);
        rl_log_layout=findViewById(R.id.rl_log_layout);
        rvLog=findViewById(R.id.rv_log);
        tvRtmpOk = findViewById(R.id.tv_rtmp_ok);
        tvRtmpStatus = findViewById(R.id.tv_rtmp_status);
        tvRtcOk = findViewById(R.id.tv_rtc_ok);
        rvMsgList = findViewById(R.id.rv_msg_list);
        viewSpace = findViewById(R.id.view_space);
        mImmersionBar.titleBar(viewSpace).init();
        tvMemberNum = findViewById(R.id.tv_member_num);
        tvLineList = findViewById(R.id.tv_line_list);
        rvLineList = findViewById(R.id.rv_line_list);
        tv_host_name = findViewById(R.id.tv_host_name);
        initLineFragment();
        logAdapter = new LogAdapter();
        rvLog.setLayoutManager(new LinearLayoutManager(this));
        logAdapter.bindToRecyclerView(rvLog);
        mAdapter = new LiveMessageAdapter();
        audioLineAdapter = new AudioLineAdapter(true);
        audioLineAdapter.setOnItemChildClickListener(this);
        rvLineList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLineList.setAdapter(audioLineAdapter);
        rvMsgList.setLayoutManager(new LinearLayoutManager(this));
        rvMsgList.setAdapter(mAdapter);
        pushURL = getIntent().getStringExtra("pushURL");
        pullURL=getIntent().getStringExtra("pullURL");
        tvTitle.setText("房间ID：" + liveId);
        nickname = ARApplication.getNickName();
        tv_host_name.setText(nickname);
        mRtmpAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });
        mRtmpAudioManager.init();
        ARRtmpcEngine.Inst().getHosterOption().setMediaType(ARVideoCommon.ARMediaType.Audio);
        mHosterKit = new ARRtmpcHosterKit(mHosterListener);
        mHosterKit.startPushRtmpStream(pushURL);
        mHosterKit.createRTCLine("", liveId, "host", getUserData(), getLiveInfo(pullURL, pullURL));
    }

    public String getLiveInfo(String pullUrl, String hlsUrl) {
        JSONObject liveInfo = new JSONObject();

        try {
            liveInfo.put("hosterId", userId);
            liveInfo.put("rtmpUrl", pullUrl);
            liveInfo.put("hlsUrl", hlsUrl);
            liveInfo.put("liveTopic", liveId);
            liveInfo.put("anyrtcId", liveId);
            liveInfo.put("isAudioLive", 1);
            liveInfo.put("hosterName", nickname);
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
                addChatMessageList(new MessageBean(MessageBean.AUDIO, nickname, text));
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamOk");
                    if (tvRtmpOk != null) {
                        tvRtmpOk.setText("RTMP连接成功");
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamReconnecting times:" + times);

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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamFailed code:" + code);
                    if (tvRtmpStatus != null) {
                        tvRtmpStatus.setText("推流失败");
                    }
                }
            });
        }

        /**
         * rtmp 推流关闭回调
         */
        @Override
        public void onRtmpStreamClosed() {
            Log.d("RTMPC", "onRtmpStreamClosed ");
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRtmpStreamClosed ");
                    if (tvRtmpStatus != null) {
                        tvRtmpStatus.setText("RTMP流关闭");
                    }
                }
            });

        }


        /**
         * RTC 连接回调
         * @param code 0： 连接成功
         */
        @Override
        public void onRTCCreateLineResult(final int code, String s) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调： onRTCCreateLineResult  code:" + code);
                    if (code == 0) {
                        if (tvRtcOk != null) {
                            tvRtcOk.setText(R.string.str_rtc_connect_success);
                        }
                    } else {
                        if (tvRtcOk != null) {
                            tvRtcOk.setText(ARUtils.getErrString(code));
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCApplyToLine  strLivePeerID:" + strLivePeerID + " strCustomID:" + strCustomID + " strUserData:" + strUserData);
                    try {
                        JSONObject jsonObject = new JSONObject(strUserData);
                        if (line_dialog != null && lineListener != null && mHosterKit != null) {
                            lineListener.AddAudioGuest(new LineBean(strLivePeerID, jsonObject.getString("nickName"), false), mHosterKit);
                            tvLineList.setSelected(true);
                        }
                        applyLineList.add(strLivePeerID);
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCCancelLine  strLivePeerID:" + strLivePeerID + "nCode:" + nCode);

                    if (nCode == 602) {
                        ToastUtil.show("连麦人数已满");
                    }
                    if (nCode == 0) {
                        if (line_dialog != null && lineListener != null) {
                            lineListener.RemoveGuest(strLivePeerID);
                        }
                        if (applyLineList.contains(strLivePeerID)) {
                            applyLineList.remove(strLivePeerID);
                        }
                        if (applyLineList.size()==0){//小红点
                            tvLineList.setSelected(false);
                        }
                    }
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调： onRTCOpenRemoteAudioLine  strLivePeerID:" + strLivePeerId + " strUserId:" + strUserId + " strUserData:" + strUserData);
                    try {
                        JSONObject jsonObject = new JSONObject(strUserData);
                        audioLineAdapter.addData(new LineBean(strLivePeerId, jsonObject.getString("nickName"), false));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onRTCCloseRemoteAudioLine(final String strLivePeerId, final String strUserId) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调： onRTCCloseRemoteAudioLine  strLivePeerID:" + strLivePeerId + " strUserId:" + strUserId);
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
        public void onRTCLocalAudioActive(final int leave) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTLocalAudioActive leave:" + leave);
                }
            });
        }


        @Override
        public void onRTCRemoteAudioActive(final String strLivePeerId, final String strUserId, final int nTime) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCAudioActive  strLivePeerID:" + strLivePeerId + " strUserId:" + strUserId + " nTime:" + nTime);
                    if (strLivePeerId.equals("RTMPC_Hoster")) {//主播

                    } else {
//                        for (int i = 0; i < audioLineAdapter.getData().size(); i++) {
//                            if (strLivePeerId.equals(audioLineAdapter.getData().get(i).peerId)) {
//                                audioLineAdapter.getItem(i).setStartAnim(true);
//                                audioLineAdapter.notifyItemChanged(i);
//                            }
//                        }
                    }


                }
            });
        }

        @Override
        public void onRTCRemoteAVStatus(final String s, boolean b, boolean b1) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCRemoteAVStatus peerID:"+s);
                }
            });
        }

        /**
         * RTC 连接关闭回调
         * @param code 207：请去AnyRTC官网申请账号,如有疑问请联系客服!
         * @param strReason
         */
        /**
         * RTC 连接关闭回调
         * @param code 207：请去AnyRTC官网申请账号,如有疑问请联系客服!
         */
        @Override
        public void onRTCLineClosed(final int code, String s) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCLineClosedLine  code:" + code);
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    printLog("回调：onRTCUserMessage  nType:" + nType + " strUserId:" + strCustomID + " strCustomName:" + strCustomName + " strCustomHeader:" + strCustomHeader + " strMessage:" + strMessage);
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
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

    public void SetLineListener(HosterActivity.LineListener mLineListener) {
        this.lineListener = mLineListener;
    }


    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (mHosterKit != null) {
            mHosterKit.hangupRTCLine(audioLineAdapter.getItem(position).peerId);
            audioLineAdapter.remove(position);
        }

    }


}

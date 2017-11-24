package org.anyrtc.hoster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.anyrtc.BaseActivity;
import org.anyrtc.adapter.AudioLineAdapter;
import org.anyrtc.adapter.LiveMessageAdapter;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LineBean;
import org.anyrtc.model.LiveBean;
import org.anyrtc.model.MessageBean;
import org.anyrtc.rtmpc_hybrid.RTMPCAudioHosterEvent;
import org.anyrtc.rtmpc_hybrid.RTMPCAudioHosterKit;
import org.anyrtc.utils.AnyRTCUtils;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.DisplayUtils;
import org.anyrtc.utils.ToastUtil;
import org.anyrtc.widgets.CircleImageView;
import org.anyrtc.widgets.CustomDialog;
import org.anyrtc.widgets.KeyboardDialogFragment;
import org.anyrtc.widgets.MemberListDialog;
import org.anyrtc.widgets.MultiCircleDrawable;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 音频主播页面
 */
public class AudioHosterActivity extends BaseActivity implements BaseQuickAdapter.OnItemChildClickListener, Chronometer.OnChronometerTickListener {

    @BindView(R.id.rl_rtmpc_videos)
    RelativeLayout rlRtmpcVideos;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_audio)
    ImageButton btnAudio;
    @BindView(R.id.btn_close)
    ImageButton btnClose;
    @BindView(R.id.rl_tool_btn)
    RelativeLayout rlToolBtn;
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
    @BindView(R.id.tv_line_list)
    ImageButton tvLineList;
    @BindView(R.id.rv_line_list)
    RecyclerView rvLineList;
    @BindView(R.id.ll_v_host)
    LinearLayout llVHost;
    @BindView(R.id.ll_hor_host)
    LinearLayout llHorHost;
    @BindView(R.id.iv_anim_v)
    ImageView ivAnimV;
    @BindView(R.id.iv_anim_h)
    ImageView ivAnimH;
    @BindView(R.id.rl_bg)
    RelativeLayout rlBg;
    @BindView(R.id.chronometer)
    Chronometer chronometer;
    @BindView(R.id.tv_host_v)
    TextView tvHostV;
    @BindView(R.id.tv_host_h)
    TextView tvHostH;
    private AudioLineAdapter audioLineAdapter;
    private RTMPCAudioHosterKit mHosterKit;
    private AnyRTCAudioManager mRtmpAudioManager = null;
    private LiveMessageAdapter mAdapter;
    private LiveBean liveBean;
    private String nickname;
    private CustomDialog line_dialog;
    private LineFragment lineFragment;
    private boolean isShowLineList = false;
    private MemberListDialog memberListDialog;
    HosterActivity.LineListener lineListener;
    private boolean isFullScreen = false;
    private String livetimes = "";
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private long time;

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


        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }

        if (mHosterKit != null) {
            mHosterKit.clear();
            mHosterKit = null;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_hoster;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initLineFragment();

        memberListDialog = new MemberListDialog();
        mImmersionBar.titleBar(viewSpace).init();
        Bundle bundle = getIntent().getExtras();
        mAdapter = new LiveMessageAdapter();
        audioLineAdapter = new AudioLineAdapter(true);
        audioLineAdapter.setOnItemChildClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvLineList.setLayoutManager(linearLayoutManager);
        rvLineList.setAdapter(audioLineAdapter);
        rvMsgList.setLayoutManager(new LinearLayoutManager(this));
        rvMsgList.setAdapter(mAdapter);
        liveBean = (LiveBean) bundle.getSerializable(Constans.LIVEBEAN);
        String liveinfo = bundle.getString(Constans.LIVEINFO);
        String userinfo = bundle.getString(Constans.USERINFO);
        nickname = liveBean.getmHostName();
        if (liveBean != null && !TextUtils.isEmpty(liveinfo) && !TextUtils.isEmpty(userinfo)) {
            tvTitle.setText(liveBean.getmLiveTopic());
            tvRoomId.setText("房间ID：" + liveBean.getmAnyrtcId());
            tvHostH.setText(nickname);
            tvHostV.setText(nickname);
            if (liveBean.getIsLiveLandscape() == 0) {
                llVHost.setVisibility(View.VISIBLE);
                rlBg.setBackgroundResource(R.drawable.audio_bg_v);
                isFullScreen = false;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                llHorHost.setVisibility(View.VISIBLE);
                rlBg.setBackgroundResource(R.drawable.bg_audio);
                isFullScreen = true;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            mRtmpAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
                @Override
                public void run() {
                    onAudioManagerChangedState();
                }
            });
            mRtmpAudioManager.init();
            mHosterKit = new RTMPCAudioHosterKit(mHosterListener, true);
            mHosterKit.startPushRtmpStream(liveBean.getmPushUrl());
            mHosterKit.createRTCLine(liveBean.getmAnyrtcId(), "host", userinfo, liveinfo);
            chronometer.setOnChronometerTickListener(this);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            time = System.currentTimeMillis();
            if (chronometer != null) {
                chronometer.start();
            }
            ivAnimV.setImageDrawable(new MultiCircleDrawable());
            ivAnimH.setImageDrawable(new MultiCircleDrawable());
        } else {
            finishAnimActivity();
        }

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
        ivMessage.setVisibility(View.GONE);
        keyboardDialogFragment.setEdittextListener(new KeyboardDialogFragment.EdittextListener() {
            @Override
            public void setTextStr(String text) {
                addChatMessageList(new MessageBean(1, nickname, text, ""));
                mHosterKit.sendUserMessage(0, nickname, "", text);
            }

            @Override
            public void dismiss(DialogFragment dialogFragment) {
                ivMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 连线弹窗
     *
     * @param context
     * @param strLivePeerID
     * @param strCustomID
     */
    private void ShowDialog(Context context, final String strLivePeerID, final String strCustomID) {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setTitle(getString(R.string.str_connect_hoster));
        build.setMessage(String.format(getString(R.string.str_apply_connect_line), strCustomID));
        build.setPositiveButton(getString(R.string.str_agree), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /**
                 * 主播接受连线请求
                 */
                mHosterKit.acceptRTCLine(strLivePeerID);
            }
        });
        build.setNegativeButton(getString(R.string.str_refused), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                /**
                 * 主播拒绝连线请求
                 */
                mHosterKit.rejectRTCLine(strLivePeerID);
            }
        });

        build.show();
    }

    private void ShowExitDialog() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle(R.string.str_exit);
        build.setMessage(R.string.str_live_stop);
        build.setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Bundle b = new Bundle();
                b.putString("HostName", nickname);
                b.putString("livetime", livetimes);
                startAnimActivity(LiveEndActivity.class, b);
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
     * 主播回调信息接口
     */
    private RTMPCAudioHosterEvent mHosterListener = new RTMPCAudioHosterEvent() {
        /**
         * rtmp连接成功
         */
        @Override
        public void onRtmpStreamOk() {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRtmpStreamOk");
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
                    Log.d("RTMPC", "onRtmpStreamReconnecting times:" + times);

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
                    Log.d("RTMPC", "onRtmpStreamStatus delayMs:" + delayMs + "netBand:" + netBand);
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
                    Log.d("RTMPC", "onRtmpStreamFailed code:" + code);
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
            if (tvRtmpStatus != null) {
                tvRtmpStatus.setText("RTMP流关闭");
            }
        }


        /**
         * RTC 连接回调
         * @param code 0： 连接成功
         */
        @Override
        public void onRTCCreateLineResult(final int code) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCreateLineResult  code:" + code);
                    if (code == 0) {
                        tvRtcOk.setText(R.string.str_rtc_connect_success);
                    } else {
                        tvRtcOk.setText(AnyRTCUtils.getErrString(code));
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
                    Log.d("RTMPC", "onRTCApplyToLine  strLivePeerID:" + strLivePeerID + " strCustomID:" + strCustomID + " strUserData:" + strUserData);
                    try {
                        JSONObject jsonObject = new JSONObject(strUserData);
                        if (line_dialog != null && lineListener != null && mHosterKit != null) {
                            lineListener.AddAudioGuest(new LineBean(strLivePeerID, jsonObject.getString("nickName"), false), mHosterKit);
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCancelLine  nCode:" + nCode + "strLivePeerID:" + strLivePeerID);

                    if (nCode == 602) {
                        ToastUtil.show("连麦人数已满");
                    }
                    if (nCode == 0) {
                        if (line_dialog != null && lineListener != null) {
                            lineListener.RemoveGuest(strLivePeerID);
                        }
                    }
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
        public void onRTCLineClosed(final int code) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCLineClosedLine  code:" + code);
                }
            });
        }


        @Override
        public void onRTCOpenAudioLine(final String strLivePeerId, final String strUserId, final String strUserData) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCOpenAudioLine  strLivePeerID:" + strLivePeerId + " strUserId:" + strUserId + " strUserData:" + strUserData);
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
        public void onRTCCloseAudioLine(final String strLivePeerId, final String strUserId) {
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCloseAudioLine  strLivePeerID:" + strLivePeerId + " strUserId:" + strUserId);
                    if (line_dialog != null && lineListener != null) {
                        lineListener.RemoveGuest(strLivePeerId);
                    }
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCAudioActive  strLivePeerID:" + strLivePeerId + " strUserId:" + strUserId + " nTime:" + nTime);
                    if (strLivePeerId.equals("RTMPC_Hoster")) {//主播

                        if (!isFullScreen) {
                            if (ivAnimV != null) {
                                MultiCircleDrawable drawable = (MultiCircleDrawable) ivAnimV.getDrawable();
                                drawable.start();
                            }
                        } else {
                            if (ivAnimH != null) {
                                MultiCircleDrawable drawable = (MultiCircleDrawable) ivAnimH.getDrawable();
                                drawable.start();
                            }
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
                    Log.d("RTMPC", "onRTCUserMessage  nType:" + nType + " strUserId:" + strCustomID + " strCustomName:" + strCustomName + " strCustomHeader:" + strCustomHeader + " strMessage:" + strMessage);
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
            AudioHosterActivity.this.runOnUiThread(new Runnable() {
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

    };

    @OnClick({R.id.btn_audio, R.id.btn_close, R.id.iv_message, R.id.tv_line_list, R.id.rl_member})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_audio:
                if (btnAudio.isSelected()) {
                    btnAudio.setSelected(false);
                    mHosterKit.setLocalAudioEnable(true);
                } else {
                    mHosterKit.setLocalAudioEnable(false);
                    btnAudio.setSelected(true);
                }
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
            case R.id.rl_member:
                if (memberListDialog != null) {
                    memberListDialog.show(getSupportFragmentManager(), "tag");
                }
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


    @Override
    public void onChronometerTick(Chronometer chronometer) {
        long tp = System.currentTimeMillis();
        livetimes = sdf.format(new Date(tp - time));
    }

}

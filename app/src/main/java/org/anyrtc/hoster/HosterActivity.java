package org.anyrtc.hoster;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.anyrtc.BaseActivity;
import org.anyrtc.adapter.LiveMessageAdapter;
import org.anyrtc.common.enums.AnyRTCRTMPCLineVideoLayout;
import org.anyrtc.common.enums.AnyRTCRTMPCVideoMode;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LineBean;
import org.anyrtc.model.LiveBean;
import org.anyrtc.model.MessageBean;
import org.anyrtc.rtmpc_hybrid.RTMPCAudioHosterKit;
import org.anyrtc.rtmpc_hybrid.RTMPCHosterKit;
import org.anyrtc.rtmpc_hybrid.RTMPCHosterVideoOption;
import org.anyrtc.rtmpc_hybrid.RTMPCHybrid;
import org.anyrtc.rtmpc_hybrid.RTMPCVideoHosterEvent;
import org.anyrtc.utils.AnyRTCUtils;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.DisplayUtils;
import org.anyrtc.utils.ToastUtil;
import org.anyrtc.widgets.CircleImageView;
import org.anyrtc.widgets.CustomDialog;
import org.anyrtc.widgets.KeyboardDialogFragment;
import org.anyrtc.widgets.MemberListDialog;
import org.anyrtc.widgets.RTMPCVideoView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 视频主播页面
 */
public class HosterActivity extends BaseActivity implements Chronometer.OnChronometerTickListener {
    @BindView(R.id.rl_rtmpc_videos)
    RelativeLayout rlRtmpcVideos;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.btn_camare)
    ImageButton btnCamare;
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
    @BindView(R.id.btn_jingxiang)
    ImageButton btnJingxiang;
    @BindView(R.id.view_space)
    View viewSpace;
    @BindView(R.id.rl_tool_btn)
    RelativeLayout rlToolBtn;
    @BindView(R.id.ll_status)
    LinearLayout llStatus;
    @BindView(R.id.iv_icon)
    CircleImageView ivIcon;
    @BindView(R.id.tv_room_id)
    TextView tvRoomId;
    @BindView(R.id.tv_member_num)
    TextView tvMemberNum;
    @BindView(R.id.tv_line_list)
    ImageButton tvLineList;
    @BindView(R.id.tv_mode_1)
    ImageButton tvMode1;
    @BindView(R.id.tv_mode_2)
    ImageButton tvMode2;
    @BindView(R.id.tv_futures)
    TextView tvFutures;
    @BindView(R.id.ll_h_futures)
    LinearLayout llHFutures;
    @BindView(R.id.chronometer)
    Chronometer chronometer;
    private RTMPCHosterKit mHosterKit;
    private RTMPCVideoView mVideoView;
    private AnyRTCAudioManager mRtmpAudioManager;
    private LiveMessageAdapter mAdapter;
    private LiveBean liveBean;
    private String nickname;
    private CustomDialog more_future_dialog;
    private CustomDialog line_dialog;
    private LineFragment lineFragment;
    private MoreFuturesFragment moreFuturesFragment;
    private boolean isShowLineList = false;
    private LineListener lineListener;
    private MoreFutureListener moreFutureListener;
    private MemberListDialog memberListDialog;
    private String livetimes = "";
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private long time;

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
            mVideoView.OnRtcRemoveLocalRender();
            mHosterKit.clear();
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
        mImmersionBar.titleBar(viewSpace).init();
        rlRtmpcVideos.setOnTouchListener(new CameraZoomListener());
        initLineFragment();
        memberListDialog = new MemberListDialog();
        tvMode1.setSelected(true);
        Bundle bundle = getIntent().getExtras();
        mAdapter = new LiveMessageAdapter();
        rvMsgList.setLayoutManager(new LinearLayoutManager(this));
        rvMsgList.setAdapter(mAdapter);
        liveBean = (LiveBean) bundle.getSerializable(Constans.LIVEBEAN);
        String liveinfo = bundle.getString(Constans.LIVEINFO);
        String userinfo = bundle.getString(Constans.USERINFO);
        nickname = liveBean.getmHostName();
        RTMPCHosterVideoOption videoOption = new RTMPCHosterVideoOption();
        //设置视频质量 参数对应清晰度，可查看API文档
        if (liveBean.getLiveMode() == 0) {
            videoOption.setmVideoMode(AnyRTCRTMPCVideoMode.RTMPC_Video_SD);
        } else if (liveBean.getLiveMode() == 1) {
            videoOption.setmVideoMode(AnyRTCRTMPCVideoMode.RTMPC_Video_1080P);
        } else {
            videoOption.setmVideoMode(AnyRTCRTMPCVideoMode.RTMPC_Video_Low);
        }
        if (liveBean != null && !TextUtils.isEmpty(liveinfo) && !TextUtils.isEmpty(userinfo)) {
            tvTitle.setText(liveBean.getmLiveTopic());
            tvRoomId.setText("房间ID:" + liveBean.getmAnyrtcId());

            //设置横竖屏
            if (liveBean.getIsLiveLandscape() == 0) {
                tvMode1.setVisibility(View.GONE);
                tvMode2.setVisibility(View.GONE);
                videoOption.setmScreenOriention(AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                tvFutures.setVisibility(View.GONE);
                llHFutures.setVisibility(View.VISIBLE);
                videoOption.setmScreenOriention(AnyRTCScreenOrientation.AnyRTC_SCRN_Landscape);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            //音频管理对象  当靠近听筒时将会减小音量
            mRtmpAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
                @Override
                public void run() {
                    onAudioManagerChangedState();
                }
            });
            mRtmpAudioManager.init();
            //实例化主播对象
            mHosterKit = new RTMPCHosterKit(mHosterListener, videoOption);
            //实例化连麦窗口对象
            mVideoView = new RTMPCVideoView(this, rlRtmpcVideos, RTMPCHybrid.Inst().egl(), true, RTMPCVideoView.RTMPCVideoLayout.RTMPC_V_1X3);
            mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);
            //设置本地视频采集
            VideoRenderer render = mVideoView.OnRtcOpenLocalRender(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            mHosterKit.setLocalVideoCapturer(render.GetRenderPointer());
            //设置录像URL，必须在开始推流前进行设置
            mHosterKit.setRtmpRecordUrl(liveBean.getmRtmpPullUrl());
            //开始推流
            mHosterKit.startPushRtmpStream(liveBean.getmPushUrl());
            //创建RTC连接，必须放在开始推流之后
            mHosterKit.createRTCLine(liveBean.getmAnyrtcId(), "host", userinfo, liveinfo);
            //设置音频连麦直播，默认视频
            chronometer.setOnChronometerTickListener(this);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            time = System.currentTimeMillis();
            if (chronometer != null) {
                chronometer.start();
            }
            initFuturesFragment();
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
     * 连线时小图标的关闭连接按钮及切换摄像头按钮
     */
    private RTMPCVideoView.BtnVideoCloseEvent mBtnVideoCloseEvent = new RTMPCVideoView.BtnVideoCloseEvent() {

        @Override
        public void CloseVideoRender(View view, String strPeerId) {
            /**
             * 挂断连线
             */
            mHosterKit.hangupRTCLine(strPeerId);
        }

        @Override
        public void OnSwitchCamera(View view) {
            /**
             * 切换摄像头
             */
            mHosterKit.switchCamera();
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
    private RTMPCVideoHosterEvent mHosterListener = new RTMPCVideoHosterEvent() {
        /**
         * rtmp连接成功
         */
        @Override
        public void onRtmpStreamOk() {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRtmpStreamOk");
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
                    Log.d("RTMPC", "onRtmpStreamReconnecting times:" + times);
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
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRtmpStreamFailed code:" + code);
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
            Log.d("RTMPC", "onRtmpStreamClosed ");
            finish();
        }

        /**
         * RTC 连接回调
         * @param code 0： 连接成功
         */
        @Override
        public void onRTCCreateLineResult(final int code) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCreateLineResult  code:" + code);
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
                    Log.d("RTMPC", "onRTCApplyToLine  strLivePeerID:" + strLivePeerID + " strCustomID:" + strCustomID + " strUserData:" + strUserData);
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
                    Log.d("RTMPC", "onRTCCancelLine  strLivePeerID:" + strLivePeerID + "nCode:" + nCode);
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


        /**
         * RTC 连接关闭回调
         * @param code 207：请去AnyRTC官网申请账号,如有疑问请联系客服!
         */
        @Override
        public void onRTCLineClosed(final int code) {
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
        @Override
        public void onRTCOpenVideoRender(final String strLivePeerId, final String strPublishId, final String strUserId, final String strUserData) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCOpenVideoRender  strPublishId:" + strPublishId + " strUserId:" + strUserId + " strUserData:" + strUserData);
                    final VideoRenderer render = mVideoView.OnRtcOpenRemoteRender(strLivePeerId, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                    if (null != render) {
                        mHosterKit.setRTCVideoRender(strPublishId, render.GetRenderPointer());
                    }
                }
            });
        }


        /**
         * 连线关闭时的视频图像回调；
         */
        @Override
        public void onRTCCloseVideoRender(final String strLivePeerId,final String strPublishId, final String strUserId) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCCloseVideoRender  strPublishId:" + strPublishId + " strUserId:" + strUserId);
                    mHosterKit.setRTCVideoRender(strPublishId, 0);
                    mVideoView.OnRtcRemoveRemoteRender(strLivePeerId);
                    if (line_dialog != null && lineListener != null) {
                        lineListener.RemoveGuest(strLivePeerId);
                    }
                }
            });
        }


        /**
         * 音频连麦接通
         * @param strLivePeerId  RTC服务生成的代表连麦人的ID
         * @param strUserId 连麦者自定义ID
         * @param strUserData  连麦者自定义数据
         */
        @Override
        public void onRTCOpenAudioLine(String strLivePeerId, String strUserId, String strUserData) {

        }

        /**
         * 音频连麦关闭
         * @param strLivePeerId RTC服务生成的代表连麦人的ID
         * @param strUserId 连麦者自定义ID
         */
        @Override
        public void onRTCCloseAudioLine(String strLivePeerId, String strUserId) {

        }


        @Override
        public void onRTCAudioActive(final String strLivePeerId, final String strUserId, final int nTime) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTMPC", "onRTCAudioActive  strLivePeerID:" + strLivePeerId + " strUserId:" + strUserId + " nTime:" + nTime);
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
            HosterActivity.this.runOnUiThread(new Runnable() {
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
            HosterActivity.this.runOnUiThread(new Runnable() {
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

        @Override
        public void onRTCLanScreenFound(final String strPeerScrnId, String strName, String strPlatform) {
            HosterActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHosterKit.connectPeerScreen(strPeerScrnId);
                }
            });
        }

        @Override
        public void onRTCLanScreenClosed(String strPeerScrnId) {

        }
    };


    @OnClick({R.id.btn_jingxiang, R.id.btn_camare, R.id.btn_audio, R.id.btn_video, R.id.btn_close, R.id.iv_message, R.id.tv_line_list, R.id.tv_mode_1, R.id.tv_mode_2, R.id.tv_futures, R.id.rl_member})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camare:
                if (mHosterKit == null) {
                    return;
                }

                mHosterKit.switchCamera();
                if (btnCamare.isSelected()) {
                    btnCamare.setSelected(false);
                } else {
                    btnCamare.setSelected(true);
                }
                break;
            case R.id.btn_audio:
                if (mHosterKit == null) {
                    return;
                }
                if (btnAudio.isSelected()) {
                    btnAudio.setSelected(false);
                    mHosterKit.setLocalAudioEnable(true);
                } else {
                    mHosterKit.setLocalAudioEnable(false);
                    btnAudio.setSelected(true);
                }
                break;
            case R.id.btn_video:
                if (mHosterKit == null) {
                    return;
                }
                if (btnVideo.isSelected()) {
                    btnVideo.setSelected(false);
                    mHosterKit.setLocalVideoEnable(true);
                } else {
                    mHosterKit.setLocalVideoEnable(false);
                    btnVideo.setSelected(true);
                }
                break;
            case R.id.btn_close:
                ShowExitDialog();
                break;
            case R.id.iv_message:
                showChatLayout();
                break;
            case R.id.btn_jingxiang:
                if (mHosterKit == null) {
                    return;
                }
                if (btnJingxiang.isSelected()) {
                    btnJingxiang.setSelected(false);
                    RTMPCHybrid.Inst().setFrontCameraMirrorEnable(false);
                } else {
                    RTMPCHybrid.Inst().setFrontCameraMirrorEnable(true);
                    btnJingxiang.setSelected(true);
                }
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
            case R.id.tv_mode_1:
                if (tvMode2.isSelected()) {
                    tvMode2.setSelected(false);
                    tvMode1.setSelected(true);
                    mVideoView.changeTem(RTMPCVideoView.RTMPCVideoLayout.RTMPC_V_1X3);
                    mHosterKit.setMixVideoModel(AnyRTCRTMPCLineVideoLayout.RTMPC_LINE_V_1big_3small);
                }
                break;
            case R.id.tv_mode_2:
                if (tvMode1.isSelected()) {
                    tvMode2.setSelected(true);
                    tvMode1.setSelected(false);
                    mVideoView.changeTem(RTMPCVideoView.RTMPCVideoLayout.RTMPC_V_2X2);
                    mHosterKit.setMixVideoModel(AnyRTCRTMPCLineVideoLayout.RTMPC_LINE_V_1_equal_others);
                }
                break;
            case R.id.tv_futures:
                more_future_dialog.show();
                moreFutureListener.setRTMPCHosterKit(mHosterKit);
                break;
            case R.id.rl_member:
                if (memberListDialog != null) {
                    memberListDialog.show(getSupportFragmentManager(), "tag");
                }
                break;
        }
    }

    private void initFuturesFragment() {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.item_more_futures)
                .setAnimId(R.style.AnimBottom)
                .setGravity(Gravity.BOTTOM)
                .setLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, DisplayUtils.getScreenHeightPixels(this) / 3)
                .setBackgroundDrawable(true)
                .build();
        more_future_dialog = builder.show(new CustomDialog.Builder.onInitListener() {
            @Override
            public void init(CustomDialog view) {
                if (moreFuturesFragment == null) {
                    moreFuturesFragment = new MoreFuturesFragment();
                }
            }
        });
        more_future_dialog.hide();
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


    @Override
    public void onChronometerTick(Chronometer chronometer) {
        long tp = System.currentTimeMillis();
        livetimes = sdf.format(new Date(tp - time));
    }


    public interface LineListener {
        void AddAudioGuest(LineBean lineBean, RTMPCAudioHosterKit hosterKit);//添加音频游客的申请到列表

        void AddGuest(LineBean lineBean, RTMPCHosterKit hosterKit);//添加游客的申请到列表

        void RemoveGuest(String peerid);//从列表移除游客
    }

    public interface MoreFutureListener {
        void setRTMPCHosterKit(RTMPCHosterKit hosterKit);//把对象带过去
    }

    public void SetMoreFutureListener(MoreFutureListener moreFutureListener) {
        this.moreFutureListener = moreFutureListener;
    }

    public void SetLineListener(LineListener mLineListener) {
        this.lineListener = mLineListener;
    }

    public void closeLineDialog() {
        if (line_dialog != null) {
            line_dialog.hide();
        }
    }

    //缩放调焦
    //调焦
    float oldDist = 1f;
    private final int DOUBLE_TAP_TIMEOUT = 200;
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    public class CameraZoomListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mPreviousUpEvent != null
                            && mCurrentDownEvent != null
                            && isConsideredDoubleTap(mCurrentDownEvent,
                            mPreviousUpEvent, event)) {
                        if (mHosterKit != null && mHosterKit.isZoomSupported()) {
                            mHosterKit.setCameraZoom(0);
                        }
                    }
                    mCurrentDownEvent = MotionEvent.obtain(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mPreviousUpEvent = MotionEvent.obtain(event);
                }

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = getFingerSpacing(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float newDist = getFingerSpacing(event);
                        if (newDist > oldDist) {
                            handleZoom(true);
                        } else if (newDist < oldDist) {
                            handleZoom(false);
                        }
                        oldDist = newDist;
                        break;
                }
            } catch (Exception e) {
                Log.e("error", "出错");
            }
            return true;
        }
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown,
                                          MotionEvent firstUp, MotionEvent secondDown) {
        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }
        int deltaX = (int) firstUp.getX() - (int) secondDown.getX();
        int deltaY = (int) firstUp.getY() - (int) secondDown.getY();
        return deltaX * deltaX + deltaY * deltaY < 10000;
    }

    private void handleZoom(boolean isZoomIn) {
        if (mHosterKit == null) {
            return;
        }
        if (mHosterKit.isZoomSupported()) {
            int maxZoom = mHosterKit.getCameraMaxZoom();
            Log.d("焦距", maxZoom + "//最大焦距");
            int zoom = mHosterKit.getCameraZoom();
            Log.d("焦距", maxZoom + "//当前焦距");
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            mHosterKit.setCameraZoom(zoom);
        }
    }


    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}

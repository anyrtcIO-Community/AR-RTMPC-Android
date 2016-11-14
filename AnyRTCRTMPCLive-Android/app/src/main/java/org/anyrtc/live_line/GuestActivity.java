package org.anyrtc.live_line;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;

import org.anyrtc.adapter.LiveChatAdapter;
import org.anyrtc.application.HybirdApplication;
import org.anyrtc.rtmpc_hybrid.RTMPCAbstractGuest;
import org.anyrtc.rtmpc_hybrid.RTMPCGuestKit;
import org.anyrtc.rtmpc_hybrid.RTMPCHybird;
import org.anyrtc.rtmpc_hybrid.RTMPCVideoView;
import org.anyrtc.utils.ChatMessageBean;
import org.anyrtc.utils.RTMPAudioManager;
import org.anyrtc.utils.RTMPUrlHelper;
import org.anyrtc.utils.ShareHelper;
import org.anyrtc.utils.SoftKeyboardUtil;
import org.anyrtc.utils.ThreadUtil;
import org.anyrtc.widgets.ScrollRecycerView;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * 游客页面
 */
public class GuestActivity extends AppCompatActivity implements ScrollRecycerView.ScrollPosation {
    private static final int CLOSED = 0;
    private RTMPCGuestKit mGuestKit;
    private RTMPCVideoView mVideoView;
    private Button mBtnConnect;
    private boolean mStartLine = false;
    private ShareHelper mShareHelper;

    private String mNickname;
    private String mRtmpPullUrl;
    private String mAnyrtcId;
    private String mHlsUrl;
    private String mGuestId;
    private JSONObject mUserData;
    private String mTopic;

    private SoftKeyboardUtil softKeyboardUtil;

    private int duration = 100;//软键盘延迟打开时间
    private boolean isKeybord = false;

    private Switch mCheckBarrage;
    private DanmakuView mDanmakuView;
    private EditText editMessage;
    private ViewAnimator vaBottomBar;
    private LinearLayout llInputSoft;
    private FrameLayout flChatList;
    private ScrollRecycerView rcLiveChat;

    private ImageView btnChat;

    private List<ChatMessageBean> mChatMessageList;
    private LiveChatAdapter mChatLiveAdapter;

    private int maxMessageList = 150; //列表中最大 消息数目

    private RTMPAudioManager mRtmpAudioManager = null;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CLOSED: {
                    mGuestKit.HangupRTCLine();
                    mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                    mStartLine = false;
                    finish();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_guest);
        mChatMessageList = new ArrayList<ChatMessageBean>();
        mShareHelper = new ShareHelper(this);
        mBtnConnect = (Button) findViewById(R.id.btn_line);

        mNickname = ((HybirdApplication)HybirdApplication.app()).getmNickname();
        mRtmpPullUrl = getIntent().getExtras().getString("rtmp_url");
        mAnyrtcId = getIntent().getExtras().getString("anyrtcId");
        mHlsUrl = getIntent().getExtras().getString("hls_url");
        mGuestId = mNickname;//getIntent().getExtras().getString("guestId");

        mTopic = getIntent().getExtras().getString("topic");
        setTitle(mTopic);
        ((TextView) findViewById(R.id.txt_title)).setText(mTopic);
        mCheckBarrage = (Switch) findViewById(R.id.check_barrage);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);
        editMessage = (EditText) findViewById(R.id.edit_message);
        vaBottomBar = (ViewAnimator) findViewById(R.id.va_bottom_bar);
        llInputSoft = (LinearLayout) findViewById(R.id.ll_input_soft);
        flChatList = (FrameLayout) findViewById(R.id.fl_chat_list);
        btnChat = (ImageView) findViewById(R.id.iv_host_text);

        rcLiveChat = (ScrollRecycerView) findViewById(R.id.rc_live_chat);
        mChatLiveAdapter = new LiveChatAdapter(mChatMessageList, this);
        rcLiveChat.setLayoutManager(new LinearLayoutManager(this));
        rcLiveChat.setAdapter(mChatLiveAdapter);
        rcLiveChat.addScrollPosation(this);
        setEditTouchListener();
        vaBottomBar.setAnimateFirstView(true);

        //设置横屏模式，也可sdk初始化时进行设置
        //RTMPCHybird.Inst().SetScreenToLandscape();
        mVideoView = new RTMPCVideoView((RelativeLayout) findViewById(R.id.rl_rtmpc_videos), RTMPCHybird.Inst().Egl(), false);

        mVideoView.setBtnCloseEvent(mBtnVideoCloseEvent);

        {
            // Create and audio manager that will take care of audio routing,
            // audio modes, audio device enumeration etc.
            mRtmpAudioManager = RTMPAudioManager.create(this, new Runnable() {
                // This method will be called each time the audio state (number
                // and
                // type of devices) has been changed.
                @Override
                public void run() {
                    onAudioManagerChangedState();
                }
            });
            // Store existing audio settings and change audio mode to
            // MODE_IN_COMMUNICATION for best possible VoIP performance.
            mRtmpAudioManager.init();
        }

        mUserData = new JSONObject();
        try {
            mUserData.put("nickName", mNickname);
            mUserData.put("headUrl", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**
         * 初始化rtmp播放器
         */
        mGuestKit = new RTMPCGuestKit(this, mGuestListener);
        VideoRenderer render = mVideoView.OnRtcOpenLocalRender(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        /**
         * 开始播放rtmp流
         */
        mGuestKit.StartRtmpPlay(mRtmpPullUrl, render.GetRenderPointer());
        /**
         * 开启RTC连线连接
         */
        mGuestKit.JoinRTCLine(mAnyrtcId, mGuestId, mUserData.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDanmakuView.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDanmakuView.hide();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if(mStartLine) {
                ShowExitDialog();
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmakuView.clear();
        softKeyboardUtil.removeGlobalOnLayoutListener(this);
        // Close RTMPAudioManager
        if (mRtmpAudioManager != null) {
            mRtmpAudioManager.close();
            mRtmpAudioManager = null;

        }

        /**
         * 销毁rtmp播放器
         */
        if (mGuestKit != null) {
            mGuestKit.Clear();
            mVideoView.OnRtcRemoveLocalRender();
            mGuestKit = null;
        }
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
                mGuestKit.HangupRTCLine();
                mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                mStartLine = false;
                finish();
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

    public void OnBtnClicked(View btn) {
        if(btn.getId() == R.id.btn_copy_hls) {
            int index = mHlsUrl.lastIndexOf("/");
            int lastIndex = mHlsUrl.lastIndexOf(".");
            String shareUrl = String.format(RTMPUrlHelper.SHARE_WEB_URL, mHlsUrl.substring(index + 1, lastIndex));
            mShareHelper.shareWeiXin(mTopic, shareUrl);
        } else if (btn.getId() == R.id.btn_line) {
            if (!mStartLine) {

                JSONObject json = new JSONObject();
                try {
                    json.put("guestId", mNickname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /**
                 * 向主播申请连线
                 */
                mGuestKit.ApplyRTCLine(json.toString());
                mStartLine = true;
                mBtnConnect.setText(R.string.str_hangup_connect);
            } else {
                /**
                 * 挂断连线
                 */
                mGuestKit.HangupRTCLine();
                mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                mStartLine = false;
                mBtnConnect.setText(R.string.str_connect_hoster);
            }
        } else if (btn.getId() == R.id.btn_send_message) {
            String message = editMessage.getText().toString();
            editMessage.setText("");
            if (message.equals("")) {
                return;
            }
            if (mCheckBarrage.isChecked()) {
                mGuestKit.SendBarrage(mNickname, "", message);
                IDanmakuItem item = new DanmakuItem(GuestActivity.this, new SpannableString(message), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
                mDanmakuView.addItemToHead(item);
            } else {
                mGuestKit.SendUserMsg(mNickname, "", message);
            }
            addChatMessageList(new ChatMessageBean(mNickname, mNickname, "", message));
        } else if (btn.getId() == R.id.iv_host_text) {
            btnChat.clearFocus();
            vaBottomBar.setDisplayedChild(1);
            editMessage.requestFocus();
            softKeyboardUtil.showKeyboard(GuestActivity.this, editMessage);
        }
    }

    /**
     * 设置 键盘的监听事件
     */
    private void setEditTouchListener() {
        softKeyboardUtil = new SoftKeyboardUtil();

        softKeyboardUtil.observeSoftKeyboard(GuestActivity.this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
            @Override
            public void onSoftKeyBoardChange(int softKeybardHeight, boolean isShow) {
                if (isShow) {
                    ThreadUtil.runInUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isKeybord) {
                                isKeybord = true;
                                llInputSoft.animate().translationYBy(-editMessage.getHeight() / 3).setDuration(100).start();
                                flChatList.animate().translationYBy(-editMessage.getHeight() / 3).setDuration(100).start();
                            }

                        }
                    }, duration);
                } else {
                    btnChat.requestFocus();
                    vaBottomBar.setDisplayedChild(0);
                    llInputSoft.animate().translationYBy(editMessage.getHeight() / 3).setDuration(100).start();
                    flChatList.animate().translationYBy(editMessage.getHeight() / 3).setDuration(100).start();
                    isKeybord = false;
                }
            }
        });
    }

    /**
     * 更新列表
     * @param chatMessageBean
     */
    private void addChatMessageList(ChatMessageBean chatMessageBean) {
        // 150 条 修改；

        if (mChatMessageList == null) {
            return;
        }

        if (mChatMessageList.size() < maxMessageList) {
            mChatMessageList.add(chatMessageBean);
        } else {
            mChatMessageList.remove(0);
            mChatMessageList.add(chatMessageBean);
        }
        mChatLiveAdapter.notifyDataSetChanged();
        rcLiveChat.smoothScrollToPosition(mChatMessageList.size() - 1);
    }


    @Override
    public void ScrollButtom() {
    }

    @Override
    public void ScrollNotButtom() {

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
            mGuestKit.HangupRTCLine();
            mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
            mStartLine = false;
            mBtnConnect.setText(R.string.str_connect_hoster);
        }

        @Override
        public void OnSwitchCamera(View view) {
            /**
             * 连线时切换游客摄像头
             */
            mGuestKit.SwitchCamera();
        }
    };

    /**
     * 观看直播回调信息接口
     */
    private RTMPCAbstractGuest mGuestListener = new RTMPCAbstractGuest() {

        /**
         * rtmp 连接成功
         */
        @Override
        public void OnRtmplayerOKCallback() {
        }

        /**
         * rtmp 当前播放状态
         * @param cacheTime 当前缓存时间
         * @param curBitrate 当前播放器码流
         */
        @Override
        public void OnRtmplayerStatusCallback(int cacheTime, int curBitrate) {

        }

        /**
         * rtmp 播放缓冲区时长
         * @param time 缓冲时间
         */
        @Override
        public void OnRtmplayerCacheCallback(int time) {

        }

        /**
         * rtmp 播放器关闭
         * @param errcode
         */
        @Override
        public void OnRtmplayerClosedCallback(int errcode) {

        }

        /**
         * 游客RTC 状态回调
         * @param code 回调响应码：0：正常；101：主播未开启直播；
         * @param strReason 原因描述
         */
        @Override
        public void OnRTCJoinLineResultCallback(final int code, String strReason) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {

                    } else if (code == 101) {
                        Toast.makeText(GuestActivity.this, R.string.str_hoster_not_live, Toast.LENGTH_LONG).show();
                        mHandler.sendEmptyMessageDelayed(CLOSED, 2000);
                    }
                }
            });
        }

        /**
         * 游客申请连线回调
         * @param code 0：申请连线成功；-1：主播拒绝连线
         */
        @Override
        public void OnRTCApplyLineResultCallback(final int code) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        VideoRenderer render = mVideoView.OnRtcOpenRemoteRender("LocalCameraRender", RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                        mGuestKit.SetVideoCapturer(render.GetRenderPointer(), true);
                    } else if (code == -1) {
                        Toast.makeText(GuestActivity.this, R.string.str_hoster_refused, Toast.LENGTH_LONG).show();
                        mStartLine = false;
                        mBtnConnect.setText(R.string.str_connect_hoster);
                    }
                }
            });
        }

        /**
         * 当与主播连线成功时，其他用户连线回调
         * @param strLivePeerID
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCOtherLineOpenCallback(String strLivePeerID, String strCustomID, String strUserData) {

        }

        /**
         * 其他用户连线回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCOtherLineCloseCallback(String strLivePeerID) {

        }

        /**
         * 挂断连线回调
         */
        @Override
        public void OnRTCHangupLineCallback() {
            //主播连线断开
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGuestKit.HangupRTCLine();
                    mVideoView.OnRtcRemoveRemoteRender("LocalCameraRender");
                    mStartLine = false;
                    mBtnConnect.setText(R.string.str_connect_hoster);
                }
            });
        }

        /**
         * 主播已离开回调
         * @param code
         * @param strReason
         */
        @Override
        public void OnRTCLineLeaveCallback(int code, String strReason) {
            //主播关闭直播
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GuestActivity.this, R.string.str_hoster_leave, Toast.LENGTH_LONG).show();
                    mHandler.sendEmptyMessageDelayed(CLOSED, 2000);
                }
            });
        }

        /**
         * 连线接通后回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCOpenVideoRenderCallback(final String strLivePeerID) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final VideoRenderer render = mVideoView.OnRtcOpenRemoteRender(strLivePeerID, RendererCommon.ScalingType.SCALE_ASPECT_FIT);
                    mGuestKit.SetRTCVideoRender(strLivePeerID, render.GetRenderPointer());
                }
            });
        }

        /**
         * 连线关闭后图像回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCloseVideoRenderCallback(final String strLivePeerID) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGuestKit.SetRTCVideoRender(strLivePeerID, 0);
                    mVideoView.OnRtcRemoveRemoteRender(strLivePeerID);
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
        public void OnRTCUserMessageCallback(final String strCustomID, final String strCustomName, final String strCustomHeader, final String strMessage) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, "", strMessage));
                }
            });
        }

        /**
         * 弹幕回调
         * @param strCustomID 弹幕的发送者id
         * @param strCustomName 弹幕的发送者昵称
         * @param strCustomHeader 弹幕的发送者头像url
         * @param strBarrage 弹幕的内容
         */
        @Override
        public void OnRTCUserBarrageCallback(final String strCustomID, final String strCustomName, final String strCustomHeader, final String strBarrage) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, "", strBarrage));
                    IDanmakuItem item = new DanmakuItem(GuestActivity.this, new SpannableString(strBarrage), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
                    mDanmakuView.addItemToHead(item);
                }
            });
        }

        /**
         * 观看直播的总人数回调
         * @param totalMembers 观看直播的总人数
         */
        @Override
        public void OnRTCMemberListWillUpdateCallback(final int totalMembers) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.txt_watcher_number)).setText(String.format(getString(R.string.str_live_watcher_number), totalMembers));
                }
            });
        }

        /**
         * 人员上下线回调
         * @param strCustomID
         * @param strUserData
         */
        @Override
        public void OnRTCMemberCallback(final String strCustomID, final String strUserData) {
            GuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject userData = new JSONObject(strUserData);
                        addChatMessageList(new ChatMessageBean(userData.getString("nickName"), "", userData.getString("headUrl"), ""));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        /**
         * 直播观看总人数回调结束
         */
        @Override
        public void OnRTCMemberListUpdateDoneCallback() {

        }
    };
}

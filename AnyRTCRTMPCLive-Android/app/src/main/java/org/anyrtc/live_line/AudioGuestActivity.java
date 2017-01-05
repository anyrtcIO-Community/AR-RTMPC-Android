package org.anyrtc.live_line;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;

import org.anyrtc.adapter.GuestAdapter;
import org.anyrtc.adapter.LiveAudioAdapter;
import org.anyrtc.adapter.LiveChatAdapter;
import org.anyrtc.application.HybirdApplication;
import org.anyrtc.rtmpc_hybrid.RTMPCAbstractGuest;
import org.anyrtc.rtmpc_hybrid.RTMPCGuestKit;
import org.anyrtc.rtmpc_hybrid.RTMPCHybird;
import org.anyrtc.rtmpc_hybrid.RTMPCVideoView;
import org.anyrtc.utils.AudioItemBean;
import org.anyrtc.utils.ChatMessageBean;
import org.anyrtc.utils.RTMPAudioManager;
import org.anyrtc.utils.RTMPCHttpSDK;
import org.anyrtc.utils.RTMPUrlHelper;
import org.anyrtc.utils.ShareHelper;
import org.anyrtc.utils.SoftKeyboardUtil;
import org.anyrtc.utils.ThreadUtil;
import org.anyrtc.widgets.DiffuseView;
import org.anyrtc.widgets.HorizontalListView;
import org.anyrtc.widgets.ScrollRecycerView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频游客界面
 */
public class AudioGuestActivity extends AppCompatActivity implements ScrollRecycerView.ScrollPosation {
    private static final int CLOSED = 0;
    private RTMPCGuestKit mGuestKit;
    private RTMPCVideoView mVideoView;
    private Button mBtnConnect;
    private boolean mStartLine = false;
    private ShareHelper mShareHelper;

    private String mNickname;
    private String mRtmpPullUrl;
    private String mAnyrtcId;
    private String mHostId;
    private String mHlsUrl;
    private String mGuestId;
    private JSONObject mUserData;
    private String mTopic;

    private SoftKeyboardUtil softKeyboardUtil;

    private int duration = 100;//软键盘延迟打开时间
    private boolean isKeybord = false;

    private Switch mCheckBarrage;
    private DanmakuView mDanmakuView;
    private DiffuseView mDiffView;
    private EditText editMessage;
    private ViewAnimator vaBottomBar;
    private LinearLayout llInputSoft;
    private FrameLayout flChatList;
    private ScrollRecycerView rcLiveChat;

    private ImageView btnChat;

    private ListView mListAudioChat;
    private HorizontalListView mListGuest;
    private List<AudioItemBean> mAudioChatList;
    private List<AudioItemBean> mGuestList;
    private List<ChatMessageBean> mChatMessageList;
    private LiveChatAdapter mChatLiveAdapter;

    private LiveAudioAdapter mAudioChatLiveAdapter;
    private GuestAdapter mGuestAdapter;

    private int maxMessageList = 150; //列表中最大 消息数目

    private RTMPAudioManager mRtmpAudioManager = null;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CLOSED: {
                    mGuestKit.HangupRTCLine();
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
        setContentView(R.layout.activity_audio_guest);
        mChatMessageList = new ArrayList<ChatMessageBean>();
        mAudioChatList = new ArrayList<AudioItemBean>();
        mGuestList = new ArrayList<AudioItemBean>();
        mShareHelper = new ShareHelper(this);
        mBtnConnect = (Button) findViewById(R.id.btn_line);

        mNickname = ((HybirdApplication) HybirdApplication.app()).getmNickname();
        mRtmpPullUrl = getIntent().getExtras().getString("rtmp_url");
        mAnyrtcId = getIntent().getExtras().getString("anyrtcId");
        mHlsUrl = getIntent().getExtras().getString("hls_url");
        mHostId = getIntent().getExtras().getString("hosterId");
        mGuestId = RTMPCHttpSDK.getRandomString(9);

        mTopic = getIntent().getExtras().getString("topic");
        setTitle(mTopic);
        ((TextView) findViewById(R.id.txt_title)).setText(mTopic);
        mDiffView = (DiffuseView)findViewById(R.id.wl_content);
        mCheckBarrage = (Switch) findViewById(R.id.check_barrage);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);
        editMessage = (EditText) findViewById(R.id.edit_message);
        vaBottomBar = (ViewAnimator) findViewById(R.id.va_bottom_bar);
        llInputSoft = (LinearLayout) findViewById(R.id.ll_input_soft);
        flChatList = (FrameLayout) findViewById(R.id.fl_chat_list);
        btnChat = (ImageView) findViewById(R.id.iv_host_text);

        rcLiveChat = (ScrollRecycerView) findViewById(R.id.rc_live_chat);
        mListAudioChat = (ListView) findViewById(R.id.rc_audio_chat);
        mListGuest = (HorizontalListView) findViewById(R.id.rc_audio_chat_guest);
        mChatLiveAdapter = new LiveChatAdapter(mChatMessageList, this);
        rcLiveChat.setLayoutManager(new LinearLayoutManager(this));
        rcLiveChat.setAdapter(mChatLiveAdapter);
        rcLiveChat.addScrollPosation(this);

        mAudioChatLiveAdapter = new LiveAudioAdapter(mAudioChatList, this, mCloseAudioHelper, false, mHostId);
        mListAudioChat.setAdapter(mAudioChatLiveAdapter);

        mGuestAdapter = new GuestAdapter(mGuestList, this);
        mListGuest.setAdapter(mGuestAdapter);

        setEditTouchListener();
        vaBottomBar.setAnimateFirstView(true);

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
            mUserData.put("NickName", mNickname);
            mUserData.put("IconUrl", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /**
         * 设置音频模式并且监测音频大小
         */
        RTMPCHybird.Inst().SetLiveToAuidoOnly(true, true);

        /**
         * 初始化rtmp播放器
         */
        mGuestKit = new RTMPCGuestKit(this, mGuestListener);
        /**
         * 开始播放rtmp流
         */
        mGuestKit.StartRtmpPlay(mRtmpPullUrl, 0);
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mStartLine) {
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
        if (btn.getId() == R.id.btn_copy_hls) {
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
                IDanmakuItem item = new DanmakuItem(AudioGuestActivity.this, new SpannableString(message), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
                mDanmakuView.addItemToHead(item);
            } else {
                mGuestKit.SendUserMsg(mNickname, "", message);
            }
            addChatMessageList(new ChatMessageBean(mNickname, mNickname, "", message));
        } else if (btn.getId() == R.id.iv_host_text) {
            btnChat.clearFocus();
            vaBottomBar.setDisplayedChild(1);
            editMessage.requestFocus();
            softKeyboardUtil.showKeyboard(AudioGuestActivity.this, editMessage);
        }
    }

    /**
     * 设置 键盘的监听事件
     */
    private void setEditTouchListener() {
        softKeyboardUtil = new SoftKeyboardUtil();

        softKeyboardUtil.observeSoftKeyboard(AudioGuestActivity.this, new SoftKeyboardUtil.OnSoftKeyboardChangeListener() {
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
     *
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
            mStartLine = false;
            mBtnConnect.setText(R.string.str_connect_hoster);
        }

        @Override
        public void OnSwitchCamera(View view) {

        }
    };

    /**
     * 音频连麦人员添加到连麦列表中
     * @param strLivePeerID
     * @param strCustomID
     */
    private void addAudioChatList(String strLivePeerID, String strCustomID) {
        if(!strCustomID.equals(mGuestId)) {
            AudioItemBean audioBean = new AudioItemBean();
            audioBean.setmStrCustomid(strCustomID);
            audioBean.setmStrPeerId(strLivePeerID);
            mAudioChatList.add(audioBean);
            mAudioChatLiveAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新语音连麦人员信息
     * @param customid
     * @param level
     */
    private void updateAudioChatLevel(String customid, int level) {
        if(null == mAudioChatList) {
            mAudioChatList = new ArrayList<AudioItemBean>();
        }
        for(int i = 0; i < mAudioChatList.size(); i ++) {
            if(customid.equals(mAudioChatList.get(i).getmStrCustomid())) {
                mAudioChatList.get(i).setmAudioLevel(level);
                break;
            }
        }

        for(int i = 0; i < mGuestList.size(); i ++) {
            if(customid.equals(mGuestList.get(i).getmStrCustomid())) {
                mGuestList.get(i).setmAudioLevel(level);
                break;
            }
        }
        mAudioChatLiveAdapter.notifyDataSetChanged();
        mGuestAdapter.notifyDataSetChanged();
    }

    /**
     * 连麦挂断后更新连麦人数列表
     * @param strPeerid
     */
    private void removeAudioChatLevel(String strPeerid) {
        for(int i = 0; i < mAudioChatList.size(); i ++) {
            if(strPeerid.equals(mAudioChatList.get(i).getmStrPeerId())) {
                mAudioChatList.remove(i);
                break;
            }
        }
        mAudioChatLiveAdapter.notifyDataSetChanged();
    }

    /**
     * 音频连麦时的关闭连麦回调
     */
    private LiveAudioAdapter.CloseAudioHelper mCloseAudioHelper = new LiveAudioAdapter.CloseAudioHelper() {
        @Override
        public void onCloseAudioChat(String strLivePeerid) {
            mGuestKit.HangupRTCLine();
            removeAudioChatLevel(strLivePeerid);
        }
    };

    /**
     * 观看直播回调信息接口
     */
    private RTMPCAbstractGuest mGuestListener = new RTMPCAbstractGuest() {

        /**
         * rtmp 连接成功 音频即将播放；音频播放前的操作可以在此接口中进行操作
         */
        @Override
        public void OnRtmplayerOKCallback() {

        }

        /**
         * rtmp 开始播放 视频开始播放
         */
        @Override
        public void OnRtmplayerStartCallback() {

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
         * rtmp 音频模式下，音频实时检测
         */
        @Override
        public void OnRtmpAudioLevelCallback(final String strCustomId, final int level) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(strCustomId.equals(mHostId)) {
                        if(level > 0) {
                            mDiffView.start();
                        } else {
                            mDiffView.stop();
                        }
                    }
                    updateAudioChatLevel(strCustomId, level);
                }
            });
        }

        /**
         * 游客RTC 状态回调
         * @param code 回调响应码：0：正常；101：主播未开启直播；
         * @param strReason 原因描述
         */
        @Override
        public void OnRTCJoinLineResultCallback(final int code, String strReason) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {

                    } else if (code == 101) {
                        Toast.makeText(AudioGuestActivity.this, R.string.str_hoster_not_live, Toast.LENGTH_LONG).show();
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {

                    } else if (code == -1) {
                        Toast.makeText(AudioGuestActivity.this, R.string.str_hoster_refused, Toast.LENGTH_LONG).show();
                        mStartLine = false;
                        mBtnConnect.setText(R.string.str_connect_hoster);
                    }
                }
            });
        }

        /**
         * 挂断连线回调
         */
        @Override
        public void OnRTCHangupLineCallback() {
            //主播连线断开
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGuestKit.HangupRTCLine();
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AudioGuestActivity.this, R.string.str_hoster_leave, Toast.LENGTH_LONG).show();
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

        }

        /**
         * 连线关闭后图像回调
         * @param strLivePeerID
         */
        @Override
        public void OnRTCCloseVideoRenderCallback(final String strLivePeerID) {

        }

        /**
         * 音频连麦接通时回调
         * @param strLivePeerID
         * @param strCustomID
         */
        @Override
        public void OnRTCOpenAudioLineCallback(final String strLivePeerID, final String strCustomID) {

            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addAudioChatList(strLivePeerID, strCustomID);
                }
            });
        }

        /**
         * 音频连麦挂断时回调
         * @param strLivePeerID
         * @param strCustomID
         */
        @Override
        public void OnRTCCloseAudioLineCallback(final String strLivePeerID, final String strCustomID) {
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    removeAudioChatLevel(strLivePeerID);
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addChatMessageList(new ChatMessageBean(strCustomID, strCustomName, "", strBarrage));
                    IDanmakuItem item = new DanmakuItem(AudioGuestActivity.this, new SpannableString(strBarrage), mDanmakuView.getWidth(), 0, R.color.yellow_normol, 18, 1);
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGuestList.clear();
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject userData = new JSONObject(strUserData);
                        addChatMessageList(new ChatMessageBean(userData.getString("NickName"), "", userData.getString("IconUrl"), ""));
                        if (!TextUtils.isEmpty(strCustomID) && !TextUtils.isEmpty(strUserData)) {
                            AudioItemBean item = new AudioItemBean();
                            item.setmStrCustomid(strCustomID);
                            item.setmStrCustomIcon(userData.getString("IconUrl"));
                            mGuestList.add(item);
                        }
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
            AudioGuestActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   mGuestAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}

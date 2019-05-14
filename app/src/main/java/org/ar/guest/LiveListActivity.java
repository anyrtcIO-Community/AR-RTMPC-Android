package org.ar.guest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.SimpleResponseListener;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.ar.BaseActivity;
import org.ar.ARApplication;
import org.ar.adapter.LiveListAdapter;
import org.ar.hoster.AudioHosterActivity;
import org.ar.hoster.HosterActivity;
import org.ar.rtmpc.BuildConfig;
import org.ar.model.LiveBean;
import org.ar.utils.DeveloperInfo;
import org.ar.utils.MD5;
import org.ar.utils.PermissionsCheckUtil;
import org.ar.utils.ToastUtil;
import org.ar.rtmpc_hybrid.ARRtmpcHttpKit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LiveListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.OnItemClickListener, View.OnClickListener {


    RecyclerView rvList;
    SwipeRefreshLayout swipeRefresh;
    LiveListAdapter mAdapter;
    private List<LiveBean> live_list = new ArrayList<>();
    Button btn_video, btn_audio;
    TextView tvVersion;
    @Override
    public int getLayoutId() {
        return org.ar.rtmpc.R.layout.activity_live_list;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

        tvVersion=findViewById(org.ar.rtmpc.R.id.tv_version);
        tvVersion.setText("v "+ BuildConfig.VERSION_NAME);
        btn_video = findViewById(org.ar.rtmpc.R.id.btn_video);
        btn_audio =  findViewById(org.ar.rtmpc.R.id.btn_audio);
        btn_video.setOnClickListener(this);
        btn_audio.setOnClickListener(this);
        rvList =  findViewById(org.ar.rtmpc.R.id.rv_list);
        swipeRefresh = findViewById(org.ar.rtmpc.R.id.swipe_refresh);
        mAdapter = new LiveListAdapter();
        mAdapter.setOnItemClickListener(this);
        mAdapter.setEmptyView(getEmptyView());
        swipeRefresh.setOnRefreshListener(this);
        rvList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        rvList.setAdapter(mAdapter);

        AndPermission.with(this).runtime().permission(Permission.RECORD_AUDIO,Permission.CAMERA).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLiveList();
    }

    public View getEmptyView(){
        View view=View.inflate(this, org.ar.rtmpc.R.layout.empty_act_data,null);
        TextView tvReGet= (TextView) view.findViewById(org.ar.rtmpc.R.id.tv_reget);
        tvReGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLiveList();
            }
        });
        return view;
    }
    @Override
    public void onRefresh() {
        getLiveList();
    }

    private void getPullUrlAndJoin(final String liveId, final int isAudioLive, final String hostName) {
        showProgressDialog();
        String random = (int) ((Math.random() * 9 + 1) * 100000) + "";
        long timestamp = System.currentTimeMillis();
        StringRequest request = new StringRequest("https://vdn.anyrtc.cc/oauth/anyapi/v1/vdnUrlSign/getAppVdnUrl", RequestMethod.POST);
        request.add("appid", DeveloperInfo.APPID);
        request.add("stream", liveId);
        request.add("random", random);
        request.add("signature", MD5.getMD5(DeveloperInfo.APPID + timestamp + DeveloperInfo.APP_V_TOKEN + random));
        request.add("timestamp", timestamp);
        request.add("appBundleIdPkgName", DeveloperInfo.APP_PACKAGE);
        NoHttp.getRequestQueueInstance().add(1, request, new SimpleResponseListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                Log.d("rtmpcUrl", response.get());
                hiddenProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response.get());
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        String pullUrl = jsonObject.getString("pull_url");
                        if (!TextUtils.isEmpty(pullUrl)) {
                                Intent intent = new Intent(LiveListActivity.this, isAudioLive==1 ? AudioGuestActivity.class : GuestActivity.class);
                                intent.putExtra("pullURL", pullUrl);
                                intent.putExtra("liveId",liveId);
                                intent.putExtra("hostName",hostName);
                                startActivity(intent);
                        } else {
                            ToastUtil.show("拉流地址为空");
                        }
                    } else {
                        String result = jsonObject.getString("message");
                        ToastUtil.show("Error:" + result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtil.show("解析数据失败");
                }

            }

            @Override
            public void onFailed(int what, Response<String> response) {
                hiddenProgressDialog();
            }
        });

    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, final int position) {
        if (AndPermission.hasPermissions(LiveListActivity.this,Permission.CAMERA,Permission.RECORD_AUDIO)){
            getPullUrlAndJoin(mAdapter.getItem(position).getmAnyrtcId(),mAdapter.getItem(position).getIsAudioLive(),mAdapter.getItem(position).getmHostName());
        }else {
            PermissionsCheckUtil.showMissingPermissionDialog(LiveListActivity.this, "请先开启录音和相机权限");
        }
    }

    private void getLiveList() {
        ARRtmpcHttpKit.getLivingList(this, new ARRtmpcHttpKit.RTMPCHttpCallback() {
            @Override
            public void OnRTMPCHttpOK(String s) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                if (TextUtils.isEmpty(s)) {
                    return;
                }
                try {
                    live_list.clear();
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.has("LiveList")) {
                        JSONArray liveList = jsonObject.getJSONArray("LiveList");
                        JSONArray member = jsonObject.getJSONArray("LiveMembers");
                        for (int i = 0; i < liveList.length(); i++) {
                            JSONObject itemJson = new JSONObject(liveList.getString(i));
                            LiveBean bean = new LiveBean();
                            bean.setmRtmpPullUrl(itemJson.getString("rtmpUrl"));
                            bean.setmHlsUrl(itemJson.getString("hlsUrl"));
                            bean.setmLiveTopic(itemJson.getString("liveTopic"));
                            bean.setIsAudioLive(itemJson.getInt("isAudioLive"));
                            bean.setmAnyrtcId(itemJson.getString("anyrtcId"));
                            bean.setmHostName(itemJson.getString("hosterName"));
                            if (i <= member.length()) {
                                bean.setmMemberNum(member.get(i).toString());
                            }
                            live_list.add(bean);
                        }
                        mAdapter.setNewData(live_list);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void OnRTMPCHttpFailed(int i) {
                ToastUtil.show("获取列表失败");
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case org.ar.rtmpc.R.id.btn_audio:
                if (AndPermission.hasPermissions(LiveListActivity.this,Permission.CAMERA,Permission.RECORD_AUDIO)){
                    getPushUrlAndStartLive(true);
                }else {
                    PermissionsCheckUtil.showMissingPermissionDialog(LiveListActivity.this, "请先开启录音和相机权限");
                }

                break;
            case org.ar.rtmpc.R.id.btn_video:
                if (AndPermission.hasPermissions(LiveListActivity.this,Permission.CAMERA,Permission.RECORD_AUDIO)){
                    getPushUrlAndStartLive(false);
                }else {
                    PermissionsCheckUtil.showMissingPermissionDialog(LiveListActivity.this, "请先开启录音和相机权限");
                }
                break;
        }
    }

    private void getPushUrlAndStartLive(final boolean isAudioLive) {
        showProgressDialog();
        String random = (int) ((Math.random() * 9 + 1) * 100000) + "";
        long timestamp = System.currentTimeMillis();
        StringRequest request = new StringRequest("https://vdn.anyrtc.cc/oauth/anyapi/v1/vdnUrlSign/getAppVdnUrl", RequestMethod.POST);
        request.add("appid", DeveloperInfo.APPID);
        request.add("stream", ARApplication.LIVE_ID);
        request.add("random", random);
        request.add("signature", MD5.getMD5(DeveloperInfo.APPID + timestamp + DeveloperInfo.APP_V_TOKEN + random));
        request.add("timestamp", timestamp);
        request.add("appBundleIdPkgName", DeveloperInfo.APP_PACKAGE);
        NoHttp.getRequestQueueInstance().add(1, request, new SimpleResponseListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                Log.d("rtmpcUrl", response.get());
                hiddenProgressDialog();
                try {
                    JSONObject jsonObject = new JSONObject(response.get());
                    int code = jsonObject.getInt("code");
                    if (code == 200) {
                        String pushUrl = jsonObject.getString("push_url");
                        if (!TextUtils.isEmpty(pushUrl)) {
                            Intent intent = new Intent(LiveListActivity.this, isAudioLive ? AudioHosterActivity.class : HosterActivity.class);
                            intent.putExtra("pushURL", pushUrl);
                            startActivity(intent);
                        } else {
                            ToastUtil.show("获取推流地址失败");
                        }
                    } else {
                        String result = jsonObject.getString("message");
                        ToastUtil.show("Error:" + result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ToastUtil.show("解析数据失败");
                }

            }

            @Override
            public void onFailed(int what, Response<String> response) {
                hiddenProgressDialog();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0);
            finishAnimActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

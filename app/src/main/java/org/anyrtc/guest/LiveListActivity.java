package org.anyrtc.guest;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.anyrtc.BaseActivity;
import org.anyrtc.HybirdApplication;
import org.anyrtc.adapter.LiveListAdapter;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LiveBean;
import org.anyrtc.rtmpc_hybrid.RTMPCHttpKit;
import org.anyrtc.utils.Constans;
import org.anyrtc.utils.MD5;
import org.anyrtc.utils.NetHttp;
import org.anyrtc.utils.PermissionsCheckUtil;
import org.anyrtc.utils.ResultListener;
import org.anyrtc.utils.ToastUtil;
import org.anyrtc.widgets.CircleImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static org.anyrtc.utils.Constans.LIVEBEAN;
import static org.anyrtc.utils.Constans.REQUECT_CODE_CAMARE;

public class LiveListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.OnItemClickListener {


    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    LiveListAdapter mAdapter;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.iv_icon)
    CircleImageView ivIcon;
    @BindView(R.id.tv_name)
    TextView tvName;
    private List<LiveBean> live_list = new ArrayList<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_live_list;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mAdapter = new LiveListAdapter();
        mAdapter.setOnItemClickListener(this);
        mAdapter.setEmptyView(R.layout.empty_act_data, (ViewGroup) rvList.getParent());
        swipeRefresh.setOnRefreshListener(this);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(mAdapter);
        tvName.setText(HybirdApplication.getNickName());
        getLiveList();
    }

    @Override
    public void onRefresh() {
        getLiveList();
    }
    private void getRTMPCUrl(final LiveBean liveBean){
        showProgressDialog();
        String random=(int)((Math.random()*9+1)*100000)+"";
        long timestamp=System.currentTimeMillis();
        StringRequest request=new StringRequest("https://vdn.anyrtc.cc/oauth/anyapi/v1/vdnUrlSign/getAppVdnUrl", RequestMethod.POST);
        request.add("appid", Constans.APPID);
        request.add("stream",liveBean.getmAnyrtcId());
        request.add("random",random);
        request.add("signature", MD5.getMD5(Constans.APPID+timestamp+Constans.APP_V_TOKEN+random));
        request.add("timestamp",timestamp);
        request.add("appBundleIdPkgName",Constans.APP_PACKAGE);
        NetHttp.getInstance().request(1, request, new ResultListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                hiddenProgressDialog();
                Log.d("rtmpcUrl",response.get());
                try {
                    JSONObject jsonObject=new JSONObject(response.get());
                    int code=jsonObject.getInt("code");
                    if (code==200){
                        String pullUrl=jsonObject.getString("pull_url");
                        if (!TextUtils.isEmpty(pullUrl)){
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(LIVEBEAN, liveBean);
                            bundle.putString("pull_url",pullUrl);
                            if (liveBean.isAudioLive == 1) {
                                startAnimActivity(AudioGuestActivity.class, bundle);
                            } else {
                                startAnimActivity(GuestActivity.class, bundle);
                            }
                        }else {
                            ToastUtil.show("拉流地址为空");
                        }
                    }else {
                        String result=jsonObject.getString("message");
                        ToastUtil.show("Error:"+result);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndPermission.with(this)
                    .requestCode(REQUECT_CODE_CAMARE)
                    .permission(Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO)
                    .callback(new PermissionListener() {
                        @Override
                        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                            getRTMPCUrl(mAdapter.getItem(position));
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                            if (deniedPermissions.size() == 2) {
                                PermissionsCheckUtil.showMissingPermissionDialog(LiveListActivity.this, "请先开启录音和相机权限");
                                return;
                            }
                            for (int i = 0; i < deniedPermissions.size(); i++) {
                                if (deniedPermissions.get(i).equals(Manifest.permission.RECORD_AUDIO)) {
                                    PermissionsCheckUtil.showMissingPermissionDialog(LiveListActivity.this, "请先开启录音权限");
                                } else {
                                    PermissionsCheckUtil.showMissingPermissionDialog(LiveListActivity.this, "请先开启相机权限");
                                }
                            }
                        }
                    }).start();
        } else {
            getRTMPCUrl(mAdapter.getItem(position));
        }
    }

    private void getLiveList() {
        RTMPCHttpKit.getLivingList(this, new RTMPCHttpKit.RTMPCHttpCallback() {
            @Override
            public void OnRTMPCHttpOK(String s) {
                if (swipeRefresh!=null) {
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
                            bean.setIsLiveLandscape(itemJson.getInt("isLiveLandscape"));
                            bean.setmAnyrtcId(itemJson.getString("anyrtcId"));
                            bean.setmHostName(itemJson.getString("hosterName"));
                            if (i<=member.length()) {
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
                if (swipeRefresh!=null) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }


    @OnClick(R.id.iv_back)
    public void onClick() {
        finishAnimActivity();
    }

}

package org.anyrtc.guest;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.anyrtc.BaseActivity;
import org.anyrtc.HybirdApplication;
import org.anyrtc.adapter.LiveListAdapter;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LiveBean;
import org.anyrtc.rtmpc_hybrid.RTMPCHttpKit;
import org.anyrtc.utils.PermissionsCheckUtil;
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
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(LIVEBEAN, mAdapter.getItem(position));
                            if (mAdapter.getItem(position).isAudioLive == 1) {
                                startAnimActivity(AudioGuestActivity.class, bundle);
                            } else {
                                startAnimActivity(GuestActivity.class, bundle);
                            }
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
            Bundle bundle = new Bundle();
            bundle.putSerializable(LIVEBEAN, mAdapter.getItem(position));
            if (mAdapter.getItem(position).isAudioLive == 1) {
                startAnimActivity(AudioGuestActivity.class, bundle);
            } else {
                startAnimActivity(GuestActivity.class, bundle);
            }
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

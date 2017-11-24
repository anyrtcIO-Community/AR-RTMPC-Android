package org.anyrtc.widgets;

import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import org.anyrtc.adapter.LiveMemberAdapter;
import org.anyrtc.live_line.R;
import org.anyrtc.rtmpc_hybrid.RTMPCHttpKit;
import org.anyrtc.utils.ToastUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;

/**
 * Created by liuxiaozhong on 2017-09-25.
 */

public class MemberListDialog extends AppBaseDialogFragment implements SwipeRefreshLayout.OnRefreshListener{
    @BindView(R.id.rv_list)
    RecyclerView rvList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    private LiveMemberAdapter liveMemberAdapter;
    private String anyRTCID,strServerId, strRoomId;
    @Override
    protected int getContentViewID() {
        return R.layout.fragment_member;
    }

    @Override
    protected void initData() {
        swipeRefresh.setOnRefreshListener(this);
        rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        liveMemberAdapter = new LiveMemberAdapter();
        liveMemberAdapter.setEmptyView(R.layout.empty_no_member, (ViewGroup) rvList.getParent());
        rvList.setAdapter(liveMemberAdapter);
    }

    @Override
    protected void setLayout() {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//必须放在setContextView之前调用, 去掉Dialog中的蓝线
    }

    @Override
    public void onRefresh() {
        getMemberList();
    }

    public void setParams(String anyRTCID,String strServerId,String strRoomId){
        this.anyRTCID=anyRTCID;
        this.strRoomId=strRoomId;
        this.strServerId=strServerId;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        getMemberList();
    }

    public void getMemberList() {
        if (TextUtils.isEmpty(anyRTCID) || TextUtils.isEmpty(strServerId) || TextUtils.isEmpty(strRoomId)) {
            swipeRefresh.setRefreshing(false);
            return;
        }
        RTMPCHttpKit.getLiveMemberList(getActivity(), anyRTCID, strServerId, strRoomId, 0 + "", new RTMPCHttpKit.RTMPCHttpCallback() {
            @Override
            public void OnRTMPCHttpOK(String strContent) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                liveMemberAdapter.getData().clear();
                Log.d("RTMPC", "人员列表" + strContent);
                try {
                    JSONObject jsonObject = new JSONObject(strContent);
                    JSONArray name = jsonObject.getJSONArray("UserData");
                    for (int i = 0; i < name.length(); i++) {
                        JSONObject userdata=new JSONObject(name.getString(i));
                        liveMemberAdapter.addData(userdata.getString("nickName"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnRTMPCHttpFailed(int code) {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                    ToastUtil.show("获取列表失败");
                }

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics( dm );
        getDialog().getWindow().setLayout( dm.widthPixels,  getDialog().getWindow().getAttributes().height );
    }
}

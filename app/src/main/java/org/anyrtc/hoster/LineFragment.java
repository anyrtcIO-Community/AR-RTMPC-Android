package org.anyrtc.hoster;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.anyrtc.adapter.LiveLineAdapter;
import org.anyrtc.live_line.R;
import org.anyrtc.model.LineBean;
import org.anyrtc.rtmpc_hybrid.RTMPCAudioHosterKit;
import org.anyrtc.rtmpc_hybrid.RTMPCHosterKit;

/**
 * Created by liuxiaozhong on 2017/9/24.
 */

public class LineFragment extends Fragment implements HosterActivity.LineListener,BaseQuickAdapter.OnItemChildClickListener{

    private RecyclerView lineList;
    private LiveLineAdapter mAadapter;
    private RTMPCHosterKit rtmpcHosterKit;
    private RTMPCAudioHosterKit audioHosterKit;
    private HosterActivity activity;
    private AudioHosterActivity audioHosterActivity;
    private SwipeRefreshLayout swipe_refresh;
    private boolean isAudioLive=false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_line, container, false);
        swipe_refresh= (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipe_refresh.setEnabled(false);
        lineList= (RecyclerView) view.findViewById(R.id.rv_list);
        lineList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAadapter=new LiveLineAdapter();
        mAadapter.setEmptyView(R.layout.empty_no_line_data, (ViewGroup) lineList.getParent());
        mAadapter.setOnItemChildClickListener(this);
        lineList.setAdapter(mAadapter);
        if (getActivity() instanceof HosterActivity){
            isAudioLive=false;
            activity= (HosterActivity) getActivity();
            activity.SetLineListener(this);
        }else {
            isAudioLive=true;
            audioHosterActivity= (AudioHosterActivity) getActivity();
            audioHosterActivity.SetLineListener(this);
        }


        return view;

    }

    @Override
    public void AddAudioGuest(LineBean lineBean, RTMPCAudioHosterKit hosterKit) {
        isAudioLive=true;
        this.audioHosterKit=hosterKit;
        mAadapter.addData(lineBean);
    }

    @Override
    public void AddGuest(LineBean lineBean, RTMPCHosterKit hosterKit) {
        isAudioLive=false;
        this.rtmpcHosterKit=hosterKit;
        mAadapter.addData(lineBean);
    }

    @Override
    public void RemoveGuest(String peerid) {
        int index=9;
        for (int i=0;i<mAadapter.getData().size();i++){
            if (mAadapter.getItem(i).peerId.equals(peerid)){
                index=i;
            }
        }
        if (index!=9&&index<=mAadapter.getData().size()){
            mAadapter.remove(index);
        }
    }


    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        switch (view.getId()){
            case R.id.tv_agree:
                if (isAudioLive){
                    if (audioHosterKit != null) {
                        audioHosterKit.acceptRTCLine(mAadapter.getItem(position).peerId);
                        mAadapter.remove(position);

                    }
                }else {
                    if (rtmpcHosterKit != null) {
                        rtmpcHosterKit.acceptRTCLine(mAadapter.getItem(position).peerId);
                        mAadapter.remove(position);
                            if (activity != null) {
                                activity.closeLineDialog();
                        }
                    }
                }

                break;
            case R.id.tv_refuse:
                if (isAudioLive){
                    if (audioHosterKit != null) {
                        audioHosterKit.rejectRTCLine(mAadapter.getItem(position).peerId);
                        mAadapter.remove(position);
                    }
                }else {
                    if (rtmpcHosterKit != null) {
                        rtmpcHosterKit.rejectRTCLine(mAadapter.getItem(position).peerId);
                        mAadapter.remove(position);
                    }
                }
                break;
        }
    }
}

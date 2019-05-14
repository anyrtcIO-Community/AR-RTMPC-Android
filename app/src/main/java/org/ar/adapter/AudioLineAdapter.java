package org.ar.adapter;

import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.ar.rtmpc.R;
import org.ar.model.LineBean;

/**
 * Created by liuxiaozhong on 2017-09-25.
 */

public class AudioLineAdapter extends BaseQuickAdapter<LineBean,BaseViewHolder> {
    boolean isHost;
    public AudioLineAdapter(boolean isHost) {
        super(R.layout.item_audio_line);
        this.isHost=isHost;
    }

    @Override
    protected void convert(BaseViewHolder helper, LineBean item) {
        TextView hangup=helper.getView(R.id.tv_hangup);
        helper.setText(R.id.tv_name,item.name);
        helper.addOnClickListener(R.id.tv_hangup);
        if (isHost){
            hangup.setVisibility(View.VISIBLE);
        }else {
            if (item.isSelf){
                hangup.setVisibility(View.VISIBLE);
            }else {
                hangup.setVisibility(View.INVISIBLE);
            }
        }
    }

}

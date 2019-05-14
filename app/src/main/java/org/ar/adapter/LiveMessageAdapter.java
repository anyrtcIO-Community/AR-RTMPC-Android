package org.ar.adapter;

import android.graphics.Color;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.ar.model.MessageBean;

/**
 * Created by liuxiaozhong on 2017-09-22.
 */

public class LiveMessageAdapter extends BaseQuickAdapter<MessageBean, BaseViewHolder> {
    public LiveMessageAdapter() {
        super(android.R.layout.simple_list_item_1);
    }

    @Override
    protected void convert(BaseViewHolder helper, MessageBean item) {
        helper.setTextColor(android.R.id.text1, item.type==1 ? Color.parseColor("#ffffff") : Color.parseColor("#666666"));
        helper.setText(android.R.id.text1, item.name+":"+item.content);
    }
}

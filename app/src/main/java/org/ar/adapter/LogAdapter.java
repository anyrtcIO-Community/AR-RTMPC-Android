package org.ar.adapter;

import android.graphics.Color;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

/**
 * Created by liuxiaozhong on 2019/3/12.
 */
public class LogAdapter extends BaseQuickAdapter<String,BaseViewHolder> {
    public LogAdapter() {
        super(android.R.layout.simple_list_item_1);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        TextView textView=helper.getView(android.R.id.text1);
        textView.setTextColor(Color.parseColor("#666666"));
        helper.setText(android.R.id.text1,item);
    }
}

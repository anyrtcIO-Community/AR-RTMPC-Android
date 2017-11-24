package org.anyrtc.widgets;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by KathLine on 2016/12/30.
 */

public abstract class AppBaseDialogFragment extends DialogFragment {
    private View view;
    protected Unbinder unbinder;

    public AppBaseDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//必须放在setContextView之前调用, 去掉Dialog中的蓝线
//        view = LayoutInflater.from(getActivity()).inflate(getContentViewID(), container, false);
        view = inflater.inflate(getContentViewID(), container);
        setLayout();
        unbinder = ButterKnife.bind(this, view);
        initData();
        return view;
    }

    protected void setLayout() {
    }

    @LayoutRes
    protected abstract int getContentViewID();

    protected abstract void initData();
}

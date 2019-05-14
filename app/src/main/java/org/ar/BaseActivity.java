package org.ar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gyf.barlibrary.ImmersionBar;

/**
 * Created by Skyline on 2016/5/24.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected ImmersionBar mImmersionBar;
    ProgressDialog pd;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.getLayoutId());
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarDarkFont(true,0.2f).init();
        this.initView(savedInstanceState);
    }
    private void ProgressDialog(Context ctx) {
        pd = new ProgressDialog(ctx);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }
    public void showProgressDialog() {
        if (pd == null) {
            ProgressDialog(this);
        }
        pd.setMessage("正在加载...");
        pd.show();
    }

    public void hiddenProgressDialog() {
        if (pd != null && pd.isShowing()) {
            pd.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null)
            mImmersionBar.destroy();
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }


    public void startAnimActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    public void finishAnimActivity() {
        finish();
    }

    public void startAnimActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public abstract int getLayoutId();

    public abstract void initView(Bundle savedInstanceState);

}

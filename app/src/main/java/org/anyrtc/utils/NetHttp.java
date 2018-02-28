package org.anyrtc.utils;

import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.RequestQueue;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.rest.SimpleResponseListener;

/**
 * Created by liuxiaozhong on 2017/12/18.
 */

public class NetHttp {

    private static NetHttp instance;

    public static NetHttp getInstance() {
        if (instance == null)
            synchronized (NetHttp.class) {
                if (instance == null)
                    instance = new NetHttp();
            }
        return instance;
    }

    private RequestQueue queue;

    private NetHttp() {
        queue = NoHttp.newRequestQueue(5);
    }

    public <T> void request(int what, Request<T> request, final ResultListener<T> listener) {
        queue.add(what, request, new SimpleResponseListener<T>() {
            @Override
            public void onStart(int what) {
                super.onStart(what);
            }

            @Override
            public void onSucceed(int what, Response<T> response) {
                listener.onSucceed(what,response);
            }

            @Override
            public void onFailed(int what, Response<T> response) {
                ToastUtil.show("请检查网络");
                listener.onFailed(what,response);
            }

            @Override
            public void onFinish(int what) {
                super.onFinish(what);
            }
        });
    }


    // 完全退出app时，调用这个方法释放CPU。
    public void stop() {
        queue.stop();
    }

    public void cancle() {
        queue.cancelAll();
    }
}

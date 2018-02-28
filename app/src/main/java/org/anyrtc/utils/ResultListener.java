package org.anyrtc.utils;

import com.yanzhenjie.nohttp.rest.Response;

/**
 * Created by liuxiaozhong on 2017/12/18.
 */

public interface ResultListener<T>{

    public void onSucceed(int what, Response<T> response);

    public void onFailed(int what, Response<T> response);
}

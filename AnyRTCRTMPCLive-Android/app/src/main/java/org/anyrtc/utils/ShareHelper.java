package org.anyrtc.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.platformtools.Util;

import org.anyrtc.live_line.R;

import java.util.List;

/**
 * @author zhulang <br/>
 *         ShareHelper create at 2015-12-22
 *         上午10:59:20
 */
public class ShareHelper {
    private Context context;
    private static final String App_ID = "wx40db3ffd58b0c6a9";
    private IWXAPI api;

    public ShareHelper(Context context) {
        this.context = context;
        api = WXAPIFactory.createWXAPI(context, App_ID, true);
        api.registerApp(App_ID);
    }

    /**
     * Share Weixing
     * @param webUrl
     */
    public void shareWeiXin(String webUrl) {
        String msgTitle = context.getString(R.string.app_name);
        String msgText = context.getString(R.string.share_str_weixing_title);
        shareWeiXin(webUrl, msgTitle, msgText);
    }

    /**
     * Share Wei Xin
     * @param msgTitle
     * @param msgText
     * @param webUrl
     */
    public void shareWeiXin( String webUrl,String msgTitle, String msgText) {
        shareToWeiXin(webUrl, msgTitle, msgText);
    }


    /**
     * WeChat share meeting
     *
     * @param webpageUrl
     * @param title
     * @param description
     */
    public void shareToWeiXin(String webpageUrl, String title, String description) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = webpageUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        msg.thumbData = Util.bmpToByteArray(thumb, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    /**
     * @param packageName
     * @return
     */
    public boolean isAvilible(String packageName) {
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (((PackageInfo) pinfo.get(i)).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }

    private class ShareItem {
        String title;

        String activityName;
        String packageName;

        public ShareItem(String title, String activityName, String packageName) {
            this.title = title;
            this.activityName = activityName;
            this.packageName = packageName;
        }
    }

}

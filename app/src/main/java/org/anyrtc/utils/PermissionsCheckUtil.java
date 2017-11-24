package org.anyrtc.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class PermissionsCheckUtil {

    public static final int SETTING_APP = 0x123;
    private static String TAG = "PermissionsCheckUtil";
    private static String[] PHONE_MTYB = new String[]{"sanxing", "xiaomi"};

    /**
     * @param activity
     * @param message  显示缺失权限提示说明
     */
    public static void showMissingPermissionDialog(final Activity activity, String message) {
        boolean canSetting = false;
        String mtyb = Build.BRAND;//手机品牌
        for (int i = 0; i < PHONE_MTYB.length; i++) {
            if (PHONE_MTYB[i].equalsIgnoreCase(mtyb)) {//相等可以调用到设置界面进行权限设置
                canSetting = true;
                break;
            } else {
                canSetting = false;
            }
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("帮助");
        builder.setMessage(message);
        if (canSetting) {
            builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startAppSettings(activity);
                    dialog.dismiss();
                }
            });
        }
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // 启动应用的设置
    public static void startAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, SETTING_APP);
    }
}

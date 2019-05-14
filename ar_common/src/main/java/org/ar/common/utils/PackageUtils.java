package org.ar.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Ming on 2016-09-24.
 */
public class PackageUtils {
    private static final PackageUtils sSingleton = new PackageUtils();
    private static final char[] HEX_CHAR = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private PackageUtils() {}

    public static PackageUtils getInstance() {
        return sSingleton;
    }

    /**
     * <p>Get the PackageInfo of specified package.</p>
     * @param context
     * @param pkgName the package name
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    public PackageInfo getPackageInfo(Context context, String pkgName) {
        try {
            return context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 获取签名的MD5摘要 */
    public String getSignatureDigest(PackageInfo pkgInfo) {
        Signature[] signaturesArray = pkgInfo.signatures;
        if(null == signaturesArray) {
            return "";
        } else {
            int length = pkgInfo.signatures.length;
            if (length <= 0) {
                return "";
            }

            Signature signature = pkgInfo.signatures[0];
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                // Should not occur
            }
            byte[] digest = md5.digest(signature.toByteArray()); // get digest with md5 algorithm
            return toHexString(digest);
        }
    }

    /** 将字节数组转化为对应的十六进制字符串 */
    private String toHexString(byte[] rawByteArray) {
        char[] chars = new char[rawByteArray.length * 2];
        for (int i = 0; i < rawByteArray.length; ++i) {
            byte b = rawByteArray[i];
            chars[i*2] = HEX_CHAR[(b >>> 4 & 0x0F)];
            chars[i*2+1] = HEX_CHAR[(b & 0x0F)];
        }
        return new String(chars);
    }

    public List<PackageInfo> getInstalledPackages(Context context) {
        return context.getPackageManager().getInstalledPackages(PackageManager.GET_SIGNATURES);
    }

    /**
     * 获取应用的签名
     * @param context 上下文环境
     * @param mUpperCase 返回字符串大写或小写；true：大写，false：小写
     * @return
     */
    public String getStrSignatureDigest(Context context, boolean mUpperCase) {
        String digest = null;
        digest = getSignatureDigest(getPackageInfo(context, context.getPackageName()));
        digest = mUpperCase ? digest.toUpperCase(): digest.toLowerCase();

        return digest;
    }

    public String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }
}

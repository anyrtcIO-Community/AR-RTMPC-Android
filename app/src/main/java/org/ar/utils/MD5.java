package org.ar.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liuxiaozhong on 2016/5/30.
 */
public class MD5 {

    public static String getMD5(String password ) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if(i<0) i+= 256;
                if(i<16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString().toLowerCase();
            //System.out.println("result: " + buf.toString());//32位的加密
           // System.out.println("result: " + buf.toString().substring(8,24));//16位的加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


}

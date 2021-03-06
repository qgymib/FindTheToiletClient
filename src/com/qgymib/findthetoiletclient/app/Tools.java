package com.qgymib.findthetoiletclient.app;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.qgymib.findthetoiletclient.data.ConfigData;

import android.util.Log;

/**
 * 常用工具库，封装一些通用操作。
 * 
 * @author qgymib
 *
 */
public class Tools {

    /**
     * 取得字符串的MD5校验码。
     * 
     * @param src
     *            需要取得md5码的字符串
     * @return 字符串的md5值
     */
    public static String getMD5(String src) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };// 用来将字节转换成16进制表示的字符
        String hexResult = null;
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("MD5");
            md.update(src.getBytes());
            byte[] result = md.digest();
            // 每个字节用 16 进制表示的话，使用两个字符， 所以表示成 16
            char str[] = new char[16 * 2];
            for (int k = 0, i = 0; i < 16; i++) {
                // 从第一个字节开始，对 MD5 的每一个字节
                // 转换成16进制字符
                byte byte0 = result[i];// 取第 i 个字节
                // 取字节中高 4 位的数字转换,//
                // >>>
                // 为逻辑右移，将符号位一起右移
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                // 取字节中低 4 位的数字转换
                str[k++] = hexDigits[byte0 & 0xf];
            }
            hexResult = new String(str);
        } catch (NoSuchAlgorithmException e) {
            Log.e(ConfigData.Common.tag, "Tools:getMD5() 无此运算法则");
            e.printStackTrace();
        }

        return hexResult;
    }

    /**
     * 取得指定字符串的CRC32校验值。
     * 
     * @param src
     *            需要取得CRC32校验码的字符串
     * @return 字符串的CRC32码
     */
    public static String getCRC32(String src) {
        String hexResult = null;
        CRC32 crc32 = new CRC32();

        crc32.update(src.getBytes());
        hexResult = Long.toHexString(crc32.getValue());

        return hexResult;
    }

    /**
     * 取得两点之间的距离
     * 
     * @param p1
     * @param p2
     * @return
     */
    public static double getDistance(GeoPoint p1, GeoPoint p2) {
        return DistanceUtil.getDistance(p1, p2);
    }

    /**
     * 取得两点之间的距离
     * 
     * @param latitude1E6
     * @param longitude1E6
     * @param latitude2E6
     * @param longitude2E6
     * @return
     */
    public static double getDistance(int latitude1E6, int longitude1E6,
            int latitude2E6, int longitude2E6) {
        return getDistance(new GeoPoint(latitude1E6, longitude1E6),
                new GeoPoint(latitude2E6, longitude2E6));
    }

    /**
     * 取得两点之间的距离
     * 
     * @param latitude1
     * @param longitude1
     * @param latitude2
     * @param longitude2
     * @return
     */
    public static double getDistance(double latitude1, double longitude1,
            double latitude2, double longitude2) {
        return getDistance((int) (latitude1 * 1E6), (int) (longitude1 * 1E6),
                (int) (latitude2 * 1E6), (int) (longitude2 * 1E6));
    }
}

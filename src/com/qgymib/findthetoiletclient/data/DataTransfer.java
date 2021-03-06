package com.qgymib.findthetoiletclient.data;

import android.os.Bundle;

import com.qgymib.findthetoiletclient.gui.AccountFragment;
import com.qgymib.findthetoiletclient.gui.BaiduMapFragment;
import com.qgymib.findthetoiletclient.gui.InfoFragment;
import com.qgymib.findthetoiletclient.gui.LoginFragment;
import com.qgymib.findthetoiletclient.gui.SignupFragment;
import com.qgymib.findthetoiletclient.service.LocationService;

/**
 * 用于各个组件之间的信息交互。
 * 
 * @author qgymib
 *
 */
public class DataTransfer {

    /**
     * 为AccountFragment及其子Fragment设计的信息交互接口。
     * 
     * @author qgymib
     * @see AccountFragment
     * @see LoginFragment
     * @see SignupFragment
     * @see InfoFragment
     */
    public static interface ViewTransfer {
        /**
         * 子Fragment通过ViewID通知父Fragment需要切换的目标Fragment。
         * 
         * @param viewID
         *            R.layout.fragment_account_xxxx
         */
        public void viewTransAction(int viewID);
    }

    /**
     * 为LocationService与调用者设计的信息交互接口。
     * 
     * @author qgymib
     * @see LocationService
     * @see BaiduMapFragment
     */
    public static interface LocationTransfer {
        /**
         * LocationService向其调用者传递定位结果。
         * 
         * @param locationInfoBundle
         */
        public void locationTransAction(Bundle locationInfoBundle);
    }

    /**
     * 为NavigationDrawerFragment与MainActivity以及BaiduMapFragment设计的接口。
     * 此接口用于告诉百度地图不用等待精确定位，立即导航。
     * 
     * @author qgymib
     *
     */
    public static interface NavigationTransfer {
        /**
         * NavigationDrawerFragment通过MainActivity中转告诉BaiduMapFragment立即定位。
         */
        public void navigationTransAction();
    }

    /**
     * 城市洗手间信息封装
     * 
     * @author qgymib
     *
     */
    public static final class LocationInfo {
        /**
         * 数据版本
         */
        public long version = 0;
        /**
         * 洗手间地理信息集
         */
        public String value = null;
    }
}

package com.qgymib.findthetoiletclient.app;

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
        public void transAction(int viewID);
    }

    /**
     * 为LocationService与BaiduMapFragment设计的信息交互接口。
     * 
     * @author qgymib
     * @see LocationService
     * @see BaiduMapFragment
     */
    public static interface LocationTransfer {
        /**
         * LocationService向BaiduMapFragment传递定位结果。
         * 
         * @param locationInfoBundle
         */
        public void transAction(Bundle locationInfoBundle);
    }
}

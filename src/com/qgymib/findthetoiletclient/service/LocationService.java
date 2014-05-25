package com.qgymib.findthetoiletclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.qgymib.findthetoiletclient.app.ConfigureInfo;
import com.qgymib.findthetoiletclient.app.DataTransfer;

/**
 * 定位服务。依据用户提供（或默认）参数进行基于A-GPS或GPS的定位。
 * 
 * @author qgymib
 *
 */
public class LocationService extends Service {

    private LocationClient mLocationClient = null;
    private LocationClientOption mLocationClientOption = null;
    private DataTransfer.LocationTransfer mLocationTransfer = null;
    private boolean isForce = false;

    /**
     * 设置定位参数
     */
    private void setLocationOptions() {
        if (mLocationClient != null && mLocationClientOption == null) {
            mLocationClientOption = new LocationClientOption();
            // 定位模式：精度
            mLocationClientOption.setLocationMode(LocationMode.Hight_Accuracy);
            // 定位结果集
            mLocationClientOption.setCoorType("bd0911");
            // 定位请求间隔
            mLocationClientOption.setScanSpan(1000);
            // 是否包含地址信息
            mLocationClientOption.setIsNeedAddress(true);
            // 是否包含运动方向
            mLocationClientOption.setNeedDeviceDirect(true);
            // 设置参数
            mLocationClient.setLocOption(mLocationClientOption);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化定位sdk
        mLocationClient = new LocationClient(getApplicationContext());
        // 注册监听结果调用
        mLocationClient.registerLocationListener(new LocationReceiveListener());
        // 设置定位参数
        setLocationOptions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocationServiceBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止定位sdk
        mLocationClient.stop();
    }

    /**
     * 处理定位sdk返回结果的回调类
     * 
     * @author qgymib
     *
     */
    private class LocationReceiveListener implements BDLocationListener {

        /**
         * 处理定位结果的回调函数
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO 实现接收定位信息功能
            if (location == null) {
                return;
            }

            // TODO 实现包含定位信息的Bundle的封装
            Bundle locationInfoBundle = new Bundle();

            // 定位结果有效性检查
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                // 定位有效
                locationInfoBundle.putBoolean(
                        ConfigureInfo.Location.Key.isValid, true);
                // 包含运动速度
                locationInfoBundle.putFloat(ConfigureInfo.Location.Key.speed,
                        location.getSpeed());
                // 包含卫星数量
                locationInfoBundle.putInt(ConfigureInfo.Location.Key.satellite,
                        location.getSatelliteNumber());
                // 包含运动方向
                locationInfoBundle.putFloat(
                        ConfigureInfo.Location.Key.direction,
                        location.getDirection());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                // 定位有效
                locationInfoBundle.putBoolean(
                        ConfigureInfo.Location.Key.isValid, true);
                // 包含地址
                locationInfoBundle.putString(
                        ConfigureInfo.Location.Key.address,
                        location.getAddrStr());
                locationInfoBundle.putInt(
                        ConfigureInfo.Location.Key.operationer,
                        location.getOperators());
            } else {
                // 定位失败
                locationInfoBundle.putBoolean(
                        ConfigureInfo.Location.Key.isValid, false);
            }

            // 包含半径
            locationInfoBundle.putFloat(ConfigureInfo.Location.Key.radius,
                    location.getRadius());
            // 包含经度
            locationInfoBundle.putDouble(ConfigureInfo.Location.Key.longitude,
                    location.getLongitude());
            // 包含纬度
            locationInfoBundle.putDouble(ConfigureInfo.Location.Key.latitude,
                    location.getLatitude());
            // 包含时间
            locationInfoBundle.putString(ConfigureInfo.Location.Key.time,
                    location.getTime());
            // 包含运行状态
            locationInfoBundle.putInt(ConfigureInfo.Location.Key.loc_type,
                    location.getLocType());

            // 定位有效或者需要强制执行时，执行回调函数
            if (locationInfoBundle
                    .getBoolean(ConfigureInfo.Location.Key.isValid) || isForce) {
                mLocationTransfer.transAction(locationInfoBundle);
            }
        }

        @Override
        public void onReceivePoi(BDLocation arg0) {
            // 留空
        }

    }

    /**
     * 用于返回LocationService的绑定
     * 
     * @author qgymib
     * 
     */
    public class LocationServiceBinder extends Binder {
        /**
         * 开始定位。此函数应该在
         * {@link #bindLocationTransfer(com.qgymib.findthetoiletclient.app.DataTransfer.LocationTransfer)}
         * 之后调用。
         */
        public void startLocate() {
            mLocationClient.start();
        }

        /**
         * 停止定位。
         */
        public void stopLocate() {
            mLocationClient.stop();
        }

        /**
         * 绑定用于处理定位结果的回调函数。当一个定位结果出现时，定位服务会调用此函数。若定位结果无效但是用户指定强制执行回调函数，
         * 则此回调函数仍然会被调用。 为了防止在出现定位结果之后由于尚未注册回调函数而导致结果处理失败，此函数应在
         * {@link #startLocate()} 之后立即调用或在启动服务之前调用。
         * 
         * @param locationTransfer
         *            用于处理定位结果的回调函数
         * @param isForce
         *            当定位结果无效时是否仍然执行回调函数
         */
        public void bindLocationTransfer(
                DataTransfer.LocationTransfer locationTransfer, boolean isForce) {
            mLocationTransfer = locationTransfer;
            LocationService.this.isForce = isForce;
        }

        /**
         * 绑定用于处理定位结果的回调函数。<b>仅</b>当一个有效的定位结果出现时，定位服务会调用此函数。
         * 为了防止在出现定位结果之后由于尚未注册回调函数而导致结果处理失败，此函数应在 {@link #startLocate()}
         * 之后立即调用或在启动服务之前调用。
         * 
         * @param locationTransfer
         *            用于处理定位结果的回调函数
         */
        public void bindLocationTransfer(
                DataTransfer.LocationTransfer locationTransfer) {
            bindLocationTransfer(locationTransfer, false);
        }
    }
}

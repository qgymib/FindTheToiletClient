package com.qgymib.findthetoiletclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.qgymib.findthetoiletclient.data.ConfigData;
import com.qgymib.findthetoiletclient.data.DataTransfer.LocationTransfer;

/**
 * 定位服务。依据用户提供（或默认）参数进行基于A-GPS或GPS的定位。
 * 
 * @author qgymib
 *
 */
public class LocationService extends Service {

    private LocationClient mLocationClient = null;
    private LocationClientOption mLocationClientOption = null;
    private LocationTransfer mLocationTransfer = null;
    private LocationReceiveListener locationReceiveListener = new LocationReceiveListener();
    private boolean isForced = false;
    private LocationServiceBinder binder = new LocationServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(ConfigData.Common.tag, "location service started");
        // 初始化定位sdk
        mLocationClient = new LocationClient(getApplicationContext());
        // 注册监听结果调用
        mLocationClient.registerLocationListener(locationReceiveListener);
        // 设置定位参数
        setLocationOptions();
        // 启动定位sdk
        mLocationClient.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止定位sdk
        mLocationClient.stop();
    }

    /**
     * 设置定位参数
     */
    private void setLocationOptions() {
        mLocationClientOption = new LocationClientOption();
        // 定位模式：精度
        mLocationClientOption.setLocationMode(LocationMode.Hight_Accuracy);
        // 定位结果集
        mLocationClientOption.setCoorType(ConfigData.Location.type);
        // 定位请求间隔
        mLocationClientOption.setScanSpan(ConfigData.Location.interval);
        // 是否包含地址信息
        mLocationClientOption.setIsNeedAddress(true);
        // 是否包含运动方向
        mLocationClientOption.setNeedDeviceDirect(true);
        // 设置参数
        mLocationClient.setLocOption(mLocationClientOption);
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

            // 若结果为空，则不作处理
            if (location == null) {
                return;
            }

            Bundle locationInfoBundle = new Bundle();

            // 包含运行状态
            locationInfoBundle.putInt(ConfigData.Location.Key.loc_type,
                    location.getLocType());
            // 定位结果有效性检查
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                // 定位有效
                locationInfoBundle.putBoolean(
                        ConfigData.Location.Key.isValid, true);
                // 包含类别
                locationInfoBundle.putString(ConfigData.Location.Key.type,
                        "GPS");
                // 包含运动速度
                locationInfoBundle.putFloat(ConfigData.Location.Key.speed,
                        location.getSpeed());
                // 包含卫星数量
                locationInfoBundle.putInt(ConfigData.Location.Key.satellite,
                        location.getSatelliteNumber());
                // 包含运动方向
                locationInfoBundle.putFloat(
                        ConfigData.Location.Key.direction,
                        location.getDirection());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                // 定位有效
                locationInfoBundle.putBoolean(
                        ConfigData.Location.Key.isValid, true);
                // 包含类别
                locationInfoBundle.putString(ConfigData.Location.Key.type,
                        "NET");
                // 包含地址
                locationInfoBundle.putString(
                        ConfigData.Location.Key.address,
                        location.getAddrStr());
                // 运营商信息
                locationInfoBundle.putInt(
                        ConfigData.Location.Key.operationer,
                        location.getOperators());
            } else {
                // 定位失败
                locationInfoBundle.putBoolean(
                        ConfigData.Location.Key.isValid, false);
            }

            // 包含半径
            locationInfoBundle.putFloat(ConfigData.Location.Key.radius,
                    location.getRadius());
            // 包含经度
            locationInfoBundle.putDouble(ConfigData.Location.Key.longitude,
                    location.getLongitude());
            // 包含纬度
            locationInfoBundle.putDouble(ConfigData.Location.Key.latitude,
                    location.getLatitude());
            // 包含时间
            locationInfoBundle.putString(ConfigData.Location.Key.time,
                    location.getTime());

            // 定位有效或者需要强制执行时，执行回调函数
            if (locationInfoBundle
                    .getBoolean(ConfigData.Location.Key.isValid) || isForced) {
                mLocationTransfer.locationTransAction(locationInfoBundle);
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
         * 开始定位。此函数应该在 {@link #bindLocationTransfer(LocationTransfer)} 之后调用。
         */
        public void startLocate() {
            Log.i(ConfigData.Common.tag, "startLocate");

            new Thread(new Runnable() {

                @Override
                public void run() {

                    if (mLocationClient.isStarted()) {
                        Log.i(ConfigData.Common.tag, "定位sdk已启动");
                        mLocationClient.requestLocation();
                    } else {
                        Log.i(ConfigData.Common.tag, "定位sdk启动失败");
                    }

                }
            }).start();

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
         * @param isForced
         *            当定位结果无效时是否仍然执行回调函数
         */
        public void bindLocationTransfer(LocationTransfer locationTransfer,
                boolean isForced) {
            mLocationTransfer = locationTransfer;
            LocationService.this.isForced = isForced;
        }

        /**
         * 绑定用于处理定位结果的回调函数。<b>仅</b>当一个有效的定位结果出现时，定位服务会调用此函数。
         * 为了防止在出现定位结果之后由于尚未注册回调函数而导致结果处理失败，此函数应在 {@link #startLocate()}
         * 之后立即调用或在启动服务之前调用。
         * 
         * @param locationTransfer
         *            用于处理定位结果的回调函数
         */
        public void bindLocationTransfer(LocationTransfer locationTransfer) {
            bindLocationTransfer(locationTransfer, false);
        }
    }
}

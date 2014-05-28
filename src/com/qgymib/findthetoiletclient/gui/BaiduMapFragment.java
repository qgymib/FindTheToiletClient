package com.qgymib.findthetoiletclient.gui;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.app.ConfigureInfo;
import com.qgymib.findthetoiletclient.app.FTTApplication;
import com.qgymib.findthetoiletclient.data.DataTransfer.LocationTransfer;

public class BaiduMapFragment extends Fragment implements LocationTransfer {
    public static final String fragmentTag = "baidumap";

    /**
     * 根视图，用于承载其他视图
     */
    private View rootView = null;
    /**
     * 百度地图视图
     */
    private MapView mapView = null;
    /**
     * 百度地图控制器
     */
    private MapController mapController = null;
    /**
     * 百度地图监听器
     */
    private MKMapViewListener mMapListener = null;
    /**
     * 承载用户地点信息
     */
    private LocationData locationData = null;
    /**
     * 定位图层
     */
    private MyLocationOverlay locationOverlay = null;
    

    public BaiduMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mapView.onRestoreInstanceState(savedInstanceState);
            return rootView;
        }

        FTTApplication app = (FTTApplication) this.getActivity()
                .getApplication();
        if (app.bMapManager == null) {
            app.bMapManager = new BMapManager(getActivity()
                    .getApplicationContext());
            app.bMapManager.init(new FTTApplication.MyGeneralListener());
        }

        // 注册根视图
        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        // 注册地图视图
        mapView = (MapView) rootView.findViewById(R.id.bmapView);
        // 注册地图控制器
        mapController = mapView.getController();
        // 允许点击
        mapController.enableClick(true);
        // 缩放级别
        mapController.setZoom(12);
        // 中心位置
        mapController.setCenter(new GeoPoint((int) (39.945 * 1E6),
                (int) (116.404 * 1E6)));
        // 启用内置缩放控件
        mapView.setBuiltInZoomControls(true);
        // 初始化定位数据包
        locationData = new LocationData();
        // 初始化定位图层
        locationOverlay = new MyLocationOverlay(mapView);
        // 设置定位数据
        locationOverlay.setData(locationData);
        // 添加定位图层
        mapView.getOverlays().add(locationOverlay);
        // 开启方向功能
        locationOverlay.enableCompass();

        // 注册事件
        mMapListener = new MKMapViewListener() {

            @Override
            public void onMapMoveFinish() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMapLoadFinish() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMapAnimationFinish() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onGetCurrentMap(Bitmap arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onClickMapPoi(MapPoi mapPoiInfo) {
                // TODO Auto-generated method stub
                String title = "";
                if (mapPoiInfo != null) {
                    title = mapPoiInfo.strText;
                    Toast.makeText(getActivity(), title, Toast.LENGTH_SHORT)
                            .show();
                    mapController.animateTo(mapPoiInfo.geoPt);
                }
            }
        };

        // 注册地图监听器
        mapView.regMapViewListener(FTTApplication.getInstance().bMapManager,
                mMapListener);

        return rootView;
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mapView.destroy();
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    

    /**
     * 此函数用于处理接收到的定位信息
     * 
     * @param infoBundle
     *            定位信息的封装
     */
    @Override
    public void transAction(Bundle infoBundle) {
        // 若未完全初始化之前就接受到定位数据，则抛弃此次数据
        if (locationData == null) {
            return;
        }

        // 取得定位精度
        locationData.accuracy = infoBundle
                .getFloat(ConfigureInfo.Location.Key.radius);
        // 取得经度
        locationData.longitude = infoBundle
                .getDouble(ConfigureInfo.Location.Key.longitude);
        // 取得纬度
        locationData.latitude = infoBundle
                .getDouble(ConfigureInfo.Location.Key.latitude);
        // 取得运动方向
        if (infoBundle.getFloat(ConfigureInfo.Location.Key.direction) != 0.0f) {
            locationData.direction = infoBundle
                    .getFloat(ConfigureInfo.Location.Key.direction);
        }

        // 更新定位数据
        locationOverlay.setData(locationData);
        // 刷新图层
        mapView.refresh();
    }
}

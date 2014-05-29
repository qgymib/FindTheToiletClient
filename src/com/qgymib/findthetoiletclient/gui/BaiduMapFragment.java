package com.qgymib.findthetoiletclient.gui;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapStatus;
import com.baidu.mapapi.map.MKMapStatusChangeListener;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKGeocoderAddressComponent;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
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
     * 调试信息
     */
    private TextView infoView = null;
    /**
     * 百度地图控制器
     */
    private MapController mapController = null;
    /**
     * 承载用户地点信息
     */
    private LocationData locationData = null;
    /**
     * 定位图层
     */
    private MyLocationOverlay locationOverlay = null;
    /**
     * 跟踪按键
     */
    private ImageView locateImageView = null;
    /**
     * 搜索模块
     */
    private MKSearch mSearch = null;
    /**
     * 是否跟踪
     */
    private boolean isFollowed = true;

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
        mapController.setZoom(ConfigureInfo.Map.zoom_level);
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
        // 注册跟踪按键
        locateImageView = (ImageView) rootView
                .findViewById(R.id.imageView_locate);

        // 注册跟踪事件
        locateImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 设置状态为跟踪用户地点
                isFollowed = true;
                // 设置图标为跟踪图标
                locateImageView.setImageDrawable(getResources().getDrawable(
                        R.drawable.locate_style_tracked));
                // 定位至用户地点
                mapController.animateTo(new GeoPoint(
                        (int) (locationData.latitude * 1E6),
                        (int) (locationData.longitude * 1E6)));
            }
        });

        // 注册地图监听器
        mapView.regMapViewListener(FTTApplication.getInstance().bMapManager,
                new MKMapViewListener() {

                    @Override
                    public void onMapMoveFinish() {
                        // 用户移动地图时不再自动校准
                        isFollowed = false;
                        // 设置图标为非跟踪图标
                        locateImageView.setImageDrawable(getResources()
                                .getDrawable(R.drawable.locate_style_untracked));
                    }

                    @Override
                    public void onMapLoadFinish() {
                    }

                    @Override
                    public void onMapAnimationFinish() {
                    }

                    @Override
                    public void onGetCurrentMap(Bitmap arg0) {
                    }

                    @Override
                    public void onClickMapPoi(MapPoi mapPoiInfo) {
                        // TODO Auto-generated method stub
                        String title = "";
                        if (mapPoiInfo != null) {
                            title = mapPoiInfo.strText;
                            Toast.makeText(getActivity(), title,
                                    Toast.LENGTH_SHORT).show();
                            mapController.animateTo(mapPoiInfo.geoPt);
                        }
                    }
                });

        mapView.regMapStatusChangeListener(new MKMapStatusChangeListener() {

            @Override
            public void onMapStatusChange(MKMapStatus mapStatus) {
                // 填写相关信息
                PackagedInfo.Zoom = mapStatus.zoom;
                PackagedInfo.Overlooking = mapStatus.overlooking;
                PackagedInfo.Rotate = mapStatus.rotate;
                PackagedInfo.Center = mapStatus.targetGeo;

                showDebugInfo();
            }
        });

        mSearch = new MKSearch();
        mSearch.init(app.bMapManager, new MKSearchListener() {

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result,
                    int iError) {
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult result,
                    int iError) {
            }

            @Override
            public void onGetSuggestionResult(MKSuggestionResult result,
                    int iError) {
            }

            @Override
            public void onGetShareUrlResult(MKShareUrlResult result, int type,
                    int error) {
            }

            @Override
            public void onGetPoiResult(MKPoiResult result, int type, int iError) {
            }

            @Override
            public void onGetPoiDetailSearchResult(int type, int iError) {
            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result,
                    int iError) {
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult result, int iError) {
            }

            @Override
            public void onGetAddrResult(MKAddrInfo result, int iError) {
                // TODO 地理位置反编码
                if (iError != 0) {
                    return;
                }

                // 逆地理编码
                if (result.type == MKAddrInfo.MK_REVERSEGEOCODE) {
                    MKGeocoderAddressComponent mkac = result.addressComponents;
                    PackagedInfo.City = mkac.city;
                }
            }
        });

        infoView = (TextView) rootView.findViewById(R.id.textView_map_info);

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
        // 定位至当前地点
        if (isFollowed) {
            mapController.animateTo(new GeoPoint(
                    (int) (locationData.latitude * 1E6),
                    (int) (locationData.longitude * 1E6)));
        }

        // 更新调试信息
        PackagedInfo.Code = infoBundle
                .getInt(ConfigureInfo.Location.Key.loc_type);
        PackagedInfo.isValid = infoBundle
                .getBoolean(ConfigureInfo.Location.Key.isValid);
        PackagedInfo.Type = infoBundle
                .getString(ConfigureInfo.Location.Key.type);
        PackagedInfo.Time = infoBundle
                .getString(ConfigureInfo.Location.Key.time);
        PackagedInfo.Longitude = infoBundle
                .getDouble(ConfigureInfo.Location.Key.longitude);
        PackagedInfo.Latitude = infoBundle
                .getDouble(ConfigureInfo.Location.Key.latitude);
        PackagedInfo.Radius = infoBundle
                .getFloat(ConfigureInfo.Location.Key.radius);

        // 显示调试信息
        showDebugInfo();
    }

    /**
     * 显示BaiduMap相关的详细信息，仅developer用户可用
     * 
     * @see ConfigureInfo.Account.Permission
     */
    private void showDebugInfo() {
        if (ConfigureInfo.Account.permission == ConfigureInfo.Account.Permission.developer) {
            StringBuffer buffer = new StringBuffer();

            buffer.append("Code: ");
            buffer.append(PackagedInfo.Code);

            buffer.append("\nisValid: ");
            buffer.append(PackagedInfo.isValid);

            buffer.append("\nType: ");
            buffer.append(PackagedInfo.Type);

            buffer.append("\nTime: ");
            buffer.append(PackagedInfo.Time);

            buffer.append("\nLongitude: ");
            buffer.append(PackagedInfo.Longitude);

            buffer.append("\nLatitude: ");
            buffer.append(PackagedInfo.Latitude);

            buffer.append("\nRadius: ");
            buffer.append(PackagedInfo.Radius);

            buffer.append("\nZoom: ");
            buffer.append(PackagedInfo.Zoom);

            buffer.append("\nOverlooking: ");
            buffer.append(PackagedInfo.Overlooking);

            buffer.append("\nRotate: ");
            buffer.append(PackagedInfo.Rotate);

            if (PackagedInfo.Center != null) {

                buffer.append("\nCenter: ");
                buffer.append(PackagedInfo.Center.getLongitudeE6()
                        + "\n\t\t\t\t\t\t\t"
                        + PackagedInfo.Center.getLatitudeE6());
            }

            if (PackagedInfo.City != null) {
                buffer.append("\nCity: ");
                buffer.append(PackagedInfo.City);
            }

            infoView.setText(buffer.toString());
        }

    }

    /**
     * 用于包裹定位信息
     * 
     * @author qgymib
     *
     */
    private static class PackagedInfo {
        /**
         * 信息状态
         */
        public static int Code = -1;
        /**
         * 信息是否有效
         */
        public static boolean isValid = false;
        /**
         * 网络服务商类型
         */
        public static String Type = null;
        /**
         * 时间
         */
        public static String Time = null;
        /**
         * 经度
         */
        public static double Longitude = 0;
        /**
         * 纬度
         */
        public static double Latitude = 0;
        /**
         * 精度
         */
        public static double Radius = 0;
        /**
         * 城市
         */
        public static String City = null;
        /**
         * 地图缩放等级
         */
        public static float Zoom = 0;
        /**
         * 地图俯视角度
         */
        public static int Overlooking = 0;
        /**
         * 地图旋转角度
         */
        public static int Rotate = 0;
        /**
         * 中心点地理坐标
         */
        public static GeoPoint Center = null;
    }

}

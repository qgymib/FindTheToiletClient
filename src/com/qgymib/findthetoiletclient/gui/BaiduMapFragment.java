package com.qgymib.findthetoiletclient.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapStatus;
import com.baidu.mapapi.map.MKMapStatusChangeListener;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
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
import com.qgymib.findthetoiletclient.app.FTTApplication;
import com.qgymib.findthetoiletclient.app.Tools;
import com.qgymib.findthetoiletclient.data.ConfigData;
import com.qgymib.findthetoiletclient.data.DataTransfer.LocationTransfer;
import com.qgymib.findthetoiletclient.service.NetworkService;

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
    /**
     * 定时任务
     */
    private TimerTask task = null;
    /**
     * 定时器
     */
    private Timer timer = null;
    /**
     * 标记是否接受到了定位信息
     */
    private boolean hasLocationData = false;
    /**
     * 最后一次取得的城市地点。用于避免重复获取城市洗手间数据
     */
    private String lastGetCity = null;
    /**
     * 洗手间列表，需要进行排序
     */
    private List<LocationSet> toiletList = new ArrayList<LocationSet>();
    /**
     * 洗手间位置覆盖物
     */
    private MapOverlay overlay = null;
    /**
     * 洗手间覆盖物资源引用
     */
    private int[] icon_mark_resources = { R.drawable.icon_marka,
            R.drawable.icon_markb, R.drawable.icon_markc,
            R.drawable.icon_markd, R.drawable.icon_marke,
            R.drawable.icon_markf, R.drawable.icon_markg,
            R.drawable.icon_markh, R.drawable.icon_marki, R.drawable.icon_markj };

    public BaiduMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化定时器任务
        task = new TimerTask() {

            @Override
            public void run() {
                if (hasLocationData) {
                    mSearch.reverseGeocode(new GeoPoint(
                            (int) (PackagedInfo.Latitude * 1E6),
                            (int) (PackagedInfo.Longitude * 1E6)));
                }
            }
        };
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
        mapController.setZoom(ConfigData.Map.zoom_level);
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
                        locateImageView
                                .setImageDrawable(getResources().getDrawable(
                                        R.drawable.locate_style_untracked));
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

                // 显示调试信息
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
                if (iError != 0) {
                    return;
                }

                // 逆地理编码
                if (result.type == MKAddrInfo.MK_REVERSEGEOCODE) {
                    MKGeocoderAddressComponent mkac = result.addressComponents;
                    PackagedInfo.City = mkac.city;
                }

                // 仅当城市信息不为空且城市信息变动时才向服务器提交搜索请求
                if (PackagedInfo.City != null
                        && (PackagedInfo.City).equals(lastGetCity)) {
                    new SearchTask().execute(Tools.getCRC32(PackagedInfo.City));
                }

                // 显示调试信息
                showDebugInfo();
            }
        });

        infoView = (TextView) rootView.findViewById(R.id.textView_map_info);

        // 初始化定时器
        timer = new Timer();
        timer.schedule(task, 5 * 1000);

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

        // 标记接收到了定位信息
        hasLocationData = true;

        // 取得定位精度
        locationData.accuracy = infoBundle
                .getFloat(ConfigData.Location.Key.radius);
        // 取得经度
        locationData.longitude = infoBundle
                .getDouble(ConfigData.Location.Key.longitude);
        // 取得纬度
        locationData.latitude = infoBundle
                .getDouble(ConfigData.Location.Key.latitude);
        // 取得运动方向
        if (infoBundle.getFloat(ConfigData.Location.Key.direction) != 0.0f) {
            locationData.direction = infoBundle
                    .getFloat(ConfigData.Location.Key.direction);
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
        PackagedInfo.Code = infoBundle.getInt(ConfigData.Location.Key.loc_type);
        PackagedInfo.isValid = infoBundle
                .getBoolean(ConfigData.Location.Key.isValid);
        PackagedInfo.Type = infoBundle.getString(ConfigData.Location.Key.type);
        PackagedInfo.Time = infoBundle.getString(ConfigData.Location.Key.time);
        PackagedInfo.Longitude = infoBundle
                .getDouble(ConfigData.Location.Key.longitude);
        PackagedInfo.Latitude = infoBundle
                .getDouble(ConfigData.Location.Key.latitude);
        PackagedInfo.Radius = infoBundle
                .getFloat(ConfigData.Location.Key.radius);

        // 显示调试信息
        showDebugInfo();
    }

    /**
     * 显示BaiduMap相关的详细信息，仅developer用户可用
     * 
     * @see ConfigData.Account.Permission
     */
    private void showDebugInfo() {
        if (ConfigData.Account.permission == ConfigData.Account.Permission.developer) {
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

    private class MapOverlay extends ItemizedOverlay<OverlayItem> {

        public MapOverlay(Drawable defaultMarker, MapView mapView) {
            super(defaultMarker, mapView);
            // TODO Auto-generated constructor stub
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

    /**
     * 地点信息集
     * 
     * @author qgymib
     *
     */
    private class LocationSet implements Comparable<LocationSet> {
        private GeoPoint point = null;
        private Double distance = 0.0;

        public LocationSet(int latitudeE6, int longitudeE6, double distance) {
            setPoint(latitudeE6, longitudeE6);
            setDistance(distance);
        }

        public GeoPoint getPoint() {
            return point;
        }

        public LocationSet setPoint(int latitudeE6, int longitudeE6) {
            this.point = new GeoPoint(latitudeE6, longitudeE6);
            return this;
        }

        public Double getDistance() {
            return distance;
        }

        public LocationSet setDistance(Double distance) {
            this.distance = distance;
            return this;
        }

        @Override
        public int compareTo(LocationSet another) {
            return getDistance().compareTo(another.getDistance());
        }

    }

    /**
     * 专属搜索任务
     * 
     * @author qgymib
     *
     */
    private class SearchTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // 取得网络服务
            FTTApplication app = (FTTApplication) (getActivity()
                    .getApplication());
            NetworkService networkService = app.getNetworkService();

            return networkService.requrestSearch(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result == null) {
                // 查找失败
            } else {
                // 分割地点
                String[] locationList = result.split("_");
                if (!toiletList.isEmpty()) {
                    // 若列表非空则清空列表
                    toiletList.clear();
                }

                for (int i = 0; i < locationList.length; i++) {
                    String[] coordinate = locationList[i].split(":");
                    // 分离纬度
                    int latitudeE6 = Integer.parseInt(coordinate[0]);
                    // 分离经度
                    int longitudeE6 = Integer.parseInt(coordinate[1]);

                    toiletList.add(new LocationSet(latitudeE6, longitudeE6,
                            Tools.getDistance(latitudeE6, longitudeE6,
                                    (int) (PackagedInfo.Latitude * 1E6),
                                    (int) (PackagedInfo.Longitude * 1E6))));
                }

                // 对列表进行排序
                Collections.sort(toiletList);
            }

            if (overlay == null) {
                overlay = new MapOverlay(getResources().getDrawable(
                        R.drawable.icon_gcoding), mapView);
            } else {
                overlay.removeAll();
            }

            for (int i = 0; i < toiletList.size()
                    && i < ConfigData.Custom.max_show_toilet_num; i++) {
                OverlayItem item = new OverlayItem(
                        toiletList.get(i).getPoint(), "" + i, "");
                item.setMarker(getResources().getDrawable(
                        icon_mark_resources[i]));
                overlay.addItem(item);
            }

            mapView.getOverlays().add(overlay);
            mapView.refresh();
        }
    }

}

package com.qgymib.findthetoiletclient.gui;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.app.FTTApplication;
import com.qgymib.findthetoiletclient.data.DataTransfer.LocationTransfer;
import com.qgymib.findthetoiletclient.service.LocationService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class BaiduMapFragment extends Fragment implements LocationTransfer {
    public static final String fragmentTag = "baidumap";

    private View rootView = null;
    private MapView mapView = null;
    private MapController mapController = null;
    private MKMapViewListener mMapListener = null;
    private ServiceConnection mServiceConnection = null;

    public BaiduMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initService();
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

        rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.bmapView);
        mapController = mapView.getController();
        mapController.enableClick(true);
        mapController.setZoom(12);
        mapController.setCenter(new GeoPoint((int) (39.945 * 1E6),
                (int) (116.404 * 1E6)));

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

    private void initService() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    // TODO 断开服务操作

                }

                @Override
                public void onServiceConnected(ComponentName name,
                        IBinder service) {
                    // 注册定位回调函数
                    ((LocationService.LocationServiceBinder) service)
                            .bindLocationTransfer((LocationTransfer) BaiduMapFragment.this);
                    // 开始定位
                    ((LocationService.LocationServiceBinder) service)
                            .startLocate();
                }
            };
        }
    }

    /**
     * 此函数用于处理{@link LocationService}向{@link BaiduMapFragment}传递的数据，是回调函数，由
     * {@link LocationService}在获取有效结果时调用
     */
    @Override
    public void transAction(Bundle locationInfoBundle) {
        // TODO 百度地图获取到用户坐标的行为

    }
}

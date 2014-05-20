package com.qgymib.findthetoiletclient.gui;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.app.FTTApplication;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class BaiduMapFragment extends Fragment {
    public static final String fragmentTag = "baidumap";
    
    private View rootView = null;
    private MapView mapView = null;
    private MapController mapController = null;
    private MKMapViewListener mMapListener = null;

    /**
     * 所有继承Fragment的子类均应该有一个空的构造函数
     */
    public BaiduMapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
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
        // sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        // ((MainActivity) activity).onSectionAttached(sectionNumber);
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }
}

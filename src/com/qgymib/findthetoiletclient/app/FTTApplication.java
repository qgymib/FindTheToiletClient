package com.qgymib.findthetoiletclient.app;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.qgymib.findthetoiletclient.service.NetworkService;

public class FTTApplication extends Application {

    private static FTTApplication mInstance = null;

    /**
     * 百度地图管理
     */
    public BMapManager bMapManager = null;
    /**
     * LBS密钥有效性
     */
    public boolean m_bKeyRight = true;
    /**
     * 网络线程模块
     */
    private NetworkService networkService = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    /**
     * 初始化网络线程模块
     */
    public void initNetworkService() {
        if (networkService == null) {
            networkService = new NetworkService();
            networkService.init();
        }
    }

    /**
     * 取得网络服务的引用
     * 
     * @return
     */
    public NetworkService getNetworkService() {
        return networkService;
    }

    /**
     * 终止网络线程模块
     */
    public void shutdownNetworkSerivce() {
        if (networkService != null) {
            networkService.shutdown();
            networkService = null;
        }
    }

    public void initEngineManager(Context context) {
        if (bMapManager == null) {
            bMapManager = new BMapManager(context);
        }

        if (!bMapManager.init(new MyGeneralListener())) {
            Toast.makeText(
                    FTTApplication.getInstance().getApplicationContext(),
                    "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 取得应用实例以便在程序任意位置取得context
     * 
     * @return
     */
    public static FTTApplication getInstance() {
        return mInstance;
    }

    public static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                Toast.makeText(
                        FTTApplication.getInstance().getApplicationContext(),
                        "您的网络出错啦！", Toast.LENGTH_LONG).show();
            } else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                Toast.makeText(
                        FTTApplication.getInstance().getApplicationContext(),
                        "输入正确的检索条件！", Toast.LENGTH_LONG).show();
            }
            // ...
        }

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(
                        FTTApplication.getInstance().getApplicationContext(),
                        "请正确配置授权Key,并检查您的网络连接是否正常！error: " + iError,
                        Toast.LENGTH_LONG).show();
                FTTApplication.getInstance().m_bKeyRight = false;
            } else {
                FTTApplication.getInstance().m_bKeyRight = true;
                Toast.makeText(
                        FTTApplication.getInstance().getApplicationContext(),
                        "key认证成功", Toast.LENGTH_LONG).show();
            }
        }
    }
}

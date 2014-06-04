package com.qgymib.findthetoiletclient.gui;

import java.util.ArrayList;
import java.util.List;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.R.id;
import com.qgymib.findthetoiletclient.R.layout;
import com.qgymib.findthetoiletclient.R.menu;
import com.qgymib.findthetoiletclient.R.string;
import com.qgymib.findthetoiletclient.app.FTTApplication;
import com.qgymib.findthetoiletclient.data.ConfigData;
import com.qgymib.findthetoiletclient.data.DataTransfer.LocationTransfer;
import com.qgymib.findthetoiletclient.gui.NavigationDrawerFragment.NavigationDrawerCallbacks;
import com.qgymib.findthetoiletclient.service.LocationService;
import com.qgymib.findthetoiletclient.service.LocationService.LocationServiceBinder;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks, LocationTransfer {

    /**
     * 储存所有fragment列表
     */
    private List<Fragment> fragmentList = new ArrayList<Fragment>();
    /**
     * 储存所有fragmentTag列表
     */
    private List<String> fragmentTagList = new ArrayList<String>();
    /**
     * fragment管理器
     */
    private FragmentManager fragmentManager = null;
    /**
     * 储存自己当前使用的Section索引 当值为-1时，说明这是程序首次运行
     */
    private int currentSectionIndex = -1;
    /**
     * 服务连接器
     */
    private ServiceConnection mServiceConnection = null;
    /**
     * Fragment managing the behaviors, interactions and presentation of the
     * navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in
     * {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(ConfigData.Common.tag, "MainActivity onCreate");

        // 取得配置信息
        ConfigData
                .initPreferences(((FTTApplication) getApplication()).preferences);

        // 启动定位服务
        initService();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 必要的初始化工作
        FTTApplication app = (FTTApplication) getApplication();
        // 初始化线程池
        app.initNetworkService();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 结束线程池
        FTTApplication app = (FTTApplication) getApplication();
        app.shutdownNetworkSerivce();

        // 更新配置信息
        ConfigData
                .updatePreferences(((FTTApplication) getApplication()).preferences);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d(ConfigData.Common.tag,
                "MainActivity onNavigationDrawerItemSelected " + position);
        // 注册fragment管理器
        fragmentManager = getSupportFragmentManager();

        // 若选择的section就是当前的section，则不作任何动作
        if (position == currentSectionIndex) {
            return;
        }

        // 若程序首次运行，则将Fragment添加至列表中
        if (currentSectionIndex == -1) {
            initFragmentList();
        }

        // 隐藏所有不需要的fragment，仅显示当前需要的fragment
        for (int i = 0; i < fragmentList.size(); i++) {
            if (position == i) {
                fragmentManager.beginTransaction().show(fragmentList.get(i))
                        .commit();
            } else {
                fragmentManager.beginTransaction().hide(fragmentList.get(i))
                        .commit();
            }
        }

        currentSectionIndex = position;
        onSectionAttached(position);
    }

    /**
     * 初始化定位服务连接
     */
    private void initService() {
        if (mServiceConnection == null) {
            mServiceConnection = new ServiceConnection() {

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    // TODO 服务异常断开

                }

                @Override
                public void onServiceConnected(ComponentName name,
                        IBinder service) {
                    // 绑定回调函数
                    ((LocationServiceBinder) service).bindLocationTransfer(
                            MainActivity.this, true);
                    // 开启服务
                    ((LocationServiceBinder) service).startLocate();
                }
            };
        }

        // 启动/绑定服务
        Intent intent = new Intent(this, LocationService.class);
        // startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 初始化fragmentList
     */
    private void initFragmentList() {
        fragmentList.add(new BaiduMapFragment());
        fragmentTagList.add(BaiduMapFragment.fragmentTag);

        fragmentList.add(new AccountFragment());
        fragmentTagList.add(AccountFragment.fragmentTag);

        fragmentList.add(new SettingsFragment());
        fragmentTagList.add(SettingsFragment.fragmentTag);

        for (int i = 0; i < fragmentList.size(); i++) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.container, fragmentList.get(i),
                            fragmentTagList.get(i)).commit();
            fragmentManager.beginTransaction().hide(fragmentList.get(i))
                    .commit();
        }
    }

    /**
     * 在section添加到Fragment时用于改变ActionBar的标题
     * 
     * @param sectionNumber
     */
    private void onSectionAttached(int sectionNumber) {
        Log.d(ConfigData.Common.tag, "MainActivity onSectionAttached");
        switch (sectionNumber) {
        case 0:
            mTitle = getString(R.string.section_map);
            break;
        case 1:
            mTitle = getString(R.string.section_account);
            break;
        case 2:
            mTitle = getString(R.string.section_settings);
            break;
        }
    }

    private void restoreActionBar() {
        Log.d(ConfigData.Common.tag, "MainActivity restoreActionBar");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(ConfigData.Common.tag, "MainActivity onCreateOptionsMenu");
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(ConfigData.Common.tag, "MainActivity onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void transAction(Bundle locationInfoBundle) {
        if (fragmentManager != null) {
            // 向BaiduMapFragment分发信息
            ((LocationTransfer) fragmentManager
                    .findFragmentByTag(BaiduMapFragment.fragmentTag))
                    .transAction(locationInfoBundle);
        }
    }
}

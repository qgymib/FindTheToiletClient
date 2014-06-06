package com.qgymib.findthetoiletclient.gui;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.data.ConfigData;
import com.qgymib.findthetoiletclient.data.DataTransfer.ViewTransfer;

/**
 * 用于显示账户相关的fragment。内部包含3个子视图：{@link LoginFragment}、{@link SignupFragment}、
 * {@link InfoFragment}。
 * 
 * @author qgymib
 */
public class AccountFragment extends Fragment implements ViewTransfer {
    public static final String fragmentTag = "account";

    public AccountFragment() {
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

        if (ConfigData.Account.isLogin) {
            viewTransAction(R.layout.fragment_account_info);
        } else {
            viewTransAction(R.layout.fragment_account_login);
        }

        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void viewTransAction(int viewID) {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment targetFragment = null;
        String tag = null;

        // 根据界面布局ID选择需要显示的界面
        switch (viewID) {
        case R.layout.fragment_account_info:
            targetFragment = new InfoFragment();
            tag = InfoFragment.fragmentTag;
            break;

        case R.layout.fragment_account_login:
            targetFragment = new LoginFragment();
            tag = LoginFragment.fragmentTag;
            break;

        case R.layout.fragment_account_signup:
            targetFragment = new SignupFragment();
            tag = SignupFragment.fragmentTag;
            break;

        default:
            break;
        }

        if (targetFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.account_container, targetFragment, tag)
                    .commit();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}

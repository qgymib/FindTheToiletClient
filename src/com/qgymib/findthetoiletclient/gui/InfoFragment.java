package com.qgymib.findthetoiletclient.gui;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.data.ConfigData;
import com.qgymib.findthetoiletclient.data.DataTransfer.ViewTransfer;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * InfoFragment，用于显示账户信息界面以及内部登录事务， {@link AccountFragment}的子视图之一。
 * 
 * @author qgymib
 *
 */
public class InfoFragment extends Fragment {
    public static final String fragmentTag = "info";

    private View containView;

    private TextView usernameTextView;
    private TextView permissionTextView;

    private Button signoutButton;

    public InfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        containView = inflater.inflate(R.layout.fragment_account_info,
                container, false);
        init();
        return containView;
    }

    private void init() {
        usernameTextView = (TextView) containView
                .findViewById(R.id.textView_info_username);
        permissionTextView = (TextView) containView
                .findViewById(R.id.textView_info_permission);

        usernameTextView.setText(ConfigData.Account.username);
        String[] permission = { "normal", "admin", "developer" };
        permissionTextView.setText(permission[ConfigData.Account.permission]);

        signoutButton = (Button) containView.findViewById(R.id.button_signout);
        signoutButton.getBackground().setColorFilter(Color.RED,
                PorterDuff.Mode.MULTIPLY);

        signoutButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ConfigData.Account.username = null;
                ConfigData.Account.permission = ConfigData.Account.Permission.normal;

                AccountFragment fragment = (AccountFragment) getParentFragment();
                ViewTransfer vt = (ViewTransfer) fragment;
                vt.viewTransAction(R.layout.fragment_account_login);
            }
        });
    }
}

package com.qgymib.findthetoiletclient.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.data.ConfigData;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.preference.PreferenceFragment;

/**
 * 设置界面
 * 
 * @author qgymib
 *
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String fragmentTag = "settings";

    private CheckBoxPreference storeZoomLevel;
    private EditTextPreference toiletNumber;
    private EditTextPreference threadpoolsize;
    private EditTextPreference serveraddress;
    private EditTextPreference serverport;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        addPreferencesFromResource(R.xml.fragment_settings);

        toiletNumber = (EditTextPreference) findPreference("max_show_toilet_num");
        toiletNumber
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        Matcher matcher = Pattern.compile(ConfigData.Regex.num)
                                .matcher(newValue.toString());
                        if (matcher.find()) {
                            ConfigData.Custom.max_show_toilet_num = Integer
                                    .parseInt(newValue.toString());
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

        storeZoomLevel = (CheckBoxPreference) findPreference("store_zoom_level");
        storeZoomLevel
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        ConfigData.Custom.isStoreZoomLevel = Boolean
                                .valueOf(newValue.toString());
                        return true;
                    }
                });

        threadpoolsize = (EditTextPreference) findPreference("thread_pool_size");
        threadpoolsize
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        Matcher matcher = Pattern.compile(ConfigData.Regex.num)
                                .matcher(newValue.toString());
                        if (matcher.find()) {
                            ConfigData.Common.thread_pool_size = Integer
                                    .parseInt(newValue.toString());
                            return true;
                        } else {
                            return false;
                        }

                    }
                });
        if (ConfigData.Account.permission == ConfigData.Account.Permission.developer) {
            threadpoolsize.setEnabled(true);
        } else {
            threadpoolsize.setEnabled(false);
        }

        serveraddress = (EditTextPreference) findPreference("server_address");
        serveraddress
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        Matcher matcher = Pattern.compile(ConfigData.Regex.ip)
                                .matcher(newValue.toString());
                        if (matcher.find()) {
                            ConfigData.Net.server_address = newValue.toString();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        if (ConfigData.Account.permission == ConfigData.Account.Permission.developer) {
            serveraddress.setEnabled(true);
        } else {
            serveraddress.setEnabled(false);
        }

        serverport = (EditTextPreference) findPreference("server_port");
        serverport
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        Matcher matcher = Pattern.compile(ConfigData.Regex.num)
                                .matcher(newValue.toString());
                        if (matcher.find()) {
                            ConfigData.Net.server_port = Integer
                                    .parseInt(newValue.toString());
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
        if (ConfigData.Account.permission == ConfigData.Account.Permission.developer) {
            serverport.setEnabled(true);
        } else {
            serverport.setEnabled(false);
        }
    }

}

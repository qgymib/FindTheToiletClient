package com.qgymib.findthetoiletclient.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.data.ConfigData;

import android.os.Bundle;
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

    private EditTextPreference toiletNumber;

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
                        Matcher matcher = Pattern.compile(
                                ConfigData.Regex.max_show_toilet_num).matcher(
                                newValue.toString());
                        if (matcher.find()) {
                            ConfigData.Custom.max_show_toilet_num = Integer
                                    .parseInt(newValue.toString());
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
    }

}

package com.qgymib.findthetoiletclient.gui;

import com.qgymib.findthetoiletclient.R;
import com.qgymib.findthetoiletclient.app.ConfigureInfo;
import com.qgymib.findthetoiletclient.data.DataTransfer.LocationTransfer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DebugFragment extends Fragment implements LocationTransfer {
    public static final String fragmentTag = "debug";

    private View containView = null;
    private TextView debugTextView = null;

    public DebugFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        containView = inflater.inflate(R.layout.fragment_debug, container,
                false);

        debugTextView = (TextView) containView
                .findViewById(R.id.textView_debug);

        return containView;
    }

    @Override
    public void transAction(Bundle locationInfoBundle) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Code:");
        buffer.append(locationInfoBundle
                .getInt(ConfigureInfo.Location.Key.loc_type));

        buffer.append("\nisVaild:");
        buffer.append(locationInfoBundle
                .getBoolean(ConfigureInfo.Location.Key.isValid));

        buffer.append("\nType:");
        buffer.append(locationInfoBundle
                .getString(ConfigureInfo.Location.Key.type));

        buffer.append("\nTime:");
        buffer.append(locationInfoBundle
                .getString(ConfigureInfo.Location.Key.time));

        buffer.append("\nLongitude:");
        buffer.append(locationInfoBundle
                .getDouble(ConfigureInfo.Location.Key.longitude));

        buffer.append("\nLatitude");
        buffer.append(locationInfoBundle
                .getDouble(ConfigureInfo.Location.Key.latitude));
        
        debugTextView.setText(buffer.toString());
    }
}

package com.vishnusivadas.locationtracker.ui.settings;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.vishnusivadas.locationtracker.R;

public class SettingsFragment extends Fragment {

    private Context context;
    private LinearLayout linearLayoutMobileData;
    private AudioManager am;
    private SwitchCompat switchCompatSilent, switchCompatNormal, switchCompatVibrate, switchCompatBlueTooth, switchCompatWiFi;
    WifiManager wifiManager;
    BluetoothAdapter bluetoothAdapter;
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        linearLayoutMobileData = root.findViewById(R.id.mobile_data);
        switchCompatNormal = root.findViewById(R.id.soundon);
        switchCompatSilent = root.findViewById(R.id.silent);
        switchCompatVibrate = root.findViewById(R.id.vibrate);
        switchCompatBlueTooth = root.findViewById(R.id.bluetooth);
        switchCompatWiFi = root.findViewById(R.id.wifi);
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);


        wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            switchCompatWiFi.setChecked(true);
        }
        switchCompatWiFi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    wifiManager.setWifiEnabled(true);
                else
                    wifiManager.setWifiEnabled(false);
            }
        });
        bluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null){
            if (bluetoothAdapter.isEnabled())
                switchCompatBlueTooth.setChecked(true);
        }
        switchCompatBlueTooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    bluetoothAdapter.enable();
                } else
                    bluetoothAdapter.disable();
            }
        });
        switchCompatSilent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    switchCompatNormal.setChecked(false);
                    switchCompatVibrate.setChecked(false);
                } else
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        });
        switchCompatVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    switchCompatNormal.setChecked(false);
                    switchCompatSilent.setChecked(false);
                } else
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        });
        switchCompatNormal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    switchCompatSilent.setChecked(false);
                    switchCompatVibrate.setChecked(false);
                }
            }
        });

        linearLayoutMobileData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
        return root;
    }
}
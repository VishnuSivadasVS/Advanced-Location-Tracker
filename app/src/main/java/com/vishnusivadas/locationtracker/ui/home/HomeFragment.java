package com.vishnusivadas.locationtracker.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.vishnusivadas.locationtracker.BackgroundLocationService;
import com.vishnusivadas.locationtracker.BuildConfig;
import com.vishnusivadas.locationtracker.R;

import static com.vishnusivadas.locationtracker.MainActivity.locationStarted;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private Context context;

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    private Button btnStartTracking;
    private Button btnStopTracking;
    private BackgroundLocationService gpsService;
    private boolean mTracking = false;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        if (isNetworkAvailable()) {
            if (locationStarted == false) {
                final Intent intent = new Intent(getContext(), BackgroundLocationService.class);
                Objects.requireNonNull(getContext()).startService(intent);
                getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            }

            btnStartTracking = root.findViewById(R.id.start_tracking);
            btnStopTracking = root.findViewById(R.id.stop_tracking);
            btnStopTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_red));
            btnStartTracking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLocationButtonClick();
                }
            });
            btnStopTracking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopLocationButtonClick();
                }
            });

        } else noInternetAvailable();
        if (checkPermission()) {
            askPermission();
        }
        return root;
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void noInternetAvailable() {
        final Dialog dialognonet = new Dialog(context);
        dialognonet.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialognonet.setContentView(R.layout.no_internet);
        dialognonet.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            lp.copyFrom(Objects.requireNonNull(dialognonet.getWindow()).getAttributes());
        }
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialognonet.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable())
                    dialognonet.dismiss();
                else
                    dialognonet.setCancelable(false);
            }
        });
        dialognonet.show();
        Objects.requireNonNull(dialognonet.getWindow()).setAttributes(lp);
    }


    @Override
    public void onStart() {
        if (locationStarted == true) {
            final Intent intent = new Intent(getContext(), BackgroundLocationService.class);
            Objects.requireNonNull(getContext()).startService(intent);
            getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            //final Handler handler = new Handler();
            /*handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gpsService.startTracking();
                    mTracking = true;
                    locationStarted = true;
                }
            }, 100);*/

        }
        super.onStart();
    }

    @Override
    public void onDestroy() {
        if (serviceConnection != null) {
            getContext().unbindService(serviceConnection);
            gpsService.stopService(new Intent(getContext(), BackgroundLocationService.class));
            mTracking = false;
        }
        super.onDestroy();
    }

    private void startLocationButtonClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkPermission()) {
                askPermission();
            }
        }
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        gpsService.startTracking();
                        mTracking = true;
                        locationStarted = true;
                        toggleButtons();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void stopLocationButtonClick() {
        mTracking = false;
        locationStarted = false;
        gpsService.stopTracking();
        toggleButtons();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void toggleButtons() {

        btnStartTracking.setEnabled(!mTracking);
        if (!btnStartTracking.isEnabled())
            btnStartTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_red));
        else
            btnStartTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.button_background));
        btnStopTracking.setEnabled(mTracking);
        if (btnStopTracking.isEnabled())
            btnStopTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.button_background));
        else
            btnStopTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_red));


        if (mTracking) {
            final Snackbar snackBar = Snackbar.make(Objects.requireNonNull(getView()).getRootView().findViewById(android.R.id.content), "Tracking Started", Snackbar.LENGTH_SHORT);
            snackBar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackBar.dismiss();
                }
            });
            snackBar.show();
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {

            String name = className.getClassName();

            if (name.endsWith("BackgroundLocationService")) {
                gpsService = ((BackgroundLocationService.LocationServiceBinder) service).getService();
                if (!mTracking) {
                    if (locationStarted) {
                        btnStopTracking.setEnabled(true);
                        btnStopTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.button_background));
                        btnStartTracking.setEnabled(false);
                        btnStartTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_red));
                    } else {
                        btnStartTracking.setEnabled(true);
                        btnStartTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.button_background));
                        btnStopTracking.setEnabled(false);
                        btnStopTracking.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_red));
                    }
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundLocationService")) {
                gpsService = null;
            }
        }
    };
}
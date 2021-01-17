package com.vishnusivadas.locationtracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static com.vishnusivadas.locationtracker.ui.notifications.NotificationsFragment.textView;

@SuppressLint("Registered")
public class BackgroundLocationService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "BackgroundService";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    public static String latitude,longitude;
    public static Date currentTime;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class LocationListener implements android.location.LocationListener {
        private final String TAG = "LocationListener";
        Location mLastLocation;

        LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            currentTime = Calendar.getInstance().getTime();
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            textView.append("\nTime: " + currentTime + "\nLocation: " + latitude + ", " + longitude);
            String type = "loc";
            DataBaseHelper backgroundWorker = new DataBaseHelper(getApplicationContext());
            backgroundWorker.execute(type, latitude, longitude);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(12345678, getNotification());
        } else {
            startForeground(123, getNotification2());
        }
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            int LOCATION_DISTANCE = 10;
            int LOCATION_INTERVAL = 500;
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
        } catch (SecurityException ex) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    public void stopTracking() {
        stopForeground(true);
        this.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {

        NotificationChannel channel;
        channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager;
        notificationManager = getSystemService(NotificationManager.class);

        Objects.requireNonNull(notificationManager).createNotificationChannel(channel);

        Notification.Builder builder;
        builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
        builder.setContentTitle("Location Tracker")
                .setContentText("Live Location Tracking")
                .setSmallIcon(R.drawable.location_on);
        return builder.build();
    }

    private Notification getNotification2() {

        Notification build = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Location Tracker")
                .setContentText("Live Location Tracking")
                .setSmallIcon(R.drawable.location_on)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        return build;
    }


    public class LocationServiceBinder extends Binder {
        public BackgroundLocationService getService() {
            return BackgroundLocationService.this;
        }
    }

}

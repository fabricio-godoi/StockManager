package com.example.stockmanager.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


/**
 * Service to create the NetworkStatusReceiver and keeping it running even when the apk is closed
 */
public class NetworkService extends Service {

    private static final String TAG = NetworkService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Starts internet service to check when the connection comes back to send data to server
        registerReceiver(new NetworkStatusReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Network service destroyed");
    }
}

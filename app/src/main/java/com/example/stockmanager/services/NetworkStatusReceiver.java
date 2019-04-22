package com.example.stockmanager.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.stockmanager.eventbus.MessageCode;
import com.example.stockmanager.eventbus.MessageEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Check for connection status changes, when the network becomes available again
 * it sends a event to notify any application that the internet access has been restored
 */
public class NetworkStatusReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkStatusReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d(TAG, "ConnectivityReceiver invoked...");

                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if (!noConnectivity) {

                    ConnectivityManager cm = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();

                    // only when connected or while connecting...
                    if (netInfo != null && netInfo.isConnectedOrConnecting()) {

                        boolean updateOnlyOnWifi = false;

                        // if we have mobile or wifi connectivity...
                        if (((netInfo.getType() == ConnectivityManager.TYPE_MOBILE) && updateOnlyOnWifi == false)
                                || (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
                            Log.d(TAG, "We have internet, start update check and disable receiver!");

                            // Internet has come back, start pictures synchronization
                            EventBus.getDefault().post(new MessageEvent(MessageCode.FORCE_DATA_SYNCHRONIZATION));
                        }
                    }
            }
        }
    }

}
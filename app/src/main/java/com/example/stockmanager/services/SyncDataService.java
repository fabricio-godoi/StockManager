package com.example.stockmanager.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.stockmanager.R;
import com.example.stockmanager.database.FTPServer;
import com.example.stockmanager.eventbus.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class SyncDataService extends Service {

    private static final String TAG = SyncDataService.class.getSimpleName();

    private static final boolean SHOUDL_DELETE_ON_SYNC = false;

    private FTPServer ftpServer = new FTPServer();

    private static Handler handler = new Handler();
    private Runnable syncData = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            try {
                if(isNetworkAvailable()) {
                    // Save picture taken in the local repository
                    String appName = getResources().getString(R.string.app_name);
                    File localDB = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), appName);

                    File toSync[] = localDB.listFiles();
                    for (File f : toSync) {
                        // Send file
                        Log.d(TAG, "Sending file: "+f.getName());
                        ftpServer.uploadFile(f, new TransferListener(f));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not synchronize the files", e);
            }

            // Check if there is news files each hour to update as well
            handler.postDelayed(syncData, 60*60*1000 );
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler.post(syncData);
        EventBus.getDefault().register(this);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*******  Used to file upload and show progress  **********/
    public class TransferListener implements FTPDataTransferListener {

        File file;

        public TransferListener(@NonNull File file){
            this.file = file;
        }

        public void started() {

            // Transfer started
            Log.d(TAG, " Upload Started ...");
        }

        public void transferred(int length) {

            // Yet other length bytes has been transferred since the last time this
            // method was called
            Log.d(TAG, " transferred ..." + length);
        }

        public void completed() {
            // Transfer completed
            Log.d(TAG," completed ..." );
            if(file != null){
                file.delete();
            }
        }

        public void aborted() {

            // Transfer aborted
            Log.d(TAG," aborted ..." );
        }

        public void failed() {
            // Transfer failed
            Log.d(TAG," failed ..." );

            /// TODO try again
        }

    }

    /**
     * Get events from any service or controller and parse to update the view accordingly
     * @param event is the event generated when some action occurred.
     *              Check MessageCode for more information
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.what) {
            case FORCE_DATA_SYNCHRONIZATION:
                handler.removeCallbacksAndMessages(null);
                handler.post(syncData);
        }
    }

}

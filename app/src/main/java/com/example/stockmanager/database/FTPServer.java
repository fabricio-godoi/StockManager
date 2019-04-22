package com.example.stockmanager.database;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

import android.util.Log;

import com.example.stockmanager.config.Constants;

public class FTPServer {

    private static String TAG = FTPServer.class.getSimpleName();

    private static FTPClient client;

    private boolean connectPerformed = false;

    public FTPServer(){
        client = new FTPClient();
//        connect();
    }


    /**
     * Connect with the FTP server given the parameters in the Constants file
     */
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if(!client.isConnected()) {
                        client.connect(Constants.FTP_HOST, Constants.FTP_PORT);
                        client.login(Constants.FTP_USER, Constants.FTP_PASS);
                        client.setType(FTPClient.TYPE_BINARY);
                        client.setPassive(true);
                        client.noop();
                        client.changeDirectory("");
                    }

                    if(client.isConnected()){
                        Log.e(TAG, "Could not connect to the FTP server");
                    }


                } catch (Exception e) {
                    Log.e(TAG, "Could not perform connection with the FTP server", e);
                    try {
                        if(client.isConnected()) {
                            client.disconnect(true);
                        }
                    } catch (Exception e2) {
                        Log.e(TAG, "Could not disconnect from the service", e2);
                    }
                }

                connectPerformed = true;
            }
        }).start();
    }

    /**
     * Upload data to the FTP Server
     * @param fileName file to be upload
     * @param listener listener to know what happened with the transfer
     */
    public void uploadFile(final File fileName, final FTPDataTransferListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                // If the client is not connect, try to connect first
                client.connect(Constants.FTP_HOST, Constants.FTP_PORT);
                client.login(Constants.FTP_USER, Constants.FTP_PASS);
                client.setType(FTPClient.TYPE_BINARY);
                client.setPassive(true);
                client.noop();
                client.changeDirectory("");

                } catch (Exception e) {
                    Log.e(TAG, "Could not connect with the FTP server", e);
                }

                if(client.isConnected()) {
                    try {
                        client.upload(fileName, listener);
                    } catch (Exception e) {
                        Log.e(TAG, "Could not perform upload", e);
                    }
                } else {
                    Log.e(TAG, "FTP Server connection not performed");
                }
            }
        }).start();

    }

}

package com.example.stockmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.stockmanager.R;
import com.example.stockmanager.eventbus.MessageCode;
import com.example.stockmanager.eventbus.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Despite not being a activity, it needs to extends it to be able to retrieve the data from
 * the camera apk
 */
public class TakePictureActivity extends Activity {

    private static final String TAG = TakePictureActivity.class.getSimpleName();

    private static final int REQUEST_PICTURE_CODE = 1;

    //private Uri capturedImageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        takePicture();
    }

    /**
     * Calls the proper apk to take a picture
     * @return
     */
    public boolean takePicture(){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                Date date = new Date();
                DateFormat df = new SimpleDateFormat("HHmmss");

                // Save picture taken in the local repository
                String appName = getResources().getString(R.string.app_name);
                String fileName = appName + "_" + df.format(date) + ".jpg";
                String filePath = appName + File.separator + fileName;
                File picture = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filePath);
                // Make sure that the path exists
                picture.getParentFile().mkdirs();

                capturedImageUri = Uri.fromFile(picture);

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                startActivityForResult(takePictureIntent, REQUEST_PICTURE_CODE);
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not take the picture", e);
            return false;
        }
        return true;
    }
    Uri capturedImageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.print("Activity result: ");

        if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICTURE_CODE) {
                Log.d(TAG, "Picture taken");

                Uri image = null;

                if (data != null) {
                    image = data.getData();
                }
                if (image == null && capturedImageUri != null) {
                    image = Uri.fromFile(new File(capturedImageUri.getPath()));
                }

                File picture = null;
                if(image != null) {
                    picture = new File(image.getPath());
                }

                // If the picture was found and taken, notify any activity that is waiting for it
                if (picture != null) {
                    EventBus.getDefault().post(new MessageEvent(MessageCode.TAKEPICTURE_CALLBACK, picture));
                    EventBus.getDefault().post(new MessageEvent(MessageCode.FORCE_DATA_SYNCHRONIZATION));
                }
            }
        }

        finish();
    }

}

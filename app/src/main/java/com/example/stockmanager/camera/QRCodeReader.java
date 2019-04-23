package com.example.stockmanager.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.stockmanager.config.Constants;
import com.example.stockmanager.eventbus.MessageCode;
import com.example.stockmanager.eventbus.MessageEvent;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * This class implements all methods to keep checking for some QRCode in a SurfaceHolder.
 * It needs access for the camera of the device.
 */
public class QRCodeReader extends CameraPreview{

    private static final String TAG = QRCodeReader.class.getSimpleName();
    private static Context CONTEXT;

    /// Check if a QRCode is found in the current run
    transient private boolean qrCodeFound = false;

    // QRCode detector
    private static BarcodeDetector detector;

    /**
     * This searches for QRCode in the frames of the camera preview
     */
    private boolean isSearchRunning = false;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            qrCodeFound = false;
            if (getCamera() != null) {
                try {
                    getCamera().setOneShotPreviewCallback(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            // When a new frame arrives, transform it to a bitmap image to be parsed
                            Camera.Parameters parameters = camera.getParameters();
                            int width = parameters.getPreviewSize().width;
                            int height = parameters.getPreviewSize().height;
                            YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
                            byte[] bytes = out.toByteArray();
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                            // Check if the image rotation is right, checking with the device rotation
                            final int rotation = ((WindowManager) CONTEXT.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                            switch (rotation) {
                                case Surface.ROTATION_0:
                                    bitmap = rotateImage(bitmap, 90);
                                    break;
                                case Surface.ROTATION_90:
                                    break;
                                case Surface.ROTATION_180:
                                    bitmap = rotateImage(bitmap, 270);
                                    break;
                                case Surface.ROTATION_270:
                                    bitmap = rotateImage(bitmap, 180);
                                    break;
                                default:
                                    break;
                            }

                            // Parse the image frame, to check if the has any QRCode in it
                            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                            SparseArray<Barcode> barcodes = detector.detect(frame);

                            // Check if there is any QRCode found
                            if (barcodes.size() > 0) {
                                // QRCode found, stop searching for it
                                qrCodeFound = true;
                                isSearchRunning = false;
                                searchHandler.removeCallbacksAndMessages(null);

                                try {
                                    // Send barcode information for the right application view
                                    Barcode thisCode = barcodes.valueAt(0);
                                    saveQRCode(thisCode.rawValue);
                                    EventBus.getDefault().post(new MessageEvent(MessageCode.QRCODE_CALLBACK, "RESULT", thisCode.rawValue));
                                } catch (Exception e) {
                                    Log.e(TAG, "Could not send back the QRCode result", e);
                                }
                            }
                        }
                    });

                    // While the QRCode are not found, keep searching it in Constants.QRCODE_SEARCH_INTERVAL_MS interval
                    if (!qrCodeFound) {
                        searchHandler.postDelayed(searchRunnable, Constants.QRCODE_SEARCH_INTERVAL_MS);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Camera should not be available");
                }
            }
        }
    };


    public QRCodeReader(@NonNull Context context){
        super(context);
        this.CONTEXT = context;
        detector = new BarcodeDetector.Builder(CONTEXT)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
    }


    /**
     * Rotate bitmap image by the provided angle
     * @param source image to be rotated
     * @param angle angle in degrees (eg. 90, 180,  270 ...)
     * @return the bitmap image rotated by the angles provided
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    /**
     * Start camera preview and start searching for QRCodes
     */
    public void start(){
        qrCodeFound = false;
        try {
            super.startPreviewing();
            isSearchRunning = true;
            searchHandler.post(searchRunnable);
        } catch (Exception e){
            Log.e(TAG, "Could not start QRCode Reader", e);
        }
    }

    /**
     * Stop gathering image from the camera
     */
    public void stop(){
        super.stopPreviewing();
        try{
            searchHandler.removeCallbacksAndMessages(null);
            isSearchRunning = false;
        } catch (Exception e) {
            Log.e(TAG, "Could not stop search engine", e);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isSearchRunning) {
            searchHandler.post(searchRunnable);
            isSearchRunning = true;
            EventBus.getDefault().post(new MessageEvent(MessageCode.QRCODE_CALLBACK, "RESTARTED"));
        }
        return super.onTouchEvent(event);
    }


    public String hasQRCode(Bitmap bitmap){
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        // Check if there is any QRCode found
        if (barcodes.size() > 0) {
            try {
                // Send barcode information for the right application view
                Barcode thisCode = barcodes.valueAt(0);
                saveQRCode(thisCode.rawValue);
                return thisCode.rawValue;
            } catch (Exception e) {
                Log.e(TAG, "Could not send back the QRCode result", e);
            }
        }

        return null;
    }

    /**
     * Check if a picture has some QRCode.
     * If a QRCode is found and decoded, it will be returned.
     * @param picture file that has the picture
     * @return null if could not decode the picture, otherwise the decoded value
     */
    public String hasQRCode(File picture){
        if (picture != null && picture.isFile()) {
            Bitmap bitmap = BitmapFactory.decodeFile(picture.getPath());
            // If it was possible to decode the file, then is a picture
            // if  bitmap is null, then is not a picture
            if(bitmap != null) {
                return hasQRCode(bitmap);
            }
        }
        return  null;
    }

    /**
     * Save all QRCodes read
     * @param qrcode string value of the qrcode
     */
    private void saveQRCode(String qrcode){
        SharedPreferences settings = CONTEXT.getSharedPreferences("qrcode_list", MODE_PRIVATE);
        Set<String> qrCodes = new HashSet<>();
        qrCodes = settings.getStringSet("qrcode_list", qrCodes);
        if(!qrCodes.contains(qrcode)){
            qrCodes.add(qrcode);
            SharedPreferences.Editor editor = settings.edit();
            editor.putStringSet("qrcode_list", qrCodes);
            editor.commit();
        }
    }

}

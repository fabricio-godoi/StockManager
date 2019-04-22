package com.example.stockmanager.camera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraPreview.class.getSimpleName();

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean mIsPreviewing = false;
    private boolean isEnabled = false;


    public CameraPreview(Context context) {
        super(context);

        initialize();
    }

    private void initialize(){
        mCamera = Camera.open();
        if(mCamera != null) {
            mCamera.setDisplayOrientation(90);

            // Set camera to continually auto-focus
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);

            mHolder = getHolder();
            mHolder.addCallback(this);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start mCamera preview
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting mCamera preview: " + e.getMessage());
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
//        refreshCamera(mCamera);

        if (mIsPreviewing) {
            mCamera.stopPreview();
            mIsPreviewing = false;
        }

        if (mCamera != null) {
            // Check for the best size of the camera
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size bestSize = getBestPreviewSize(w, h, parameters);

            if (bestSize != null) {
                parameters.setPreviewSize(bestSize.width, bestSize.height);
                mCamera.setParameters(parameters);
            }
            try {
                mCamera.setPreviewDisplay(holder);
                if(isEnabled) {
                    mCamera.startPreview();
                    mIsPreviewing = true;
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not start camera preview", e);
            }
        }
    }

    public void startPreviewing(){
        isEnabled = true;
        if(getCamera() == null){
            initialize();
            refreshCamera();
        }
        if(getCamera() != null) {
            getCamera().startPreview();
        }
    }

    public void stopPreviewing(){
        isEnabled = false;
        if(getCamera() != null) {
            getCamera().stopPreview();
            getCamera().release();
            setCamera(null);
            mIsPreviewing = false;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }
        return bestSize;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    /**
     * If the camera has been stoped, this should be called
     */
    public void refreshCamera() {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Error starting mCamera preview: ",e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        refreshCamera();
        return super.onTouchEvent(event);
    }

    /**
     * Set camera being used
     * @param camera
     */
    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    /**
     * Get camera being used
     * @return
     */
    public Camera getCamera() {
        return mCamera;
    }


}
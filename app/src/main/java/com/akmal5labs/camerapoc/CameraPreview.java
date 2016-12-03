package com.akmal5labs.camerapoc;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by azlan on 3/12/2016.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    String LOG_TAG = "Camera Direct Preview";

    Camera camera;
    SurfaceHolder holder;

    public CameraPreview(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CameraPreview(Context context) {
        super(context);
    }

    public void connectCamera(Camera camera, int cameraId) {
        this.camera = camera;

        int previewOrientation = getCameraPreviewOrientation(cameraId);
        camera.setDisplayOrientation(previewOrientation);

        holder = getHolder();
        holder.addCallback(this);

        // Start Preview
        startPreview();
    }

    public void releaseCamera() {
        if (camera != null) {
            //Stop Preview
            stopPreview();

            camera = null;
        }
    }

    void startPreview() {
        if (camera != null && holder.getSurface() != null) {
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error setting preview display: " + e.getMessage());
            }
        }
    }

    void stopPreview() {
        if (camera != null) {
            try {
                camera.stopPreview();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error stoping preview: " + e.getMessage());
            }
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        stopPreview();
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopPreview();
    }

    int getCameraPreviewOrientation(int cameraId) {
        final int DEGRESS_IN_CIRCLE = 360;
        int temp = 0;
        int previewOrientation = 0;

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int deviceOrientation = getDeviceOrientationDegress();
        switch (cameraInfo.facing) {
            case Camera.CameraInfo.CAMERA_FACING_BACK:
                temp = cameraInfo.orientation - deviceOrientation + DEGRESS_IN_CIRCLE;
                previewOrientation = temp % DEGRESS_IN_CIRCLE;
                break;
            case Camera.CameraInfo.CAMERA_FACING_FRONT:
                temp = (cameraInfo.orientation + deviceOrientation) % DEGRESS_IN_CIRCLE;
                previewOrientation = (DEGRESS_IN_CIRCLE - temp) % DEGRESS_IN_CIRCLE;
                break;
        }

        return previewOrientation;
    }

    int getDeviceOrientationDegress() {
        int degrees = 0;
        WindowManager windowManager =
                (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        return degrees;
    }
}

package com.akmal5labs.camerapoc;

import android.content.Context;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Jim
 * Date: 3/25/13
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CameraHelper {
    static final String LOG_TAG = "Camera Direct Access" ;

    public static int getDisplayOrientationForCamera(Context context, int cameraId) {
        final int DEGREES_IN_CIRCLE = 360;
        int temp = 0;
        int previewOrientation = 0;


        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int deviceOrientation = getDeviceOrientationDegrees(context);
        switch(cameraInfo.facing) {
            case Camera.CameraInfo.CAMERA_FACING_BACK:
                temp = cameraInfo.orientation - deviceOrientation + DEGREES_IN_CIRCLE;
                previewOrientation = temp % DEGREES_IN_CIRCLE;
                break;
            case Camera.CameraInfo.CAMERA_FACING_FRONT:
                temp = (cameraInfo.orientation + deviceOrientation) % DEGREES_IN_CIRCLE;
                previewOrientation = (DEGREES_IN_CIRCLE - temp) % DEGREES_IN_CIRCLE;
                break;
        }

        return previewOrientation;
    }

    public static int getDeviceOrientationDegrees(Context context) {
        int degrees = 0;

        WindowManager windowManager =
            (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        switch(rotation) {
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

    public static File getPhotoDirectory()  {
        File outputDir = null;
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            File pictureDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            outputDir = new File(pictureDir, "Pluralsight");
            if(!outputDir.exists())  {
                if(!outputDir.mkdirs()) {
                    String message = "Failed to create directory:" + outputDir.getAbsolutePath();
                    Log.e(LOG_TAG, message);
                    outputDir = null;
                }
            }
        }

        return outputDir;
    }

    public static File generateTimeStampPhotoFile() {
        File photoFile = null;
        File outputDir = getPhotoDirectory();

        if (outputDir != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String photoFileName =  "IMG_" + timeStamp + ".jpg";

            photoFile = new File(outputDir, photoFileName);
        }

        return photoFile;
    }

    public static Uri generateTimeStampPhotoFileUri() {
        Uri photoFileUri = null;
        File photoFile = generateTimeStampPhotoFile() ;

        if (photoFile != null) {
            photoFileUri = Uri.fromFile(photoFile);
        }

        return photoFileUri;
    }

}

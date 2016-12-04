package com.akmal5labs.camerapoc;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "Camera Direct Access";
    final String SELECTED_CAMERA_ID_KEY = "selectedCameraId";
    final int CAMERA_ID_NOT_SET = -1;

    int frontFacingCameraId = CAMERA_ID_NOT_SET;
    int backFacingCameraId = CAMERA_ID_NOT_SET;

    boolean hasCamera = false;
    boolean hasFrontCamera = false;

    int selectedCameraId = CAMERA_ID_NOT_SET;
    Camera selectedCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PackageManager pm = getPackageManager();
        hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
        hasFrontCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);

        if (!hasCamera) {
            showNoCameraDialog();
        }

        if (savedInstanceState != null) {
            selectedCameraId = savedInstanceState.getInt(SELECTED_CAMERA_ID_KEY, CAMERA_ID_NOT_SET);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_CAMERA_ID_KEY, selectedCameraId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (!hasCamera) {
            disableCameraMenuItems(menu);
        } else if (!hasFrontCamera) {
            disableFrontCameraMenuItems(menu);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        releaseSelectedCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openSelectedCamera();
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    public void onMenuOpenBackCamera(MenuItem item) {
        logMenuChoice(item);

        selectedCameraId = getBackFacingCameraId();
        openSelectedCamera();
    }

    public void onMenuOpenFrontCamera(MenuItem item) {
        logMenuChoice(item);

        selectedCameraId = getFrontFacingCameraId();
        openSelectedCamera();
    }

    public void onMenuCloseCamera(MenuItem item) {
        logMenuChoice(item);

        releaseSelectedCamera();
        selectedCameraId = CAMERA_ID_NOT_SET;
    }

    public void onExit(MenuItem item) {
        logMenuChoice(item);

        releaseSelectedCamera();

        finish();
    }

    private void logMenuChoice(MenuItem item) {
        CharSequence menuTitle = item.getTitle();
        Log.d(LOG_TAG, "Menu item selected:" + menuTitle);
    }

    private void showNoCameraDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Camera");
        builder.setMessage("Device does not have required camera support. Some features will not be available");
        builder.setPositiveButton("Continue", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void disableCameraMenuItems(Menu menu) {
        menu.findItem(R.id.menuOpenBackCamera).setEnabled(false);
        menu.findItem(R.id.menuOpenFrontCamera).setEnabled(false);
        menu.findItem(R.id.menuCloseCamera).setEnabled(false);
    }

    private void disableFrontCameraMenuItems(Menu menu) {
        menu.findItem(R.id.menuOpenFrontCamera).setEnabled(false);
    }

    int getFacingCameraId(int facing) {
        int cameraId = CAMERA_ID_NOT_SET;

        int nCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        for (int cameraInfoId = 0; cameraInfoId < nCameras; cameraInfoId++) {
            Camera.getCameraInfo(cameraInfoId, cameraInfo);
            if (cameraInfo.facing == facing) {
                cameraId = cameraInfoId;
                break;
            }
        }
        return cameraId;
    }

    void releaseSelectedCamera() {
        if (selectedCamera != null) {
            CameraPreview cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
            cameraPreview.releaseCamera();

            selectedCamera.release();
            selectedCamera = null;
        }
    }

    int getFrontFacingCameraId() {
        if (frontFacingCameraId == CAMERA_ID_NOT_SET) {
            frontFacingCameraId = getFacingCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        return frontFacingCameraId;
    }

    int getBackFacingCameraId() {
        if (backFacingCameraId == CAMERA_ID_NOT_SET) {
            backFacingCameraId = getFacingCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        return backFacingCameraId;
    }

    void openSelectedCamera() {
        String message;

        releaseSelectedCamera();
        if (selectedCameraId != CAMERA_ID_NOT_SET) {
            try {
                selectedCamera = Camera.open(selectedCameraId);
                message = String.format("Opened Camera ID: %d", selectedCameraId);

                CameraPreview cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
                cameraPreview.connectCamera(selectedCamera, selectedCameraId);
            } catch (Exception ex) {
                message = "Unable to open Camera: " + ex.getMessage();
                Log.e(LOG_TAG, message);
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

    }

    public void onTakePicture(MenuItem item) {
        selectedCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                onPictureJpeg(bytes, camera);
            }
        });
    }

    void onPictureJpeg(byte[] bytes, Camera camera) {
        String userMessage = null;
        int i = bytes.length;
        Log.d(LOG_TAG, String.format("bytes = %d", i));

        File f = CameraHelper.generateTimeStampPhotoFile();

        try {
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(f));
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            userMessage = "Picture saved as " +  f.getName();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error accessing photo output file:" + e.getMessage());
            userMessage = "Error saving photo";
        }

        if (userMessage != null) {
            Toast.makeText(this, userMessage, Toast.LENGTH_SHORT).show();
        }

        selectedCamera.startPreview();

    }
}

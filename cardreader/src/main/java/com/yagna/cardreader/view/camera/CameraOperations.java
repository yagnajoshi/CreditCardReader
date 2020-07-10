package com.yagna.cardreader.view.camera;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextRecognizer;
import com.yagna.cardreader.view.camera.overlay.GraphicOverlay;

import java.io.IOException;

public class CameraOperations
{
    private Activity activity;
    private static final String TAG = "OcrCaptureActivity";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    // Intent request code to handle updating play services if needed.
    // Permission request codes need to be < 256
    private static final int RC_HANDLE_GMS = 9001;


    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";
    private OcrDetectorProcessor ocrProcessor;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    public CameraOperations(Activity ocrCaptureActivity,  GraphicOverlay<OcrGraphic> mGraphicOverlay,CameraSourcePreview mPreview) {
        activity=ocrCaptureActivity;
        this.mGraphicOverlay = mGraphicOverlay;
        this.mPreview = mPreview;
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    public void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = activity;
        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        ocrProcessor = new OcrDetectorProcessor(mGraphicOverlay,activity);
        textRecognizer.setProcessor(ocrProcessor);
        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = activity.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(activity, "Ocr dependencies cannot be downloaded due to low device storage", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Ocr dependencies cannot be downloaded due to low device storage");
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource =
                new CameraSource.Builder(activity, textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }



    /**
     * Restarts the camera.
     */
    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */

    public void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                activity);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void readingStart (){
        ocrProcessor.readingState = ocrProcessor.ON;
    }
    /*public void readingStop(){
        ocrProcessor.txtOperation.activity.onResetCardReading();
        ocrProcessor.readingState = ocrProcessor.OFF;
    }*/
}

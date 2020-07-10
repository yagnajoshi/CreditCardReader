/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yagna.cardreader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.yagna.cardreader.view.camera.CameraSourcePreview;
import com.yagna.cardreader.view.camera.overlay.GraphicOverlay;
import com.yagna.cardreader.view.camera.OcrGraphic;
import com.yagna.cardreader.view.camera.OnCardCaptureListner;
import com.yagna.cardreader.view.camera.CameraOperations;

import static com.yagna.cardreader.view.camera.CameraOperations.AutoFocus;
import static com.yagna.cardreader.view.camera.CameraOperations.UseFlash;

/**
 * Activity for the Ocr Detecting app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class OcrCaptureActivity extends AppCompatActivity implements OnCardCaptureListner {


    private static final String TAG = "OcrCaptureActivity";
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;
    private TextView startTxt;
    EditText name,cardno,bankname,validtill,bin,extra,brand,valid,cardtype,level,phone,country,web;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    private ImageView blackLayer;
    private CameraSourcePreview mPreview;
    private CameraOperations cameraOperation;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ocr_capture);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);
        name = (EditText) findViewById(R.id.name);
        cardno = (EditText) findViewById(R.id.cardno);
        brand = (EditText) findViewById(R.id.brand);
        bankname = (EditText) findViewById(R.id.bankname);
        validtill = (EditText) findViewById(R.id.validtill);
        bin = (EditText) findViewById(R.id.bin);
        extra = (EditText) findViewById(R.id.extra);
        cardtype = (EditText) findViewById(R.id.cardtype);

        valid = (EditText) findViewById(R.id.valid);
        cardtype = (EditText) findViewById(R.id.cardtype);
        level = (EditText) findViewById(R.id.level);
        country = (EditText) findViewById(R.id.country);
        phone = (EditText) findViewById(R.id.phone);
        web = (EditText) findViewById(R.id.web);


        startTxt =(TextView) findViewById(R.id.startTxt);
        blackLayer = (ImageView)  findViewById(R.id.blackLayer);
        startTxt.setText("Tap to Start");

        startTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraOperation.readingStart();
                blackLayer.setVisibility(View.GONE);
                startTxt.setVisibility(View.GONE);
            }
        });

        // Set good defaults for capturing text.
        boolean autoFocus = true;
        boolean useFlash = false;

        cameraOperation = new CameraOperations(this, mGraphicOverlay, mPreview);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            cameraOperation.createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }
    }





    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    @Override
    protected void onResume() {
        super.onResume();
       cameraOperation.startCameraSource();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            cameraOperation.createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Card reader")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }




    @Override
    public void onCardRead(final String cardNumber, final String cardhHolderName, final String validTillMonth, final String validTillYear) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                name.setText(cardhHolderName);
                cardno.setText(cardNumber);
                validtill.setText(validTillMonth+"/"+validTillYear);
            }
        });

    }

    @Override
    public void onResetCardReading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blackLayer.setVisibility(View.VISIBLE);
                startTxt.setText("Tap to Retry");
                startTxt.setVisibility(View.VISIBLE);
            }
        });
    }

}

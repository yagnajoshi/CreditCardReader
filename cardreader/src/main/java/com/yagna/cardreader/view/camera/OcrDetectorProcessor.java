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
package com.yagna.cardreader.view.camera;

import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

/**
 * A very simple Processor which gets detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {


    private static final String TAG = "OcrDetectorProcessor";
    private OnCardCaptureListner onCardCaptureListner;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    public TextOperations txtOperation;
    public static final int ON = 1;
    public static final int OFF = 0;
    public int readingState = OFF;

    public OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, OnCardCaptureListner ocrCaptureActivity) {
        mGraphicOverlay = ocrGraphicOverlay;
         onCardCaptureListner =ocrCaptureActivity;
        txtOperation=new TextOperations((OnCardCaptureListner) onCardCaptureListner, this);
    }

    public void setOnCardCaptureListner(OnCardCaptureListner onCardCaptureListner) {
        this.onCardCaptureListner = onCardCaptureListner;

        if(txtOperation !=null) {
            txtOperation.setOnCardCaptureListner(onCardCaptureListner);
        }
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        if(readingState==ON) {
            SparseArray<TextBlock> items = detections.getDetectedItems();
            txtOperation.grabText(items);
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                if (item != null && item.getValue() != null) {
                    Log.e("OcrDetectorProcessor", "Text detected! " + item.getValue());
                }
                OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
                mGraphicOverlay.add(graphic);
            }
            Log.e("OcrDetectorProcessor", "Text detected!---------------------------------------- ");
        }
    }

    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}

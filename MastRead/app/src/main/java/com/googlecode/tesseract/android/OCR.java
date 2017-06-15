/*
 * Copyright (C) 2012,2013 Renard Wellnitz.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.tesseract.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.rupesh.mastread.R;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.WriteFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OCR  {

    private static final String TAG = OCR.class.getSimpleName();
    public static final String EXTRA_WORD_BOX = "word_box";
    public static final String EXTRA_OCR_BOX = "ocr_box";
    private static final String LOG_TAG = OCR.class.getSimpleName();
    private TessBaseAPI baseApi;

    static {
        //System.loadLibrary("pngo");
        //System.loadLibrary("lept");
        //System.loadLibrary("tess");
        System.loadLibrary("image_processing_jni");
        nativeInit();

    }

    public OCR(String path) {
        baseApi = new TessBaseAPI();
        Log.d(TAG, "done create OCRtest");
        baseApi.init(path, "eng");
        Log.d(TAG, "done init OCRtest");
    }

    private void prepareOCR(String storageDirPath, Context context) {

        String tessDataDirPath = storageDirPath + "/" + "tessdata";
        File tessDataDir = new File (tessDataDirPath);

        if (!tessDataDir.exists()) {
            tessDataDir.mkdirs();
        }

        String tessDataFilePath = tessDataDirPath + "/" + "eng.traineddata";
        File tessDataFile = new File(tessDataFilePath);


        if (!tessDataFile.exists()) {

            InputStream in = (InputStream) context.getResources().openRawResource(R.raw.eng);
            try {
                OutputStream out = new FileOutputStream(tessDataFile);
                byte buf[] = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Log.d(TAG, "Done copying eng.traineddata");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void doOCRTest(final Bitmap bmap) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                assert(bmap != null);

                Bitmap bmapCopy = bmap.copy(bmap.getConfig(), true);
                baseApi.setImage(bmapCopy);

                Log.d(TAG, "done setImage OCRtest");

                String recognizedText = baseApi.getUTF8Text();

                for (int iter = 0; iter < recognizedText.length(); iter++) {
                    Log.i(TAG, Character.toString(recognizedText.charAt(iter)));
                }

                Log.d(TAG, "------------------------- Recognized text length - " + recognizedText.length());


                baseApi.end();
            }
        };

        new Thread(runnable).start();

    }
    /**
     * native code takes care of the Pix, do not use it after calling this
     * function
     *
     * */
    public Bitmap preProcessImageUsingTextFairyMagic(Bitmap bmap) {
        if (bmap == null) {
            throw new IllegalArgumentException("Source pix must be non-null");
        }

        Pix sourcePix = ReadFile.readBitmap(bmap);
        long nativeTextPix = nativeOCRBook(sourcePix.getNativePix());
        Pix pixText = new Pix(nativeTextPix);

        Bitmap retBmap =  WriteFile.writeBitmap(pixText);

        return retBmap;

    }

    // Hacks to satisfy JNI
    private synchronized void onProgressImage(final long nativePix) {}
    public void onProgressValues(final int percent, final int left, final int right, final int top, final int bottom, final int left2, final int right2, final int top2, final int bottom2) {}
    private void onProgressText(int id) {}
    private void onLayoutElements(int nativePixaText, int nativePixaImages) {}
    private void onUTF8Result(String utf8Text) {}
    private void onLayoutPix(long nativePix) {}

        private static native void nativeInit();

    /**
     * takes ownership of nativePix.
     *
     * @return binarized and dewarped version of input pix
     */
    private native long nativeOCRBook(long nativePix);

    private native long[] combineSelectedPixa(long nativePixaTexts, long nativePixaImages, int[] selectedTexts, int[] selectedImages);

    private native long nativeAnalyseLayout(long nativePix);

}

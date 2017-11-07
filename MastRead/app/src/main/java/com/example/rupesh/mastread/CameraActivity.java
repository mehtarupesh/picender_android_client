package com.example.rupesh.mastread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.googlecode.tesseract.android.OCR;

import java.io.File;
import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity implements OCRCallback {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String TAG = "CameraActivity";
    private ImageView mrImageView;
    private Bitmap mrImageBitMap;
    private ArrayList<Bitmap> mrSubImagesBitMap;
    private File tempImageFile;
    static File storageDir;
    private OCR mrOCR;
    private int subImageIndex;
    private int factorBy = 8;
    private long startTimeMs;
    private Runnable mrUIRunnable;
    private Bitmap mrUIRunnableBitmap;
    private ContentManagementEngine mrCme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mrImageView = (ImageView)findViewById(R.id.imageView);
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        mrSubImagesBitMap = new ArrayList<>();
        mrCme = ContentManagementEngine.getContentManagementEngine(getApplicationContext());

        mrOCR = new OCR(storageDir.getAbsolutePath(), getApplicationContext(), this);
        takePictureViaIntent();

        mrUIRunnable = new Runnable() {
            @Override
            public void run() {
                mrImageView.setImageBitmap(mrUIRunnableBitmap);
            }
        };
    }

    private int getImageIndex(int i) {
        assert(i >= 0 && i < factorBy);

        int sign = (i % 2 == 0) ? 1 : -1;
        int offset = i / 2;

        int ret = factorBy / 2 + (sign * offset);

        Log.d(TAG, "subImageIndex = " + i);
        Log.d(TAG, "converted to new val = " + ret);
        Log.d(TAG, "--------------------");


        assert(ret >= 0 && ret < factorBy);


        return ret;

    }

    private void takePictureViaIntent() {

        String tempFileName = "mytemp.jpg";
        tempImageFile = new File(storageDir + "/" + tempFileName);
        //Log.d(TAG, "storageDir :" + storageDir.toString());
        //Log.d(TAG, "tempImagePath :" + tempImageFile.getAbsolutePath());

        if (tempImageFile != null) {
            Uri photoURI = Uri.fromFile(tempImageFile);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                tempImageFile.delete();
            }
        }
    }

    private ArrayList<Bitmap> splitBitmap(Bitmap source) {

        int height = source.getHeight();
        int width = source.getWidth();
        int newHeight = height / factorBy;
        ArrayList<Bitmap> ret = new ArrayList<>();

        int x = 0;
        int y = 0;
        for (int i = 0; i < factorBy; i++) {

            if (i == factorBy - 1) {
                newHeight = height - i * newHeight;
            }

            Bitmap img = Bitmap.createBitmap(source,x, y, width, newHeight);
            ret.add(img);
            y = y + newHeight;

        }

        return ret;
    }

    private void runOcr(Bitmap bmap) {

        mrUIRunnableBitmap = bmap;
        runOnUiThread(mrUIRunnable);

        startTimeMs = System.currentTimeMillis();
        mrOCR.doOCR(bmap);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "req code " + requestCode + " resultCode " + resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Log.d(TAG, "Image size = " + tempImageFile.length());
            mrImageBitMap = SimpleImageProcessing.preProcessImage(tempImageFile.getAbsolutePath());

            // done with tempFile, delete it.
            tempImageFile.delete();

            Log.d(TAG, "Calling TextFairy");
            mrImageBitMap = mrOCR.preProcessImageUsingTextFairyMagic(mrImageBitMap);

            mrSubImagesBitMap = splitBitmap(mrImageBitMap);

            subImageIndex = 0;
            runOcr(mrSubImagesBitMap.get(getImageIndex(subImageIndex)));
            mrCme.startSearch();
        } else {

            // Go back to parent activity
            NavUtils.navigateUpFromSameTask(this);

        }
    }


    @Override
    public void ocrCallback(String ocrText) {

        long endTimeMs = System.currentTimeMillis();
        Log.d(TAG, "SUBMIMAGE ^^^ " + getImageIndex(subImageIndex));
        Log.d(TAG, "OCR time taken in ms =" + (endTimeMs - startTimeMs));

        //for (int iter = 0; iter < ocrText.length(); iter++) {
        //    Log.i(TAG, Character.toString(ocrText.charAt(iter)));
        //}

        //Log.d(TAG, "------------------------- Recognized text length - " + ocrText.length());

        Log.d(TAG, "Doing OCR search");
        Page ret = mrCme.search(ocrText);

        if (ret != null) {
            Log.d(TAG, "FOUND PAGE!!\n " + ret.toString());
            mrCme.endSearch();
            mrUIRunnableBitmap = mrImageBitMap;
            runOnUiThread(mrUIRunnable);

            if (ret.getAudioIsOnDevice()) {
                Log.d(TAG, "cme says audio file exists, going to playback!\n");
                Intent intent = new Intent(CameraActivity.this, PlayBackActivity.class);
                intent.putExtra("PAGE_INFO", ret);
                startActivity(intent);
            }

        } else {

            subImageIndex++;
            if (subImageIndex < factorBy) {
                runOcr(mrSubImagesBitMap.get(getImageIndex(subImageIndex)));
            } else {
                mrCme.endSearch();
                mrUIRunnableBitmap = mrImageBitMap;
                runOnUiThread(mrUIRunnable);
                Log.d(TAG, "Could not OCR\n");
            }
        }

    }
}

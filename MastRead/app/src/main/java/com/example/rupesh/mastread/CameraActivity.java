package com.example.rupesh.mastread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String TAG = "CameraActivity";
    private ImageView mrImageView;
    private Bitmap mrImageBitMap;
    private String tempImagePath;
    private File tempImageFile;
    static File storageDir;
    private OpenCVImageProcessing mrImageProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mrImageView = (ImageView)findViewById(R.id.imageView);

        prepareOCR();
        try {
            createTempImageFile();
        } catch (IOException ex) {
            Log.d(TAG, "IOException : " + ex.toString());
        }

        mrImageProcessing = new OpenCVImageProcessing(this);
        takePictureViaIntent();
    }

    @Override
    public void onResume() {
        super.onResume();
        mrImageProcessing.OpenCVOnResume(this);
    }

    private void prepareOCR() {
        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String tessDataDirPath = storageDir.getAbsolutePath() + "/" + "tessdata";
        File tessDataDir = new File (tessDataDirPath);

        if (!tessDataDir.exists()) {
            tessDataDir.mkdirs();
        }

        String tessDataFilePath = tessDataDirPath + "/" + "eng.traineddata";
        File tessDataFile = new File(tessDataFilePath);

        if (!tessDataFile.exists()) {
            InputStream in = (InputStream)this.getResources().openRawResource(R.raw.eng);
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

    private void createTempImageFile() throws IOException {
        String tempFileName = "mytemp.jpg";
        tempImageFile = new File(storageDir + "/" + tempFileName);

        tempImagePath = tempImageFile.getAbsolutePath();

        Log.d(TAG, "storageDir :" + storageDir.toString());
        Log.d(TAG, "tempImagePath :" + tempImagePath);
    }

    private void takePictureViaIntent() {
            if (tempImageFile != null) {
                /*Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        tempImageFile);*/
                Uri photoURI = Uri.fromFile(tempImageFile);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*Log.d(TAG, "Setting thumbnail");
            Bundle extras = data.getExtras();
            Bitmap imageBitMap = (Bitmap) extras.get("data");
            mrImageView.setImageBitmap(imageBitMap);*/

            Log.d(TAG, "Image size = " + tempImageFile.length());
            mrImageBitMap = mrImageProcessing.preProcessImage(tempImagePath);
            //mrImageBitMap = SimpleImageProcessing.preProcessImage(tempImagePath);

            mrImageView.setImageBitmap(mrImageBitMap);

            Log.d(TAG, "calling OCRtest");
            doOCRTest();
            Log.d(TAG, "exit OCRtest");
        }
    }



    private void doOCRTest() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                TessBaseAPI baseApi = new TessBaseAPI();
                baseApi.setDebug(true);
                baseApi.init(storageDir.getAbsolutePath(), "eng");
                Log.d(TAG, "done init OCRtest");

                assert(mrImageBitMap != null);

                baseApi.setImage(mrImageBitMap);

                Log.d(TAG, "done setImage OCRtest");

                String recognizedText = baseApi.getUTF8Text();

                Log.d(TAG, "Recognized text - " + recognizedText);

                baseApi.end();
            }
        };

        new Thread(runnable).start();

    }

}

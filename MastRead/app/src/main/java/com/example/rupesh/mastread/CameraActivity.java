package com.example.rupesh.mastread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.googlecode.tesseract.android.OCR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CameraActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String TAG = "CameraActivity";
    private ImageView mrImageView;
    private Bitmap mrImageBitMap;
    private String tempImagePath;
    private File tempImageFile;
    static File storageDir;
    private OCR mrOCR;

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
        takePictureViaIntent();
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

            InputStream in = (InputStream) this.getResources().openRawResource(R.raw.eng);
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

        mrOCR = new OCR(storageDir.getAbsolutePath());

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

            mrImageBitMap = SimpleImageProcessing.preProcessImage(tempImagePath);

            Log.d(TAG, "Calling TextFairy");
            mrImageBitMap = mrOCR.preProcessImageUsingTextFairyMagic(mrImageBitMap);

            mrImageView.setImageBitmap(mrImageBitMap);

            Log.d(TAG, "calling OCRtest");
            mrOCR.doOCRTest(mrImageBitMap);
            Log.d(TAG, "exit OCRtest");
        }
    }


}

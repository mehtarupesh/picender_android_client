package com.example.rupesh.mastread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by rupesh on 5/22/17.
 */
public class OpenCVImageProcessing {
    private BaseLoaderCallback mrLoaderCallback;
    private String TAG = "OpenCVImageProcessing";

    OpenCVImageProcessing(Context context) {
        mrLoaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch(status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.d(TAG, "OpenCV Loaded Sucessfuly");
                        break;
                    }
                    default: {
                        Log.d(TAG, "onManagerConnected status = " + status);
                        super.onManagerConnected(status);
                        break;
                    }
                }
            }
        };
    }

    public void OpenCVOnResume(Context context) {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, context, mrLoaderCallback);
    }

    private Mat BitmapToMatrix(Bitmap b) {
        Mat imgMat = new Mat();
        Utils.bitmapToMat(b, imgMat);
        return imgMat;
    }

    private Bitmap MatrixToBitmap(Mat m) {

        Bitmap equalizedBmap = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, equalizedBmap);

        return equalizedBmap;
    }

    private Mat getGrayScale(Mat m) {

        Mat grayMat = new Mat();
        Imgproc.cvtColor(m, grayMat, Imgproc.COLOR_RGB2GRAY);

        return grayMat;

    }

    private Mat equalizeHistogram(Mat m) {
        Mat equalizedMat = new Mat(m.rows(), m.cols(), CvType.CV_8UC3);
        Imgproc.equalizeHist(m, equalizedMat);

        return equalizedMat;
    }

    private Mat adaptiveThreshold(Mat m, int blockSize_val, double constant_val) {

        Mat adaptiveThresholdMat = new Mat(m.rows(), m.cols(), CvType.CV_8UC3);
        Imgproc.adaptiveThreshold(m, adaptiveThresholdMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize_val, constant_val);
        return adaptiveThresholdMat;
    }

    private Mat resizeMatrix(Mat m, int rows, int cols) {
        Mat resizedMat = new Mat(rows, cols, CvType.CV_8UC3);
        Size sz = new Size(rows, cols);
        Imgproc.resize(m, resizedMat, sz);

        return resizedMat;
    }

    public Bitmap preProcessImage(String imagePath) {
        Bitmap bmap = BitmapFactory.decodeFile(imagePath);

        Mat imgMat = BitmapToMatrix(bmap);

        Mat grayMat = getGrayScale(imgMat);

        Mat equalizedMat = equalizeHistogram(grayMat);

        Mat resizedMat = resizeMatrix(equalizedMat, 600, 800);

        Bitmap equalizedBmap = MatrixToBitmap(resizedMat);

        Bitmap cbBitmap = SimpleImageProcessing.changeBitmapContrastBrightness(equalizedBmap, 5, -100);

        Mat cbMat = BitmapToMatrix(cbBitmap);

        Mat adaptiveThMap = adaptiveThreshold(getGrayScale(cbMat), 5, 7);

        Bitmap ret = MatrixToBitmap(adaptiveThMap);

        return ret;

    }
}

package com.example.rupesh.mastread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by rupesh on 5/22/17.
 */
public class SimpleImageProcessing {

    static final String TAG = "SimpleImageProcessing";

    public static Bitmap preProcessImage(String bmapPath) {

        Bitmap ret;

        Bitmap sampled =  getSampledBitmap(bmapPath, 800, 600);
        //Bitmap equalized = equalizeBitmap(sampled);
        //Bitmap cbCorrected = changeBitmapContrastBrightness(equalized, 5, -100);
        //Bitmap gray = createGrayscale(cbCorrected);

        ret = sampled;
        return ret;
    }

    public static Bitmap getSampledBitmap(String path, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int h = options.outHeight;
        int w = options.outWidth;

        Log.d(TAG, "Out H = " + h);
        Log.d(TAG, "Out W = " + w);
        int inSampleSize = 1;

        if (h > height) {
            inSampleSize = Math.round((float)h / (float)height);
        }

        int expectedW = width / inSampleSize;

        if (expectedW > width) {
            inSampleSize = Math.round((float)expectedW / (float) width);
        }

        Log.d(TAG, "inSampleSize =" + inSampleSize);
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(path, options);
        //return equalizeBitmap(BitmapFactory.decodeFile(path, options));
    }

    private static Bitmap equalizeBitmap(Bitmap source) {
        int height = source.getHeight();
        int width = source.getWidth();

        MRStatistics red_array = new MRStatistics("red", 0 , 255);
        MRStatistics blue_array = new MRStatistics("blue", 0 , 255);
        MRStatistics green_array = new MRStatistics("green", 0 , 255);
        MRStatistics alpha_array = new MRStatistics("alpha", 0 , 255);


        Log.d(TAG, "width = " + width);
        Log.d(TAG, "height = " + height);

        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        /* get values */
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                int pixel = source.getPixel(i, j);

                int red = Color.red(pixel);
                int blue = Color.blue(pixel);
                int green = Color.green(pixel);
                int alpha = Color.alpha(pixel);

                red_array.add(red);
                blue_array.add(blue);
                green_array.add(green);
                alpha_array.add(alpha);
            }
        }

        red_array.calculateCDF();
        blue_array.calculateCDF();
        green_array.calculateCDF();
        alpha_array.calculateCDF();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                int pixel = source.getPixel(i, j);
                int red = Color.red(pixel);
                int blue = Color.blue(pixel);
                int green = Color.green(pixel);
                int alpha = Color.alpha(pixel);

                int red_n = red_array.equalize(red);
                int blue_n = blue_array.equalize(blue);
                int green_n = green_array.equalize(green);
                int alpha_n = alpha_array.equalize(alpha);

                ret.setPixel(i, j, Color.argb(alpha_n, red_n, green_n, blue_n));
            }
        }

        return ret;

    }
    /**
     * Stack overflow
     * @param bmp input bitmap
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
    {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }

    private static Bitmap createGrayscale(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOut);
        ColorMatrix ma = new ColorMatrix();
        ma.setSaturation(0);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(ma));
        canvas.drawBitmap(src, 0, 0, paint);
        return bmOut;
    }
}

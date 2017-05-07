package com.grafixartist.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
/**
 * Created by rupesh on 2/5/16.
 */

/* keeps file property (name + size)
*  encrypts/decrypts file property using SHA1
*/
public class Metadata {

    private String fid;
    private String fdir;
    private int fsize;
    private String SHARED_KEY = "~9)*4:r!61!2@S@NT0M&5";
    private String DELIMITER = "@@##$$^^";
    public static int HEADER_SIZE = 512;
    private String DIR_STRING = "DIR";
    private String ID_STRING = "ID";
    private String LEN_STRING = "LENGTH";
    private static String TAG = "Metadata";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static String metaId = ".meta";

    Metadata(String fdirName, String fname, int size) {

        this.fdir = fdirName;
        this.fid = fname;
        this.fsize = size;
        //create fprop object
    }

    String gen_header_string() {

        JSONObject jobj = new JSONObject();

        try {
            jobj.put(DIR_STRING, fdir);
            jobj.put(ID_STRING, fid);
            jobj.put(LEN_STRING, Integer.toString(fsize));
            Log.d(TAG, jobj.toString());
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jobj.toString();
    }

    String crypt() throws java.security.SignatureException {

        String fheader = gen_header_string();
        String result = null;
        String fdigest;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(SHARED_KEY.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(fheader.getBytes());
            fdigest = convertToHex(rawHmac);

        } catch (Exception ex) {

            throw new SignatureException("Failed to generate HMAC : "
                    + ex.getMessage());
        }

        String fsendheader = DELIMITER + fdigest + DELIMITER + fheader;

        if (HEADER_SIZE < fsendheader.length()) {
            Log.d(TAG, "******** header exceeding size limit");
            return null;
        }

        result = pad(fsendheader, HEADER_SIZE, 'X');
        //Log.d(TAG,"crypted string = " + result);
        return result;
    }

    public String pad(String str, int size, char padChar)
    {
        while (str.length() < size)
        {
            str = padChar + str;
        }

        return str;
    }

    public String convertToHex(byte[] rawHmac) {

        StringBuilder result = new StringBuilder();

        for (byte b : rawHmac) {
            result.append(String.format("%02x", b));
        }

        return result.toString();

    }

    private static String generateCacheDirPath(String fid) {

        /* store metadata info in <path_to_image_dir>/.meta/fname.meta */
        File target = new File(fid);

        if(target == null)
            return null;

        String fDirPath = target.getParent();

        /* generate cache dir path */
        String cDirPath = fDirPath + "/" + Metadata.metaId + "/";

        return cDirPath;
    }

    private static String generateCacheFilePath(String fid) {
        /* store metadata info in <path_to_image_dir>/.meta/fname.meta */
        File target = new File(fid);

        if(target == null)
            return null;

        String cDirPath = generateCacheDirPath(fid);

         /* generate cache file path */
        String cFilename = target.getName() + Metadata.metaId;
        String cFilePath = cDirPath + cFilename;

        return cFilePath;

    }
    public boolean store() {

        /* generate cache dir path */
        String cDirPath = generateCacheDirPath(this.fid);
        File cDir = new File(cDirPath);

        if(!cDir.exists()) {
            cDir.mkdirs();
        }

        String cFilePath = generateCacheFilePath(this.fid);

        //Log.d(TAG, "fdirPath" + fDirPath);
        //Log.d(TAG, "cdir = " + cDirPath);
        //Log.d(TAG, "cfile = " + cFilePath);

        File cFile = new File(cFilePath);

        /* for now, existence of file is a hint that a particular image has been sent.
         This alone can be used now to mark images as sent */
        try {
            if (!cFile.exists())
                cFile.createNewFile();
            else
                Log.d(TAG, " ALREADY exists ! : " + cFilePath);

        }catch(IOException e) {

            Log.d(TAG, "IOException for file: "+ cFilePath);
            e.printStackTrace();
        }

        return true;
    }

    public static String loadCacheInfo(String uri) {

        String cFilePath = generateCacheFilePath(uri);

        File cFile = new File(cFilePath);

        if (cFile.exists())
            return cFilePath;
        else
            return null;

    }

    public static Boolean delete(String uri) {
        File dFile = new File(uri);

        if (dFile.exists()) {
            if (dFile.delete()) {
                Log.d(TAG, "file deleted : " + uri);
                return true;
            } else {
                Log.d(TAG, "file NOT deleted : " + uri);
            }
        } else {
            Log.d(TAG, "file does not exist : " + uri);
        }

        return false;
    }

    public static byte[] compressData(byte[] bytesToCompress)
    {
        Deflater deflater = new Deflater();
        deflater.setInput(bytesToCompress);
        deflater.finish();

        byte[] bytesCompressed = new byte[32 * 4096];

        int numberOfBytesAfterCompression = deflater.deflate(bytesCompressed);

        byte[] returnValues = new byte[numberOfBytesAfterCompression];

        System.arraycopy
                (
                        bytesCompressed,
                        0,
                        returnValues,
                        0,
                        numberOfBytesAfterCompression
                );

        return returnValues;
    }

    public byte[] decompressData(byte[] compressedData) throws IOException, DataFormatException
    {
        Inflater inflater = new Inflater();
        inflater.setInput(compressedData, 0, compressedData.length);
        byte[] decompressedBytes = new byte[32 * 4096];
        int decompressedByteCount = inflater.inflate(decompressedBytes);
        byte[] retval = new byte[decompressedByteCount];

        System.arraycopy(decompressedBytes, 0, retval, 0, decompressedByteCount);

        return retval;

    }

    public void compressionTest() {

        Log.d(TAG, "Starting compression");

        String compressed_fid = this.fid + ".compressed";
        File compressed = new File(compressed_fid);
        File orig = new File(this.fid);

        try {

            int recd_bytes, total_bytes = 0;
            int DATA_BUFLEN = (16 * 4096);
            byte[] buffer = new byte[DATA_BUFLEN];

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(orig));
            FileOutputStream out = new FileOutputStream(compressed);

            /*send file in chunks */
            long start = System.currentTimeMillis();
            while ((recd_bytes = in.read(buffer, 0, DATA_BUFLEN)) != -1) {

                byte[] recd = compressData(buffer);

                /*
                byte[] decompressed = decompressData(recd);

                for (int i = 0; i < recd_bytes; i++) {
                    if (buffer[i] != decompressed[i]) {
                        Log.d(TAG, "buffer size =" + Integer.toString(recd_bytes) + " decompressed size = " + Integer.toString(decompressed.length));
                        Log.d(TAG, "unequal!!!!");
                        return;
                    }
                }*/
                total_bytes += recd.length;

            }
            long end = System.currentTimeMillis();

            Log.d(TAG, "time in ms = " + Long.toString(end - start));
            Log.d(TAG, "size in bytes = " + Integer.toString(total_bytes));

            delete(compressed_fid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

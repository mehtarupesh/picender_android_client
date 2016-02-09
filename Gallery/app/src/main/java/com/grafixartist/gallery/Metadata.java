package com.grafixartist.gallery;

import android.util.Base64;
import android.util.Log;
import android.util.Xml;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.SignatureException;

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
    private String SHARED_KEY = "cigital/ds/12@33!";
    private String DELIMITER = "@@##$$^^";
    public static int HEADER_SIZE = 512;
    private String DIR_STRING = "DIR";
    private String ID_STRING = "ID";
    private String LEN_STRING = "LENGTH";
    private String TAG = "Metadata";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

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

}

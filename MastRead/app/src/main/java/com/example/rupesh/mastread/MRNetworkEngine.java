package com.example.rupesh.mastread;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rupesh on 10/14/17.
 */
public class MRNetworkEngine {

    private DownloadManager mrDm;
    private Long refId;
    private String sampleFileName = "filelist.json";
    private static String TAG = "MRNetworkEngine";
    private Uri serverAddress;

    MRNetworkEngine(Context context, String addr)
    {
        mrDm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        serverAddress = Uri.parse(addr);

    }

    MRNetworkEngine(Context context) {
        mrDm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    void testSampleDownload(Context context) {

        Uri Download_Uri = Uri.parse("http:192.168.1.6:8888/filelist.json");

        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle("GadgetSaint Downloading " + "Sample" + ".png");
        request.setDescription("Downloading " + "Sample" + ".png");
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalFilesDir(context, null, sampleFileName);


        refId = mrDm.enqueue(request);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.d(TAG, "Download complete : referenceId out = " + referenceId);
            Log.d(TAG, "refId expect = " + refId);

            if (referenceId == refId) {

                String downloadFilePath = DownloadStatus(referenceId);
                File download = new File(downloadFilePath);
                Log.d(TAG, "Filename : " + download.getAbsolutePath());

                try {
                    BufferedReader br = new BufferedReader(new FileReader(download));
                    String line;

                    while ((line = br.readLine()) != null) {
                        Log.d(TAG, line);
                    }
                    br.close();

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }

                parseJsonFilelist(download);
            }

        }
    };

    private ArrayList<TextBook> parseJsonFilelist(File jsonFile) {

        ArrayList<TextBook> retList = new ArrayList<>();
        InputStream in = null;
        try {
            in = new FileInputStream(jsonFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int size = 0;
        byte[] buffer = {0};
        try {
            size = in.available();
            buffer = new byte[size];
            in.read(buffer);
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonString = null;
        try {
            jsonString = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            Iterator<String> keys = jsonObj.keys();

            while (keys.hasNext()) {
                String key = keys.next();

                Log.d(TAG, "key = " + key);
                String components[] = isValidTextBookPath(key);

                if (components != null) {

                    JSONArray jsonArr = (JSONArray) jsonObj.get(key);

                    assert(jsonArr.length() % 3 == 0);
                    TextBook tBook = new TextBook(components[0], components[1], components[2], components[3], jsonArr.length() / 3);


                    Log.d(TAG, "values :");
                    for (int i = 0; i < jsonArr.length(); i++) {
                        tBook.addEntry(key + jsonArr.get(i).toString());
                        Log.d(TAG, jsonArr.get(i).toString());
                    }

                    Log.d(TAG, "---------------------------------");
                    retList.add(tBook);

                } else {
                    Log.d(TAG, "INVALID key");
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return retList;

    }


    // path style : HACKY : "./BOARD_NAME/MEDIUM_NAME/GRADE/SUBJECT
    // split by "/" gives 5 individual strings
    public static String[] isValidTextBookPath(String directoryName) {

        String[] directory = directoryName.split("/");
        String ret[] = new String[4];

        int i;
        if (directory.length == 5 && directory[0].equals(".")) {
            for (i = 1; i < directory.length; i++) {
                Log.d(TAG, i + "entry is " + directory[i]);
            }
            System.arraycopy(directory, 1, ret, 0, ret.length);
            return ret;
        }
        return null;
    }

    private String DownloadStatus(long DownloadId) {

        DownloadManager.Query MusicDownloadQuery = new DownloadManager.Query();
        //set the query filter to our previously Enqueued download
        MusicDownloadQuery.setFilterById(DownloadId);

        //Query the download manager about downloads that have been requested.
        Cursor cursor = mrDm.query(MusicDownloadQuery);
        if (cursor.moveToFirst()) {


            //column for download  status
            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(columnIndex);
            //column for reason code if the download failed or paused
            int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(columnReason);
            //get the download filename
            int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
            String filename = cursor.getString(filenameIndex);

            String statusText = "";
            String reasonText = "";

            switch (status) {
                case DownloadManager.STATUS_FAILED:
                    statusText = "STATUS_FAILED";
                    switch (reason) {
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            reasonText = "ERROR_CANNOT_RESUME";
                            break;
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            reasonText = "ERROR_DEVICE_NOT_FOUND";
                            break;
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            reasonText = "ERROR_FILE_ALREADY_EXISTS";
                            break;
                        case DownloadManager.ERROR_FILE_ERROR:
                            reasonText = "ERROR_FILE_ERROR";
                            break;
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                            reasonText = "ERROR_HTTP_DATA_ERROR";
                            break;
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            reasonText = "ERROR_INSUFFICIENT_SPACE";
                            break;
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            reasonText = "ERROR_TOO_MANY_REDIRECTS";
                            break;
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                            break;
                        case DownloadManager.ERROR_UNKNOWN:
                            reasonText = "ERROR_UNKNOWN";
                            break;
                    }
                    break;
                case DownloadManager.STATUS_PAUSED:
                    statusText = "STATUS_PAUSED";
                    switch (reason) {
                        case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                            reasonText = "PAUSED_QUEUED_FOR_WIFI";
                            break;
                        case DownloadManager.PAUSED_UNKNOWN:
                            reasonText = "PAUSED_UNKNOWN";
                            break;
                        case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                            reasonText = "PAUSED_WAITING_FOR_NETWORK";
                            break;
                        case DownloadManager.PAUSED_WAITING_TO_RETRY:
                            reasonText = "PAUSED_WAITING_TO_RETRY";
                            break;
                    }
                    break;
                case DownloadManager.STATUS_PENDING:
                    statusText = "STATUS_PENDING";
                    break;
                case DownloadManager.STATUS_RUNNING:
                    statusText = "STATUS_RUNNING";
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    statusText = "STATUS_SUCCESSFUL";
                    reasonText = "Filename:\n" + filename;
                    break;
            }

            Log.d(TAG,
                    "Music Download Status:" + "\n" + statusText + "\n" +
                            reasonText);
            return filename;
        } else
            return null;


    }
}
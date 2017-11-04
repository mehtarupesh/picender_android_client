package com.example.rupesh.mastread;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rupesh on 6/15/17.
 */
public class MRResource {

    //app packaged
    AssetManager assetManager;
    private final String TAG = "MRResource";


    private DownloadManager mrDm;
    private downloadCallback mrDlCb;
    private final String serverAddress= "http:192.168.1.6:8888";


    public MRResource(Context context, downloadCallback cb) {

        mrDm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        mrDlCb = cb;

        assetManager = context.getAssets();
    }

    public Long downloadFile(Context context, String filePath) {

        Uri Download_Uri = Uri.parse(serverAddress +"/" + filePath);

        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle("MastRead Downloading " + filePath);
        request.setDescription("Downloading " + filePath);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalFilesDir(context, null, filePath);

        return mrDm.enqueue(request);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.d(TAG, "Download complete : referenceId out = " + referenceId);

            String downloadFilePath = DownloadStatus(referenceId);
            File download = new File(downloadFilePath);
            Log.d(TAG, "Downloaded File : " + download.getAbsolutePath());

                /*try {
                    BufferedReader br = new BufferedReader(new FileReader(download));
                    String line;

                    while ((line = br.readLine()) != null) {
                        Log.d(TAG, line);
                    }
                    br.close();

                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }*/

                mrDlCb.downloadFinishedCallback(download, referenceId);
        }
    };

    public ArrayList<TextBook> parseJsonFilelist(File jsonFile) {

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

                    /* TODO: sanity check for .json .txt .mp3 ??*/
                    assert(jsonArr.length() % 3 == 0);
                    TextBook tBook = new TextBook(components[0], components[1], components[2], components[3], jsonArr.length() / 3);


                    Log.d(TAG, "values :");
                    for (int i = 0; i < jsonArr.length(); i++) {
                        /* create path folder + filename */
                        tBook.addEntry(key + "/" + jsonArr.get(i).toString());
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
    private String[] isValidTextBookPath(String directoryName) {

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

    private String[] isValidResource(String bookDirPath) {

        String[] dirContents = null;
        try {
            dirContents = assetManager.list(bookDirPath);

            Log.d(TAG, "Book name = " + bookDirPath);


            int num_pages = dirContents.length / 3;
            int mp3_files = 0;
            int text_files = 0;
            int json_files = 0;

            for (int i = 0; i <  dirContents.length; i++) {

                Log.d(TAG, "content : " + dirContents[i]);
                if (dirContents[i].contains( ".mp3")) {
                    mp3_files++;
                }

                else if (dirContents[i].contains(".json")) {
                    json_files++;

                }

                else if (dirContents[i].contains(".txt")) {
                    text_files++;
                }

            }

            //if (mp3_files != num_pages && text_files != num_pages && json_files != num_pages)
            //    return null;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dirContents;
    }

    private String[] copy_to_destination_folder(String assetDirName, String[] assetfileList, String dstRootDir) throws IOException {

        String[] retVal = new String[assetfileList.length];

        File dstDir = new File(dstRootDir + "/" + assetDirName);
        if (dstDir.exists()) {
            Log.d(TAG, "Already exists --> " + dstDir.getAbsolutePath());
        } else {
            dstDir.mkdirs();
        }

        for (int i = 0; i < assetfileList.length; i++) {

            String sourceFilePath = assetDirName + "/" + assetfileList[i];
            String dstFilePath = dstDir.getAbsolutePath() + "/" + assetfileList[i];

            File dstFile = new File(dstFilePath);
            if (!dstFile.exists() || dstFile.length() == 0){
                OutputStream out = new FileOutputStream(dstFile);
                byte[] buffer = new byte[1024];
                int length;

                Log.d(TAG, "sourceFilePath = " + sourceFilePath);
                InputStream in = assetManager.open(sourceFilePath);

                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                in.close();
                out.flush();
                out.close();
            } else {
                Log.d(TAG, "Already exists : " + dstFile);
            }
            retVal[i] = dstFilePath;
        }

        return retVal;
    }

    public ArrayList<Book> fetchResources(String dstRoot) {


        ArrayList<Book> retList = new ArrayList<>();

        try {
            String[] rPaths = assetManager.list("");

            Log.d(TAG, "Assets : size = " + rPaths.length + "\n");
            for (int i = 0; i < rPaths.length; i++) {
                Log.d(TAG, rPaths[i] + "\n");
                String[] folderContents = null;
                if (rPaths[i].contains("mast_read_book") && (folderContents = isValidResource(rPaths[i])) != null) {
                    String[] ret = copy_to_destination_folder(rPaths[i], folderContents, dstRoot);

                    Book b = new Book(rPaths[i]);
                    b.addMultipleEntries(ret);
                    retList.add(b);
                    Log.d(TAG, b.toString());

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return retList;
    }


}

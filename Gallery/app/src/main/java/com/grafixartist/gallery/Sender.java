package com.grafixartist.gallery;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * Created by rupesh on 2/4/16.
 */
public class Sender extends AsyncTask<Void, Void, Void> {

    private Socket client;
    private String SERVER = "armeta.ddns.net";
    private int PORT = 8888;
    private int RECV_BUFLEN = 4096;
    private int DATA_BUFLEN = (16 * 4096);
    private String filePath = null;
    private String fileDir = null;
    private int position;
    private String TAG = "Sender";

    Sender(String fileDirName, String absFilePath, int position) {
        this.filePath = absFilePath;
        this.fileDir = fileDirName;
        this.position = position;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        File target = new File(filePath);
        OutputStream out;

        if (Metadata.loadCacheInfo(filePath) != null) {
            Log.d(TAG, " >>> " + filePath + " <<< already Exists !!");
            return null;
        }
        try {

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(target));
            client = new Socket(SERVER, PORT);
            out = client.getOutputStream();
            byte[] buffer = new byte[DATA_BUFLEN];
            int recd_bytes = 0;
            int total_bytes = 0;

            /* send metadata */
            Metadata m = new Metadata(fileDir, filePath, (int) target.length());
            String fheader = null;
            try {
                fheader = m.crypt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //Log.d(TAG, "fheader = "+fheader);
            out.write(fheader.getBytes(), 0, Metadata.HEADER_SIZE);

            /*send file in chunks */
            while ((recd_bytes = in.read(buffer, 0, DATA_BUFLEN)) != -1) {

                out.write(buffer, 0, recd_bytes);
                total_bytes += recd_bytes;

            }

            Log.d(TAG, "********sent bytes = "+Integer.toString(total_bytes));
            out.flush();
            out.close();
            client.close();

            /* mark file as sent */
            m.store();

            /* test compression */
            m.compressionTest();

        } catch (UnknownHostException e) {

            Log.d(TAG, "Unkown HOST : " + SERVER);
            e.printStackTrace();

        } catch (IOException e) {

            Log.d(TAG, "IOException for file: " + filePath);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        GalleryAdapter.markImage(this.position);
    }

}
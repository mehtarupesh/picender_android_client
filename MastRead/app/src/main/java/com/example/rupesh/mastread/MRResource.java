package com.example.rupesh.mastread;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by rupesh on 6/15/17.
 */
public class MRResource {

    //app packaged
    AssetManager assetManager;
    private final String TAG = "MRResource";

    //TODO: downloaded resource from server

    public MRResource(Context context) {
        assetManager = context.getAssets();
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

package com.example.rupesh.mastread;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by rupesh on 6/14/17.
 */
public class ContentManagementEngine implements downloadCallback {

    private static final String TAG = "ContentManagerEngine";
    private static ContentManagementEngine mrCme;
    private static MRDb mrDb;
    private static MRResource mrResource;
    private final String serverFileList = "filelist.json";


    private static boolean searchInProgress;
    private static String searchQuery;

    private ContentManagementEngine(Context context) {
               /* Update Database */
        mrResource = new MRResource(context, this);
        mrResource.downloadFile(context, serverFileList);
        mrDb = new MRDb(context);


        /*ArrayList<Book> bookList = mrResource.fetchResources(context.getExternalFilesDir(null).getAbsolutePath());

        mrDb = new MRDb(context);
        Log.d(TAG, "Num DB entries = " + mrDb.getNumberofEntries());
        for (int i = 0; i < bookList.size(); i++) {
            Book b = bookList.get(i);
            mrDb.addEntry(b);
            mrDb.printBookInfo(b.getName());
        }*/

        searchInProgress = false;
    }

    public static ContentManagementEngine getContentManagementEngine(Context context) {

        if (mrCme == null) {
            mrCme = new ContentManagementEngine(context);
        }
        return mrCme;
    }

    public void startSearch() {
        searchInProgress = true;
        searchQuery = null;
    }

    String filterWordForAlphabets(String original) {
        String ret = "";

        if (original == null)
            return ret;

        for (int i = 0; i < original.length(); i++) {
            if (Character.isLetter(original.charAt(i))) {
                ret += original.charAt(i);
            }
        }
        return ret;
    }

    public Page search(String pageText) {
        if (searchInProgress == false) {
            return null;
        }

        Page ret = null;
        String words[] = pageText.split("\\s+");
        String searchString = "";

        //TODO: optimizations
        //long dbEntriesThreshold = mrDb.getNumberofEntries() / 4;

        for (int i = 0; i < words.length; i++) {

            //words[i] = words[i].replaceAll("\n","");
            //words[i] = words[i].replaceAll("\"","");
            //words[i] = words[i].replaceAll("\'","");
            Log.d(TAG, "Unfiltererd word :  " + words[i]);

            words[i] = filterWordForAlphabets(words[i]);

            if (words[i] == null)
                return ret;

            Log.d(TAG, "Checking for word :  " + words[i]);

            int matches = mrDb.wordEntryMatches(words[i]);
            if (matches > 0) {
                Log.d(TAG, "Exists..");

                searchString = searchString + ((searchString.length() > 0) ? " AND " : "") + words[i];

                ret = mrDb.searchText(searchString);
                if (ret != null)
                    return ret;

            } else {
                Log.d(TAG, "Not found...");
            }

            Log.d(TAG, "-------------------");
        }

        return ret;
    }

    public void endSearch() {
        searchInProgress = false;
        searchQuery = null;
    }

    @Override
    public void downloadFinishedCallback(File dlFile, Long referenceId) {

        ArrayList<TextBook> textBookList= mrResource.parseJsonFilelist(dlFile);
        Log.d(TAG, "Num DB entries = " + mrDb.getNumberofEntries());
        for (int i = 0; i < textBookList.size(); i++) {
            TextBook tb = textBookList.get(i);
            mrDb.addTextBookEntry(tb);
             mrDb.printTextBookInfo(tb.getBoard(), tb.getMedium(), tb.getGrade(), tb.getName());
        }
    }
}

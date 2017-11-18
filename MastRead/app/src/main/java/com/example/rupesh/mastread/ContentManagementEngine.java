package com.example.rupesh.mastread;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by rupesh on 6/14/17.
 */
public class ContentManagementEngine implements downloadCallback {

    private static final String TAG = "ContentManagerEngine";
    private static ContentManagementEngine mrCme;
    private static MRDb mrDb;
    private static MRResource mrResource;
    private final String serverFileList = "filelist.mastread";
    private List<Long> currentTextBookDownloadBundle;
    private TextBook currentTextBook;
    private Page currentPageFocus;
    private Context mrContext;
    private static String currentResourceId;


    private static boolean searchInProgress;
    private static String searchQuery;

    private ContentManagementEngine(Context context) {
               /* Update Database */
        mrContext = context;
        mrResource = new MRResource(context, this);
        mrResource.downloadFile(context, serverFileList, true);
        mrDb = new MRDb(context);
        currentTextBookDownloadBundle = Collections.synchronizedList(new ArrayList<Long>());
        currentTextBook = null;
        currentPageFocus = null;

        searchInProgress = false;
    }

    public static ContentManagementEngine getContentManagementEngine(Context context) {

        if (mrCme == null) {
            mrCme = new ContentManagementEngine(context);
        }
        return mrCme;
    }

    public static String getCurrentResourceId() {
        return currentResourceId;
    }

    public static void setCurrentResourceId(String currentResourceId) {
        ContentManagementEngine.currentResourceId = currentResourceId;
    }

    public void downloadTextBookWithoutAudio(Context context, String board, String medium, String grade, String bookName, String resId) {

        ArrayList<String> textBookFiles = mrDb.getTextBookFiles(board, medium, grade, bookName, resId);

        if (textBookFiles.size() > 0) {

            synchronized (currentTextBookDownloadBundle) {

                /* only one textbook at a time ? */
                if (currentTextBookDownloadBundle.size() == 0) {

                    currentTextBook = new TextBook(board, medium, grade, bookName, resId);

                    for (int i = 0; i < textBookFiles.size(); i++) {
                        String dlFile = textBookFiles.get(i);
                        if (!Page.isAudiotFile(dlFile) && !mrResource.fileExistsOnDevice(dlFile)) {
                            Long ret = mrResource.downloadFile(context, dlFile, true);

                            Log.d(TAG, "added for download refid = " + ret);
                            if (ret > 0) {
                                currentTextBookDownloadBundle.add(ret);
                            }
                        }
                    }
                }
            }
        }

    }

    public void downloadTextBookWithoutAudio(Context context, String resId) {

        ArrayList<String> textBookFiles = mrDb.getTextBookFiles(resId);

        if (textBookFiles.size() > 0) {

            synchronized (currentTextBookDownloadBundle) {

                /* only one textbook at a time ? */
                if (currentTextBookDownloadBundle.size() == 0) {

                    currentTextBook = new TextBook(null, null, null, null, resId);

                    for (int i = 0; i < textBookFiles.size(); i++) {
                        String dlFile = textBookFiles.get(i);
                        if (!Page.isAudiotFile(dlFile) && !mrResource.fileExistsOnDevice(dlFile)) {
                            Long ret = mrResource.downloadFile(context, dlFile, true);

                            Log.d(TAG, "added for download refid = " + ret);
                            if (ret > 0) {
                                currentTextBookDownloadBundle.add(ret);
                            }
                        }
                    }
                }
            }
        }

    }

    public TextBook getTextBook(String resId) {
        return mrDb.getTextBook(resId);
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

        TextBook ret = null;
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
                return null;

            Log.d(TAG, "Checking for word :  " + words[i]);

            int matches = mrDb.wordEntryMatches(words[i]);
            if (matches > 0) {
                Log.d(TAG, "Exists..");

                searchString = searchString + ((searchString.length() > 0) ? " AND " : "") + words[i];

                ret = mrDb.searchText(searchString);
                if (ret != null) {
                    assert(ret.getPages().size() == 0);

                    ArrayList<String> textBookFiles = mrDb.getTextBookFiles(ret.getBoard(), ret.getMedium(), ret.getGrade(), ret.getName(), ret.getResourceId());
                    assert(textBookFiles != null);


                    /* only one page must be added by mrDB */
                    int pageNumber = ret.getPages().get(0).getNumber();

                    Log.d(TAG, "Board = " + ret.getBoard());
                    Log.d(TAG, "Medium = " + ret.getMedium());
                    Log.d(TAG, "Grade = " + ret.getGrade());
                    Log.d(TAG, "Name = " + ret.getName());

                    Log.d(TAG, "Page #= " + pageNumber);
                    Log.d(TAG, "Target page = " + ret.getPage(pageNumber).toString());


                    return processPageRequest(ret, pageNumber);

                                            /* check if already exists */
//                    if (MRResource.fileExistsOnDevice(ret.getPage(pageNumber).getAudioPath())) {
//
//                        ret.getPage(pageNumber).setAudioIsOnDevice(true);
//                        return ret.getPage(pageNumber);
//                    }
//
//                    synchronized (currentTextBookDownloadBundle) {
//                        /* signal caller to not call PBA, wait for callback hmmm... */
//                        ret.getPage(pageNumber).setAudioIsOnDevice(false);
//                        currentPageFocus = ret.getPage(pageNumber);
//                    }
//
//                    downloadFile(ret.getPage(pageNumber).getAudioPath(), true);
//
//                    return ret.getPage(pageNumber);
                }

            } else {
                Log.d(TAG, "Not found...");
            }
            Log.d(TAG, "-------------------");
        }

        return null;
    }

    public Page searchOptimized(String pageText) {
        if (searchInProgress == false) {
            return null;
        }

        TextBook ret = null;
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


            if (words[i].length() < 2)
                continue;

            if (words[i] == null)
                return null;

            Log.d(TAG, "Checking for word :  " + words[i]);

            int matches = mrDb.wordEntryMatches(words[i]);
            if (matches > 0) {
                Log.d(TAG, "Exists..");

                searchString = searchString + ((searchString.length() > 0) ? " AND " : "") + words[i];

                Log.d(TAG, "search string = " + searchString);

                ret = mrDb.searchTextOptimized(searchString, currentResourceId);
                if (ret != null) {
                    assert(ret.getPages().size() == 0);

                    ArrayList<String> textBookFiles = mrDb.getTextBookFiles(ret.getResourceId());
                    assert(textBookFiles != null);


                    /* only one page must be added by mrDB */
                    int pageNumber = ret.getPages().get(0).getNumber();

                    Log.d(TAG, "Board = " + ret.getBoard());
                    Log.d(TAG, "Medium = " + ret.getMedium());
                    Log.d(TAG, "Grade = " + ret.getGrade());
                    Log.d(TAG, "Name = " + ret.getName());

                    Log.d(TAG, "Page #= " + pageNumber);
                    Log.d(TAG, "Target page = " + ret.getPage(pageNumber).toString());


                    return processPageRequest(ret, pageNumber);
                                            /* check if already exists */
//                    if (MRResource.fileExistsOnDevice(ret.getPage(pageNumber).getAudioPath())) {
//
//                        ret.getPage(pageNumber).setAudioIsOnDevice(true);
//                        return ret.getPage(pageNumber);
//                    }
//
//                    synchronized (currentTextBookDownloadBundle) {
//                        /* signal caller to not call PBA, wait for callback hmmm... */
//                        ret.getPage(pageNumber).setAudioIsOnDevice(false);
//                        currentPageFocus = ret.getPage(pageNumber);
//                    }
//
//                    downloadFile(ret.getPage(pageNumber).getAudioPath(), true);
//
//                    return ret.getPage(pageNumber);
                }

            } else {
                Log.d(TAG, "Not found...");
            }
            Log.d(TAG, "-------------------");
        }

        return null;
    }

    public Page processPageRequest(TextBook tBook, int pageNumber) {
                                          /* check if already exists */
        if (MRResource.fileExistsOnDevice(tBook.getPage(pageNumber).getAudioPath())) {

            tBook.getPage(pageNumber).setAudioIsOnDevice(true);
            return tBook.getPage(pageNumber);
        }

        synchronized (currentTextBookDownloadBundle) {
                        /* signal caller to not call PBA, wait for callback hmmm... */
            tBook.getPage(pageNumber).setAudioIsOnDevice(false);
            currentPageFocus = tBook.getPage(pageNumber);
        }

        downloadFile(tBook.getPage(pageNumber).getAudioPath(), true);

        return tBook.getPage(pageNumber);

    }

    public Long downloadFile(String path, Boolean ignoreIfExists) {
        return mrResource.downloadFile(mrContext, path, ignoreIfExists);
    }

    public void endSearch() {
        searchInProgress = false;
        searchQuery = null;
    }

    @Override
    public void downloadFinishedCallback(File dlFile, Long referenceId) {

        //TODO: Fix this download bug
        if (dlFile.getAbsolutePath().contains("-")) {
            return;
        }
        /* bootstrapping list */
        if (dlFile.getAbsolutePath().contains(serverFileList)) {
            ArrayList<TextBook> textBookList = mrResource.parseJsonFilelist(dlFile);
            Long entryStart = mrDb.getNumberofEntries();
            for (int i = 0; i < textBookList.size(); i++) {
                TextBook tb = textBookList.get(i);
                mrDb.addTextBookEntry(tb);
                mrDb.printTextBookInfo(tb.getBoard(), tb.getMedium(), tb.getGrade(), tb.getName());
            }
            Log.d(TAG, "New DB entries added = " + (mrDb.getNumberofEntries() - entryStart));

        }
        /* media, data files */
        else {

            synchronized (currentTextBookDownloadBundle) {
                if (currentTextBookDownloadBundle.size() > 0) {
                    Log.d(TAG, "remaining size = " + currentTextBookDownloadBundle.size());

                    for (int i = 0; i < currentTextBookDownloadBundle.size(); i++) {

                        if (currentTextBookDownloadBundle.get(i).equals(referenceId)) {
                            Log.d(TAG, "removing it !");
                            currentTextBookDownloadBundle.remove(i);
                            break;
                        }
                    }

                    if (currentTextBookDownloadBundle.isEmpty()) {
                        Log.d(TAG, "Done downloading bundle, adding to DB!!");
                        Toast.makeText(mrContext, "Finished downloading " + currentTextBook.getResourceId(), Toast.LENGTH_SHORT).show();

                        //mrDb.addPageTextInfo(currentTextBook.getBoard(), currentTextBook.getMedium(), currentTextBook.getGrade(), currentTextBook.getName());
                        mrDb.addPageTextInfo(currentTextBook.getResourceId());
                        currentTextBook = null;
                    }
                }

                if (currentPageFocus != null) {

                    int pageNumber = Book.getPageNumber(dlFile.getAbsolutePath());
                    Log.d(TAG, "dl page number =" + pageNumber);
                    Log.d(TAG, "currentPageFocus page number = " + currentPageFocus.getNumber());

                    if (Book.getPageNumber(dlFile.getAbsolutePath()) == currentPageFocus.getNumber()) {

                        Log.d(TAG, "Downloaded audio file, going to playback!\n");
                        Toast.makeText(mrContext, "Finished downloading Audio !", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mrContext, PlayBackActivity.class);
                        intent.putExtra("PAGE_INFO", currentPageFocus);
                        mrContext.startActivity(intent);
                    }

                }

            }


        }
    }
}

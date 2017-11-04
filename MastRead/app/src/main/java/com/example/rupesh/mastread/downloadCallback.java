package com.example.rupesh.mastread;

import java.io.File;

/**
 * Created by rupesh on 10/27/17.
 */
public interface downloadCallback {

    void downloadFinishedCallback(File dlFile, Long referenceId);
}

/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.syncro.ui;

import co.bitshifted.appforge.syncro.http.DownloadHandler;
import co.bitshifted.appforge.syncro.http.SyncroHttpClient;
import co.bitshifted.appforge.syncro.launch.LaunchArgs;
import co.bitshifted.appforge.syncro.launch.ReleaseProcessor;
import co.bitshifted.appforge.syncro.model.ReleaseEntry;
import co.bitshifted.appforge.syncro.model.UpdateInfo;
import co.bitshifted.appforge.syncro.sync.FileDiffChecker;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UpdateWorker extends SwingWorker<Integer, String> {

    private static final String DELETE_RETRIES_FILE = "deletes.txt";
    public static final int UPDATE_COMPLETE = 0;
    public static final int UPDATE_RETRY_NEEDED = 10;
    private static final int UPDATE_FAILED = 20;

    private final JProgressBar progressBar;
    private Path workDir;
    private Path tempDir;
    private SyncroHttpClient httpClient;
    private int downloadTotal;
    private int downloadProgress = 0;
    private final StringBuilder deleteRetryBuilder;
    private boolean deleteRetryNeeded = false;

    public UpdateWorker(JProgressBar progressBar) {
        this.progressBar = progressBar;
        this.deleteRetryBuilder = new StringBuilder();
    }

    @Override
    protected Integer doInBackground() throws Exception {
        LaunchArgs launchArgs = new LaunchArgs();

        workDir = Paths.get(System.getProperty("user.dir"));
        System.out.println("work directory: " + workDir);
        String appId = launchArgs.getApplicationId();
        tempDir = Files.createTempDirectory("syncro-" + appId);
        System.out.println("temp directory: " + tempDir.toFile().getAbsolutePath());
        progressBar.setValue(2);
        httpClient = new SyncroHttpClient(launchArgs.getServerUrl(), appId, launchArgs.getReleaseId());
        UpdateInfo updateInfo = httpClient.checkForUpdates(tempDir);
        int result = UPDATE_COMPLETE;
        switch (updateInfo.getStatus()) {
            case UPDATE_AVAILABLE:
                progressBar.setString("Update is available");
                progressBar.setValue(5);
                result = processUpdate(updateInfo);
                break;
            case NO_UPDATE:
                progressBar.setString("No update available");
                break;
            case ERROR:
                progressBar.setString("Update check failed");
                result = UPDATE_FAILED;
                break;
        }
        return result;
    }

    private int processUpdate(UpdateInfo updateInfo) {
        progressBar.setString("Processing update info");
        FileDiffChecker diffChecker;
        try {
            ReleaseProcessor processor = new ReleaseProcessor(updateInfo.getContent());
            List<ReleaseEntry> releaseEntries = processor.getEntries(workDir);
            diffChecker = new FileDiffChecker(workDir);
            diffChecker.process(releaseEntries);
            System.out.println("Update list: " + diffChecker.getUpdateList());
            System.out.println("delete list: " + diffChecker.getDeleteList());
            System.out.println("New entries: " + diffChecker.getNewEntries());
        } catch(Exception ex) {
            System.err.println("Failed to process update: " + ex.getMessage());
            return UPDATE_FAILED;
        }

        progressBar.setValue(10);
        diffChecker.getDeleteList().forEach(item -> {
                try {
                    Files.delete(item.getTarget());
                } catch (IOException ex) {
                    System.err.println("Failed to delete file: " + item.getTarget().toAbsolutePath());
                    deleteRetryBuilder.append(item.getTarget().toAbsolutePath()).append("\n");
                    deleteRetryNeeded = true;
                }
            });
        // save deletes file
        if(deleteRetryBuilder.length() > 0) {
            System.out.println("Saving delete retries to file");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(workDir.resolve(DELETE_RETRIES_FILE).toFile()))) {
                bw.write(deleteRetryBuilder.toString());
            } catch (IOException ex) {
                System.err.println("Failed to create retry file: " + ex.getMessage());
            }
        }
        downloadTotal = diffChecker.getUpdateList().size() + diffChecker.getNewEntries().size();
        List<ReleaseEntry> downloadList = new ArrayList<>(diffChecker.getUpdateList());
        downloadList.addAll(diffChecker.getNewEntries());
        DownloadHandler handler = new DownloadHandler(workDir, tempDir, httpClient);
        int status = handler.handleDownload(downloadList, this);
        if(deleteRetryNeeded || status ==  UPDATE_RETRY_NEEDED) {
            return UPDATE_RETRY_NEEDED;
        }
        return UPDATE_COMPLETE;
    }

    @Override
    protected void done() {
        System.out.println("Update done");
        try {
            Integer result = get();
            System.exit(result);
        } catch (Exception ex) {
            progressBar.setString("Update failed: " + ex.getMessage());
        }
    }

    public void publish(String data) {
        super.publish(data);
    }

    public void incrementCount() {
        downloadProgress++;
    }

    @Override
    protected void process(List<String> chunks) {
        double currentProgress = ((double) downloadProgress / (double) downloadTotal) * 100;
        System.out.println("Download total: " + downloadTotal);
        System.out.println("Download progress: " + downloadProgress);
        System.out.println("Current progress: " + currentProgress);
        progressBar.setValue(10 + (int) currentProgress);
        progressBar.setString("Processing: " + chunks.get(0));
    }
}

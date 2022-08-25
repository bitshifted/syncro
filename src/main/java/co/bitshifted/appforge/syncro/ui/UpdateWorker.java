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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UpdateWorker extends SwingWorker<Integer, String> {

    private final JProgressBar progressBar;
    private Path workDir;
    private Path tempDir;
    private SyncroHttpClient httpClient;
    private int downloadTotal;
    private int downloadProgress = 0;

    public UpdateWorker(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        System.out.println("Running in background");
        LaunchArgs launchArgs = new LaunchArgs();

        workDir = Paths.get(System.getProperty("user.dir"));
        System.out.println("work directory: " + workDir);
        String appId = launchArgs.getApplicationId();
        tempDir = Files.createTempDirectory("syncro-" + appId);
        System.out.println("temp directory: " + tempDir.toFile().getAbsolutePath());
        progressBar.setValue(2);
        httpClient = new SyncroHttpClient(launchArgs.getServerUrl(), appId, launchArgs.getReleaseId());
        UpdateInfo updateInfo = httpClient.checkForUpdates(tempDir);
        int result = -1;
        switch (updateInfo.getStatus()) {
            case UPDATE_AVAILABLE:
                progressBar.setString("Update is available");
                progressBar.setValue(5);
                processUpdate(updateInfo);
                break;
            case NO_UPDATE:
                progressBar.setString("No update available");
                break;
            case ERROR:
                progressBar.setString("Update check failed");
                break;
        }
        return  result;
//        if(updateInfo.getStatus() == UpdateCheckStatus.UPDATE_AVAILABLE) {
//            ReleaseProcessor processor = new ReleaseProcessor(updateInfo.getContent());
//            List<ReleaseEntry> releaseEntries = processor.getEntries(workDir);
//            System.out.println("Release entries: " + releaseEntries);
//            FileDiffChecker diffChecker = new FileDiffChecker(workDir);
//            diffChecker.process(releaseEntries);
//            System.out.println("Update list: " + diffChecker.getUpdateList());
//            System.out.println("delete list: " + diffChecker.getDeleteList());
//            System.out.println("New entries: " + diffChecker.getNewEntries());
//            diffChecker.getDeleteList().forEach(item -> {
//                try {
//                    Files.delete(item.getTarget());
//                    System.out.println("Successfully deleted file " + item.getTarget().toAbsolutePath());
//                } catch (IOException ex) {
//                    System.err.println("Failed to delete file: " + item.getTarget().toAbsolutePath());
//                }
//            });
//            List<ReleaseEntry> downloadList = new ArrayList<>(diffChecker.getUpdateList());
//            downloadList.addAll(diffChecker.getNewEntries());
//            DownloadHandler handler = new DownloadHandler(workDir, tempDir, httpClient);
//            handler.handleDownload(downloadList);
//
//        }
    }

    private void processUpdate(UpdateInfo updateInfo) throws Exception {
        progressBar.setString("Processing update info");
        ReleaseProcessor processor = new ReleaseProcessor(updateInfo.getContent());
        List<ReleaseEntry> releaseEntries = processor.getEntries(workDir);
        System.out.println("Release entries: " + releaseEntries);
        FileDiffChecker diffChecker = new FileDiffChecker(workDir);
        diffChecker.process(releaseEntries);
        progressBar.setValue(10);
        downloadTotal = diffChecker.getUpdateList().size() + diffChecker.getNewEntries().size();
        List<ReleaseEntry> downloadList = new ArrayList<>(diffChecker.getUpdateList());
        downloadList.addAll(diffChecker.getNewEntries());
        DownloadHandler handler = new DownloadHandler(workDir, tempDir, httpClient);
        handler.handleDownload(downloadList, this);
    }

    @Override
    protected void done() {
        System.out.println("Update done");
        try {
            Integer result = get();
            System.exit(result);
        } catch(Exception ex) {
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
        progressBar.setValue(10 + (int)currentProgress);
        progressBar.setString("Processing: " + chunks.get(0));
    }
}

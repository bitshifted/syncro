/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.appforge.syncro;

import co.bitshifted.appforge.syncro.http.DownloadHandler;
import co.bitshifted.appforge.syncro.http.SyncroHttpClient;
import co.bitshifted.appforge.syncro.launch.LaunchArgs;
import co.bitshifted.appforge.syncro.launch.ReleaseProcessor;
import co.bitshifted.appforge.syncro.model.ReleaseEntry;
import co.bitshifted.appforge.syncro.model.UpdateCheckStatus;
import co.bitshifted.appforge.syncro.model.UpdateInfo;
import co.bitshifted.appforge.syncro.sync.FileDiffChecker;
import co.bitshifted.appforge.syncro.ui.UpdateWindow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
public class Syncro {

	public static void main(String... args) throws Exception {

		UpdateWindow window = new UpdateWindow();
		window.init();
		window.pack();
		window.setVisible(true);

		LaunchArgs launchArgs = new LaunchArgs();

		Path workDir = Paths.get(System.getProperty("user.dir"));
		System.out.println("work directory: " + workDir);
		String appId = launchArgs.getApplicationId();
		Path tempDir = Files.createTempDirectory("syncro-" + appId);
		System.out.println("temp directory: " + tempDir.toFile().getAbsolutePath());
		SyncroHttpClient httpClient = new SyncroHttpClient(launchArgs.getServerUrl(), appId, launchArgs.getReleaseId());
		UpdateInfo updateInfo = httpClient.checkForUpdates(tempDir);
		if(updateInfo.getStatus() == UpdateCheckStatus.UPDATE_AVAILABLE) {
			ReleaseProcessor processor = new ReleaseProcessor(updateInfo.getContent());
			List<ReleaseEntry> releaseEntries = processor.getEntries(workDir);
			System.out.println("Release entries: " + releaseEntries);
			FileDiffChecker diffChecker = new FileDiffChecker(workDir);
			diffChecker.process(releaseEntries);
			System.out.println("Update list: " + diffChecker.getUpdateList());
			System.out.println("delete list: " + diffChecker.getDeleteList());
			System.out.println("New entries: " + diffChecker.getNewEntries());
			diffChecker.getDeleteList().forEach(item -> {
				try {
					Files.delete(item.getTarget());
					System.out.println("Successfully deleted file " + item.getTarget().toAbsolutePath());
				} catch (IOException ex) {
					System.err.println("Failed to delete file: " + item.getTarget().toAbsolutePath());
				}
			});
			List<ReleaseEntry> downloadList = new ArrayList<>(diffChecker.getUpdateList());
			downloadList.addAll(diffChecker.getNewEntries());
			DownloadHandler handler = new DownloadHandler(workDir, tempDir, httpClient);
			handler.handleDownload(downloadList);

		}
	}
}

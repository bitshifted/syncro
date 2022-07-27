/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro;

import co.bitshifted.xapps.syncro.http.DownloadHandler;
import co.bitshifted.xapps.syncro.launch.LaunchArgs;
import co.bitshifted.xapps.syncro.sync.FileDiffChecker;
import co.bitshifted.xapps.syncro.http.SyncroHttpClient;
import co.bitshifted.xapps.syncro.launch.ReleaseProcessor;
import co.bitshifted.xapps.syncro.model.ReleaseEntry;
import co.bitshifted.xapps.syncro.model.UpdateCheckStatus;
import co.bitshifted.xapps.syncro.model.UpdateInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
public class Syncro {

	public static void main(String... args) throws Exception {

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

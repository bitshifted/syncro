/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro;

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
		if(args.length != 3) {
			throw new IllegalArgumentException("Required arguments: server URL, application ID, release ID");
		}

		Path workDir = Paths.get(System.getProperty("user.dir"));
		System.out.println("work directory: " + workDir.toString());
		String appId = args[1];
		Path tempDir = Files.createTempDirectory("syncro-" + appId);
		System.out.println("temp directory: " + tempDir.toFile().getAbsolutePath());
		SyncroHttpClient httpClient = new SyncroHttpClient(args[0], appId, args[2]);
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
			List<ReleaseEntry> downloadList = new ArrayList<>(diffChecker.getUpdateList());
			downloadList.addAll(diffChecker.getNewEntries());
			downloadList.forEach(d -> {
				try {
					InputStream is = httpClient.getResource(d.getHash());
					Files.copy(is, d.getTarget(), StandardCopyOption.REPLACE_EXISTING);
				} catch(IOException ex) {
					throw new IllegalStateException(ex);
				}

			});

		}
	}
}

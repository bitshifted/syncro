/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.appforge.syncro.http;

import co.bitshifted.appforge.syncro.model.ReleaseEntry;
import co.bitshifted.appforge.syncro.ui.UpdateWorker;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
public class DownloadHandler {

	private static final String RETRY_FILE_LIST = "retries.txt";

	private final Path workDir;
	private final Path tempDir;
	private final SyncroHttpClient httpClient;

	public DownloadHandler(Path workDir, Path tempDir, SyncroHttpClient httpClient){
		this.workDir = workDir;
		this.tempDir = tempDir;
		this.httpClient = httpClient;
	}

	public void handleDownload(List<ReleaseEntry> entries, UpdateWorker worker) {
		List<ReleaseEntry> retries = new ArrayList<>();
		entries.stream().forEach(e -> {
			worker.publish(workDir.relativize(e.getTarget()).toString());
			worker.incrementCount();
			byte[] fileData = null;
			try {
				fileData = downloadData(e.getHash());
				Files.createDirectories(e.getTarget().getParent());
				Files.copy(new ByteArrayInputStream(fileData), e.getTarget(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Successfully downloaded file " + e.getTarget().toAbsolutePath());
				if(e.isExecutable()) {
					e.getTarget().toFile().setExecutable(true);
					System.out.println("File " + e.getTarget().toString() + " marked as executable");
				}
			} catch(IOException ex) {
				System.err.println("Failed to write file " + e.getTarget().toAbsolutePath() + ". Exception: " + ex.getClass().getName() +  ", message: " + ex.getMessage());
				ex.printStackTrace();
				if(fileData == null){
					throw new IllegalStateException("Failed to get file data");
				}
				saveRetry(e, fileData);
			}
		});
	}

	private void saveRetry(ReleaseEntry entry, byte[] data) {
		Path tmpFile = tempDir.resolve(entry.getHash());
		try(ByteArrayInputStream bin = new ByteArrayInputStream(data)) {
			Files.copy(bin, tmpFile, StandardCopyOption.REPLACE_EXISTING);
			if(entry.isExecutable()) {
				tmpFile.toFile().setExecutable(true);
			}
		} catch(IOException ex) {
			System.err.println("Failed to copy file " + tmpFile.toAbsolutePath().toString());
		}
	}

	private byte[] downloadData(String hash) throws IOException {
		int size = 8192;
		try(InputStream is = httpClient.getResource(hash); ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
			byte[] buf = new byte[size];
			int length;
			while ((length = is.read(buf)) != -1) {
				bout.write(buf, 0, length);
			}
			return bout.toByteArray();
		}
	}
}

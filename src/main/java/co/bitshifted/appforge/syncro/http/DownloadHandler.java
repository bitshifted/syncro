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
import java.io.*;
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
	private final Path launcherFilePath;
	private final SyncroHttpClient httpClient;
	private final StringBuilder retriesBuilder;

	public DownloadHandler(Path workDir, Path tempDir, SyncroHttpClient httpClient, Path launcherFilePath){
		this.workDir = workDir;
		this.tempDir = tempDir;
		this.launcherFilePath = launcherFilePath;
		this.httpClient = httpClient;
		this.retriesBuilder = new StringBuilder();
	}

	public int handleDownload(List<ReleaseEntry> entries, UpdateWorker worker) {
		entries.stream().forEach(e -> {
			worker.publish(workDir.relativize(e.getTarget()).toString());
			worker.incrementCount();
			byte[] fileData = null;
			try {
				fileData = downloadData(e.getHash());
				if(e.getTarget().equals(launcherFilePath)) {
					System.out.println("Launcher file update detected");
					Path tmpFile = saveRetry(e, fileData);
					addRetryEntry(tmpFile, e.getTarget());
					return; // skip to next item
				}
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
				Path tmpFile = saveRetry(e, fileData);
				addRetryEntry(tmpFile, e.getTarget());
			}
		});
		if(retriesBuilder.length() > 0) {
			System.out.println("Saving retries to file");
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(workDir.resolve(RETRY_FILE_LIST).toFile()))) {
				bw.write(retriesBuilder.toString());
			} catch (IOException ex) {
				System.err.println("Failed to create retry file: " + ex.getMessage());
			}
			return UpdateWorker.UPDATE_RETRY_NEEDED;
		}
		return UpdateWorker.UPDATE_COMPLETE;
	}

	private Path saveRetry(ReleaseEntry entry, byte[] data) {
		Path tmpFile = tempDir.resolve(entry.getHash());
		try(ByteArrayInputStream bin = new ByteArrayInputStream(data)) {
			Files.copy(bin, tmpFile, StandardCopyOption.REPLACE_EXISTING);
			if(entry.isExecutable()) {
				tmpFile.toFile().setExecutable(true);
			}
		} catch(IOException ex) {
			System.err.println("Failed to copy file " + tmpFile.toAbsolutePath());
		}
		return tmpFile;
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

	private  void addRetryEntry(Path source, Path target) {
		retriesBuilder.append(source.toFile().getAbsolutePath())
			.append("->")
			.append(target.toFile().getAbsolutePath())
			.append("\n");
	}
}

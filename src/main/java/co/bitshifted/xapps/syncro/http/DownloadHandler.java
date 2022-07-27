/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.http;

import co.bitshifted.xapps.syncro.SyncroUtils;
import co.bitshifted.xapps.syncro.model.DownloadResult;
import co.bitshifted.xapps.syncro.model.ReleaseEntry;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.*;
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

	public void handleDownload(List<ReleaseEntry> entries) {
		List<ReleaseEntry> retries = new ArrayList<>();
		entries.stream().forEach(e -> {
			byte[] fileData = null;
			try {
				fileData = downloadData(e.getHash());
				Files.copy(new ByteArrayInputStream(fileData), e.getTarget(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Successfully downloaded file " + e.getTarget().toAbsolutePath());
			} catch(IOException ex) {
				System.err.println("Failed to write file " + e.getTarget().toAbsolutePath().toString());
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

//	public DownloadResult handleDownload(InputStream in) {
//		var bytesWritten = 0L;
//		try {
//			var src  = Channels.newChannel(in);
//			var dest  = Channels.newChannel(Files.newOutputStream(target));
//			bytesWritten = copyData(src, dest);
//			src.close();
//			dest.close();
//			System.out.println("Wrote file " + target.toString());
//			return new DownloadResult(DownloadResult.Result.SUCCESS, target);
//		} catch(IOException ex) {
//			System.err.println("Failed to download file: " + ex.getMessage());
//			return new DownloadResult(DownloadResult.Result.FAILURE, null);
//		}
//	}

//	private long copyData(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
//		var buffer = ByteBuffer.allocate(8 * 1024);
//		var totalBytes = 0L;
//		var current = 0;
//		while (current != -1) {
//			current = src.read(buffer);
//			buffer.flip();
//			dest.write(buffer);
//			buffer.compact();
//			totalBytes += current;
//		}
//		buffer.flip();
//		while (buffer.hasRemaining()) {
//			totalBytes += dest.write(buffer);
//		}
//		return totalBytes;
//	}
}

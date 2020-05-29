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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Vladimir Djurovic
 */
public class DownloadHandler {
	private final String applicationId;
	private final Path target;

	public DownloadHandler(String appId, Path target){
		this.applicationId = appId;
		this.target = target;
		System.out.println("target download: " + target.toString());
	}

	public DownloadResult handleDownload(InputStream in) {
		var bytesWritten = 0L;
		try {
			var src  = Channels.newChannel(in);
			var dest  = Channels.newChannel(Files.newOutputStream(target));
			bytesWritten = copyData(src, dest);
			src.close();
			dest.close();
			System.out.println("Wrote file " + target.toString());
			return new DownloadResult(DownloadResult.Result.SUCCESS, target);
		} catch(IOException ex) {
			System.err.println("Failed to download file: " + ex.getMessage());
			return new DownloadResult(DownloadResult.Result.FAILURE, null);
		}
	}

	private long copyData(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
		var buffer = ByteBuffer.allocate(8 * 1024);
		var totalBytes = 0L;
		var current = 0;
		while (current != -1) {
			current = src.read(buffer);
			buffer.flip();
			dest.write(buffer);
			buffer.compact();
			totalBytes += current;
		}
		buffer.flip();
		while (buffer.hasRemaining()) {
			totalBytes += dest.write(buffer);
		}
		return totalBytes;
	}
}

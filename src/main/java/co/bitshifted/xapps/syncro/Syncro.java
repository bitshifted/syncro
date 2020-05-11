/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro;

import co.bitshfted.xapps.zsync.Zsync;
import co.bitshifted.xapps.syncro.http.DownloadHandler;
import co.bitshifted.xapps.syncro.http.SyncroHttpClient;
import co.bitshifted.xapps.syncro.model.UpdateCheckStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Vladimir Djurovic
 */
public class Syncro {

	public static void main(String... args) throws Exception {
		if(args.length != 3) {
			throw new IllegalArgumentException("Required arguments: server URL, application ID, current version");
		}

		var appId = args[1];
		var httpClient = new SyncroHttpClient(args[0], appId, args[2]);
		var status = httpClient.checkForUpdates();
		if(status.getStatus() == UpdateCheckStatus.UPDATE_AVAILABLE) {
			var appCacheDir = SyncroUtils.getAppCacheDir(appId);
			if(!Files.exists(appCacheDir)) {
				Files.createDirectories(appCacheDir);
			}
			// download control files
			var downloadList = List.of(httpClient.downloadFull(status.getContentUrl().toURI(), new DownloadHandler(appId, "contents.zip.zsync")) ,
					httpClient.downloadFull(status.getModulesUrl().toURI(), new DownloadHandler(appId, "modules.zip.zsync")));
			downloadList.forEach(dl -> dl.join());
			// download update files
			var updateFutures = downloadList.stream()
					.map(f ->
						CompletableFuture.supplyAsync(() -> {
							try {
								var first = f.get().getOutput();
								System.out.println("Output: " + first.toString());
								Zsync.Options options = new Zsync.Options();
								options.setOutputFile(first.getParent().resolve(first.toFile().getName().replaceAll(".zsync", "")));
								var zsync = new Zsync();
								Path out  = zsync.zsync(first.toUri(), options);
								return out;
							} catch(Exception ex) {
								ex.printStackTrace();
								return null;
							}
						}))
					.collect(Collectors.toList());
			updateFutures.forEach(uf -> uf.join());
		}
	}
}

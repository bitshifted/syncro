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
import co.bitshifted.xapps.syncro.launch.AppLauncher;
import co.bitshifted.xapps.syncro.model.UpdateCheckStatus;
import co.bitshifted.xapps.syncro.sync.MacFileSyncer;

import java.nio.file.Files;
import java.nio.file.Path;
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
			var workDir = Path.of(System.getProperty("user.dir"));
			// download control files
			var downloadList = status.getDetails().stream()
					.map(d -> httpClient.downloadFull(d.getUri(),
							new DownloadHandler(appId, workDir.resolve(d.getFileName()))))
					.collect(Collectors.toList());
			downloadList.forEach(dl -> dl.join());
			// download update files
			var updateFutures = downloadList.stream()
					.map(f ->
						CompletableFuture.supplyAsync(() -> {
							try {
								var first = f.get().getOutput();
								System.out.println("Output: " + first.toString());
								Zsync.Options options = new Zsync.Options();
								var fileName = first.toFile().getName().replaceAll(".zsync", "");
								if(Files.exists(SyncroUtils.getAppCacheDir(appId).resolve(fileName))) {
									options.addInputFile(SyncroUtils.getAppCacheDir(appId).resolve(fileName));
									System.out.println("Input file: " + SyncroUtils.getAppCacheDir(appId).resolve(fileName));
								}
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
			var syncer = new MacFileSyncer(updateFutures.stream()
					.map(uf -> {
						try {
							return uf.get();
						} catch(Exception ex) {
							ex.printStackTrace();
							return null;
						}
					})
					.filter(p -> p != null)
					.collect(Collectors.toList()));
			var updateDir = syncer.sync();
			var launcher = new AppLauncher();
			var process = launcher.launch(updateDir);
			System.out.println("launch status: " + process.info().toString());
		}
	}
}

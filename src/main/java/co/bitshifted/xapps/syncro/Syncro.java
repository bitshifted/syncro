/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro;

import co.bitshifted.xapps.syncro.diff.FileDiffChecker;
import co.bitshifted.xapps.syncro.http.SyncroHttpClient;
import co.bitshifted.xapps.syncro.launch.ReleaseProcessor;
import co.bitshifted.xapps.syncro.model.ReleaseEntry;
import co.bitshifted.xapps.syncro.model.UpdateCheckStatus;
import co.bitshifted.xapps.syncro.model.UpdateInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
public class Syncro {

	public static void main(String... args) throws Exception {
		if(args.length != 3) {
			throw new IllegalArgumentException("Required arguments: server URL, application ID, release ID");
		}

		String appId = args[1];
		Path tempDir = Files.createTempDirectory("syncro-" + appId);
		SyncroHttpClient httpClient = new SyncroHttpClient(args[0], appId, args[2]);
		UpdateInfo updateInfo = httpClient.checkForUpdates(tempDir);
		if(updateInfo.getStatus() == UpdateCheckStatus.UPDATE_AVAILABLE) {
			ReleaseProcessor processor = new ReleaseProcessor(updateInfo.getContent());
			List<ReleaseEntry> releaseEntries = processor.getEntries(Paths.get("/home/vlada/local/work/bitshift/xapps/win-output"));
			System.out.println("Release entries: " + releaseEntries);
			FileDiffChecker diffChecker = new FileDiffChecker(Paths.get("/home/vlada/local/work/bitshift/xapps/win-output"));
			diffChecker.process(releaseEntries);
			System.out.println("Update list: " + diffChecker.getUpdateList());
			System.out.println("delete list: " + diffChecker.getDeleteList());
//		if(status.getStatus() == UpdateCheckStatus.UPDATE_AVAILABLE) {
//			var appCacheDir = SyncroUtils.getAppCacheDir(appId);
//			if(!Files.exists(appCacheDir)) {
//				Files.createDirectories(appCacheDir);
//			}
//			var workDir = Path.of(System.getProperty("user.dir"));
//			// download control files
//			var downloadList = status.getDetails().stream()
//					.map(d -> httpClient.downloadFull(d.getUri(),
//							new DownloadHandler(appId, workDir.resolve(d.getFileName()))))
//					.collect(Collectors.toList());
//			downloadList.forEach(dl -> dl.join());
//			// download update files
//			var updateFutures = downloadList.stream()
//					.map(f ->
//						CompletableFuture.supplyAsync(() -> {
//							try {
//								var first = f.get().getOutput();
//								System.out.println("Output: " + first.toString());
//								Zsync.Options options = new Zsync.Options();
//								var fileName = first.toFile().getName().replaceAll(".zsync", "");
//								if(Files.exists(SyncroUtils.getAppCacheDir(appId).resolve(fileName))) {
//									options.addInputFile(SyncroUtils.getAppCacheDir(appId).resolve(fileName));
//									System.out.println("Input file: " + SyncroUtils.getAppCacheDir(appId).resolve(fileName));
//								}
//								var zsync = new Zsync();
//								Path out  = zsync.zsync(first.toUri(), options);
//								return out;
//							} catch(Exception ex) {
//								ex.printStackTrace();
//								return null;
//							}
//						}))
//					.collect(Collectors.toList());
//			updateFutures.forEach(uf -> uf.join());
//			var osType = SyncroUtils.getOsType();
//			if("mac".equals(osType)) {
//				System.out.println("Mac OS found");
//				var syncer = new MacFileSyncer(updateFutures.stream()
//						.map(uf -> {
//							try {
//								return uf.get();
//							} catch(Exception ex) {
//								ex.printStackTrace();
//								return null;
//							}
//						})
//						.filter(p -> p != null)
//						.collect(Collectors.toList()));
//				var updateDir = syncer.sync();
//				var launcher = new AppLauncher();
//				var process = launcher.launch(updateDir);
//				System.out.println("launch status: " + process.info().toString());
//				syncer.cleanup();
//			} else if("win".equals(osType)) {
//				System.out.println("Windows OS found");
//				var syncer = new WindowsFileSyncer(updateFutures.stream()
//						.map(uf -> {
//							try {
//								return uf.get();
//							} catch(Exception ex) {
//								ex.printStackTrace();
//								return null;
//							}
//						})
//						.filter(p -> p != null)
//						.collect(Collectors.toList()));
//				var updateDir = syncer.sync();
//				var launcher = new AppLauncher();
//				var process = launcher.launch(updateDir);
//				System.out.println("launch status: " + process.info().toString());
//				syncer.cleanup();
//			}

		}
	}
}

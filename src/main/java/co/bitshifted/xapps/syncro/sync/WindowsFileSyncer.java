/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.sync;

import co.bitshifted.xapps.syncro.SyncroUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Vladimir Djurovic
 */
public class WindowsFileSyncer {
	private static final String EXE_COMMENT = "exec:true";

	private final List<Path> sourcePaths;
	private final Path appBundleDirectory;

	public WindowsFileSyncer(List<Path> sourcePaths) {
		this.sourcePaths = sourcePaths;
		appBundleDirectory = Path.of(System.getProperty("user.dir")).getParent().getParent();
		System.out.println("app bundle dir: " + appBundleDirectory.toString());
		System.out.println("paths: " + sourcePaths);
	}

	public Path sync() throws IOException {
		var newPath = createBackup();
		extractUpdates(newPath);
		// point to new version
		var contentsSymLink = appBundleDirectory.resolve("Contents");
		Files.delete(contentsSymLink);
		var symlink = Files.createSymbolicLink(contentsSymLink, appBundleDirectory.resolve("Contents.new"));
		return symlink.resolve("MacOS"); // for Mac, need to return this directory
	}

	public void cleanup() throws IOException {
		System.out.println("Cleaning up");
		// delete Contents.old directory
		var oldContents = appBundleDirectory.resolve("Contents.old");
		SyncroUtils.deleteDirectory(oldContents.toFile());
		// delete Contents symlink
		Files.delete(appBundleDirectory.resolve("Contents"));
		// rename Contents.new
		var contents = appBundleDirectory.resolve("Contents");
		Files.move(appBundleDirectory.resolve("Contents.new"), contents);
	}

	private Path createBackup() throws IOException {
		var contentsDir = appBundleDirectory.resolve("Contents"); // app Contents dir
		//var curDirName = appDirectory.getParent().toFile().getName(); // resolves to Contents dir
		System.out.println("Contents dir : " + contentsDir);
		var backupDir = appBundleDirectory.resolve("Contents.old");
		Files.move(contentsDir, backupDir, StandardCopyOption.REPLACE_EXISTING); // move Contents to Contents.old
		Files.createSymbolicLink(appBundleDirectory.resolve("Contents"), backupDir); // symbolic Contents->Contents.old
		return  Files.createDirectory(appBundleDirectory.resolve("Contents.new")); // Contents.new
	}

	private void extractUpdates(Path targetBasePath) {
		System.out.println("Extraction base path: " + targetBasePath);
		sourcePaths.forEach(sp -> {
			try {
				extractZipFile(sp, targetBasePath);
			} catch(IOException ex) {
				ex.printStackTrace();
			}

		});
	}

	private void extractZipFile(Path zipFilePath, Path targetBase) throws IOException {
		var zipFile = new ZipFile(zipFilePath.toFile());
		var entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			var zipEntry = (ZipEntry)entries.nextElement();
			var entryName = zipEntry.getName();
			System.out.println("entry name: " + entryName);
			var parts = entryName.split("/");
			var target = targetBase;
			if(zipFilePath.toFile().getName().equals("modules.zip") && "modules".equals(entryName)) {
				// special handling for java modules file
				System.out.println("Found java modules file");
				target = targetBase.resolve("MacOS").resolve("jre").resolve("lib").resolve(entryName);
			} else {
				for(String part : parts) {
					target = target.resolve(part);
				}
			}

			System.out.println("Extraction target: " + target);
			Files.createDirectories(target.getParent());
			Files.copy(zipFile.getInputStream(zipEntry), target, StandardCopyOption.REPLACE_EXISTING);
			// set file executable, if needed
			if(EXE_COMMENT.equals(zipEntry.getComment())) {
				target.toFile().setExecutable(true);
			}
		}
	}
}

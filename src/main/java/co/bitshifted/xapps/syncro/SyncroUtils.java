/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * @author Vladimir Djurovic
 */
public final class SyncroUtils {

	private static final String XAPPS_BASE_DIR = ".xapps";

	private SyncroUtils() {

	}

	public static Path getAppCacheDir(String applicationId) {
		var userHomeDirPath = System.getProperty("user.home");
		return Path.of(userHomeDirPath, XAPPS_BASE_DIR, "cache", applicationId);
	}

	public static String getOsType() {
		String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
			return "mac";
		} else if (OS.indexOf("win") >= 0) {
			return "win";
		} else if (OS.indexOf("nux") >= 0) {
			return "linux";
		} else {
			return "other";
		}
	}

	public static String getCpuArch() {
		var arch = System.getProperty("os.arch");
		if(arch.equals("x86_64")){
			return "x64";
		} else {
			return "x86";
		}
	}

	public static boolean deleteDirectory(File directory) {
		File[] allContents = directory.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directory.delete();
	}
}

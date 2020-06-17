/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author Vladimir Djurovic
 */
public class Cleanup {

	public static void main(String... args) throws Exception {
		if(args.length != 2) {
			System.err.println("Required arguments: app directory, source directory");
		}
		Thread.sleep(5000);
		var current = Path.of(args[0]);
		var tmpDir = Path.of(args[1]);
		SyncroUtils.deleteDirectory(current.toFile());
		if(!Files.exists(current)) {
			Files.createDirectory(current);
		}
		// copy new data
		System.out.println("Copying new data");
		Files.walk(tmpDir).forEach(p -> {
			System.out.println("Current item: " + p);
			var relPath = tmpDir.relativize(p);
			System.out.println("Relative path: " + relPath);
			var target = current.resolve(relPath);
			try {
				Files.createDirectories(target.getParent());
				Files.copy(p, target, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Copied " + target);
			} catch(Exception ex) {
				ex.printStackTrace();
			}

		});
	}
}

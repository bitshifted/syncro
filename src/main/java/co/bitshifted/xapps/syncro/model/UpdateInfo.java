/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladimir Djurovic
 */
public class UpdateInfo {
	private final UpdateCheckStatus status;
	private final Map<String, URL> targetMap = new HashMap<>();

	public UpdateInfo(UpdateCheckStatus status, String targetData) throws IOException{
		this.status = status;
		initTargets(targetData);
	}

	public UpdateInfo(UpdateCheckStatus status) {
		this.status = status;
	}

	public UpdateCheckStatus getStatus() {
		return status;
	}

	public URL getContentUrl() {
		return targetMap.get("contents.zip");
	}

	public URL getModulesUrl() {
		return targetMap.get("modules.zip");
	}

	private void initTargets(String data) throws IOException {
		try(var reader = new BufferedReader(new StringReader(data))) {
			String line;
			while ((line = reader.readLine()) != null) {
				var parts = line.split("->");
				if(parts.length == 2) {
					targetMap.put(parts[0].trim(), new URL(parts[1].trim()));
				}
			}
		}
	}

}

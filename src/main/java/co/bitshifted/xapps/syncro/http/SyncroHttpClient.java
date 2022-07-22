/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.http;

import co.bitshifted.xapps.syncro.model.UpdateCheckStatus;
import co.bitshifted.xapps.syncro.model.UpdateInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static co.bitshifted.xapps.syncro.http.HttpConstants.*;

/**
 * @author Vladimir Djurovic
 */
public class SyncroHttpClient {

	private final String serverUrl;
	private final String applicationId;
	private final String releaseId;

	public SyncroHttpClient(String serverUrl, String applicationId, String version) {
		this.serverUrl = serverUrl;
		this.applicationId = applicationId;
		this.releaseId = version;
	}

	public UpdateInfo checkForUpdates(Path targetDir)  {
		try {
			HttpURLConnection conn = (HttpURLConnection) updateCheckUrl(serverUrl, applicationId, releaseId).openConnection();
			conn.setInstanceFollowRedirects(true);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();
			if(responseCode == HTTP_STATUS_NOT_MODIFIED) {
				return new UpdateInfo(UpdateCheckStatus.NO_UPDATE);
			}
			if(responseCode == HTTP_STATUS_OK) {
				return new UpdateInfo(UpdateCheckStatus.UPDATE_AVAILABLE, readContent(conn.getInputStream()));
			}
		} catch(Exception ex) {
			System.err.println("Failed to check for updates");
			ex.printStackTrace(System.err);
		}
		return new UpdateInfo(UpdateCheckStatus.ERROR);
	}

	public InputStream getResource(String hash) throws IOException {
		try {
			HttpURLConnection conn = (HttpURLConnection) getContentUrl(serverUrl, hash).openConnection();
			conn.setInstanceFollowRedirects(true);
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("GET");
			return conn.getInputStream();
		} catch(Exception ex) {
			System.err.println("Failed to get content");
			ex.printStackTrace(System.err);
			throw new IOException(ex.getMessage());
		}
	}

	private String readContent(InputStream is) throws IOException {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			return br.lines().collect(Collectors.joining("\n"));
		}
	}

}

/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.http;

import co.bitshifted.xapps.syncro.model.DownloadResult;
import co.bitshifted.xapps.syncro.model.UpdateCheckStatus;
import co.bitshifted.xapps.syncro.model.UpdateInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static co.bitshifted.xapps.syncro.http.HttpConstants.*;

/**
 * @author Vladimir Djurovic
 */
public class SyncroHttpClient {

	private final HttpClient httpClient;
	private final String serverUrl;
	private final String applicationId;
	private final String version;

	public SyncroHttpClient(String serverUrl, String applicationId, String version) {
		httpClient = HttpClient.newBuilder().build();
		this.serverUrl = serverUrl;
		this.applicationId = applicationId;
		this.version = version;
	}

	public UpdateInfo checkForUpdates()  {
		var request = HttpRequest.newBuilder(
				URI.create(createUpdateCheckUrl(serverUrl, applicationId, version)))
				.GET().build();
		try {
			var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if(response.statusCode() == HTTP_STATUS_NOT_MODIFIED) {
				return new UpdateInfo(UpdateCheckStatus.NO_UPDATE);
			}
			if(response.statusCode() == HTTP_STATUS_OK) {
				return new UpdateInfo(UpdateCheckStatus.UPDATE_AVAILABLE, response.body());
			}
		} catch(IOException | InterruptedException ex) {
			System.err.println("Failed to check for updates");
			ex.printStackTrace(System.err);
		}
		return new UpdateInfo(UpdateCheckStatus.ERROR);
	}

	public CompletableFuture<DownloadResult> downloadFull(URI uri, DownloadHandler handler)  {
		System.out.println("URL: " + uri.toString());
		return httpClient.sendAsync(HttpRequest.newBuilder(uri).GET().build(),
				HttpResponse.BodyHandlers.ofInputStream())
				.thenApply(response -> handler.handleDownload(response.body()));
	}
}

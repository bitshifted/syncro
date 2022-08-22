/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.appforge.syncro.http;

import java.net.MalformedURLException;
import java.net.URL;

import static co.bitshifted.appforge.syncro.SyncroUtils.*;

/**
 * @author Vladimir Djurovic
 */
public final class HttpConstants {

	public static final String UPDATE_CHECK_URL_TEMPLATE = "/v1/releases/app/%s/current/%s/os/%s";
	public static final String GET_CONTENT_URL_TEMPLATE = "/v1/content/%s";
	public static final int HTTP_STATUS_OK = 200;
	public static final int HTTP_STATUS_NOT_MODIFIED = 304;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private HttpConstants() {

	}

	public static URL updateCheckUrl(String serverUrl, String appId, String releaseId) throws MalformedURLException {
		return new URL(urlFromTemplate(UPDATE_CHECK_URL_TEMPLATE, serverUrl, appId, releaseId, getOsType()));
	}

	public static URL getContentUrl(String serverUrl, String hash) throws MalformedURLException {
		return new URL(urlFromTemplate(GET_CONTENT_URL_TEMPLATE, serverUrl, hash));
	}

	private static String urlFromTemplate(String template, String serverUrl, String... vars) {
		StringBuilder sb = new StringBuilder(serverUrl);
		String url = String.format(template, vars);
		if(serverUrl.endsWith("/")) {
			sb.append(url.substring(1));
		}
		sb.append(url);
		return sb.toString();
	}
}

/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.http;

import co.bitshifted.xapps.syncro.SyncroUtils;

import static co.bitshifted.xapps.syncro.SyncroUtils.*;

/**
 * @author Vladimir Djurovic
 */
public final class HttpConstants {

	public static final String UPDATE_CHECK_URL_TEMPLATE = "/update/app/%s/release/%s?os=%s&cpu=%s";
	public static final int HTTP_STATUS_OK = 200;
	public static final int HTTP_STATUS_NOT_MODIFIED = 304;

	/**
	 * Private constructor to prevent instantiation.
	 */
	private HttpConstants() {

	}

	public static String createUpdateCheckUrl(String serverUrl, String appId, String version) {
		StringBuilder sb = new StringBuilder(serverUrl);
		String deployUrl = String.format(UPDATE_CHECK_URL_TEMPLATE, appId, version, getOsType(), getCpuArch());
		if(serverUrl.endsWith("/")) {
			sb.append(deployUrl.substring(1));
		}
		sb.append(deployUrl);
		return sb.toString();
	}
}

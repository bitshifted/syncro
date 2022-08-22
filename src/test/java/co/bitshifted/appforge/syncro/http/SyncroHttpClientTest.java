/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.http;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * @author Vladimir Djurovic
 */
public class SyncroHttpClientTest {

//	private SyncroHttpClient httpClient;
//
//	@Rule
//	public WireMockRule server = new WireMockRule(options()
//			.port(9000)
//			.notifier(new ConsoleNotifier(false)) // set to true for verbose logging
//	);
//
//	@Before
//	public void setup() {
//		httpClient = new SyncroHttpClient("http://localhost:9000", "appId", "1234");
//	}

//	@Test
//	public void testCheckForUpdateNoUpdate() {
//		stubFor(get(urlPathMatching("\\/update\\/app\\/.+\\/release\\/.+"))
//				.willReturn(aResponse()
//						.withStatus(HTTP_STATUS_NOT_MODIFIED)));
//		var status = httpClient.checkForUpdates();
//
//		assertEquals(UpdateCheckStatus.NO_UPDATE, status.getStatus());
//	}

//	@Test
//	@Ignore
//	public void testCheckForUpdateAvailable() {
//		String responseBody = "contents.zip -> http://server.com/path/to/file.zsync\n" +
//				"modules.zip-> http://server.net/another/path/file.zsync";
//		stubFor(get(urlPathMatching("\\/updates\\/app\\/.+\\/version\\/.+"))
//				.willReturn(aResponse()
//						.withBody(responseBody)
//						.withStatus(HTTP_STATUS_OK)));
//		var status = httpClient.checkForUpdates();
//		assertEquals(UpdateCheckStatus.UPDATE_AVAILABLE, status.getStatus());
//	}
}

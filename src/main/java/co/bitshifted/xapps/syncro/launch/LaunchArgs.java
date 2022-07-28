/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.xapps.syncro.launch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LaunchArgs {
    private static final String CONFIG_PROPERTIES_FILE = "/syncro.properties";
    private static final String SERVER_URL_PROPERTY = "syncro.server.url";
    private static final String APPLICATION_ID_PROPERTY = "syncro.application.id";
    private static final String RELEASE_ID_PROPERTY = "syncro.release.id";

    private final Properties properties;

    public LaunchArgs() throws IOException {
        properties = new Properties();
        try(InputStream is = getClass().getResourceAsStream(CONFIG_PROPERTIES_FILE)) {
            properties.load(is);
        }
    }

    public String getServerUrl() {
        return properties.getProperty(SERVER_URL_PROPERTY);
    }

    public String getApplicationId() {
        return properties.getProperty(APPLICATION_ID_PROPERTY);
    }

    public String getReleaseId() {
        return properties.getProperty(RELEASE_ID_PROPERTY);
    }
}

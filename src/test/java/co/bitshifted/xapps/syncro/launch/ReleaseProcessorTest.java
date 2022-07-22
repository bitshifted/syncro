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

import co.bitshifted.xapps.syncro.model.ReleaseEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ReleaseProcessorTest {

    @Test
    void testGetEntries() throws Exception {
        String data;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/release-info.xml")))) {
            data = br.lines().collect(Collectors.joining("\n"));
        }

        ReleaseProcessor processor = new ReleaseProcessor(data);
        List<ReleaseEntry> entries = processor.getEntries(Paths.get(System.getProperty("user.dir")));
        Assertions.assertNotNull(entries);
        Assertions.assertEquals(118, entries.size());
    }
}

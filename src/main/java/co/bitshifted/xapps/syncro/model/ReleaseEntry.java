/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.xapps.syncro.model;

import java.nio.file.Path;

public class ReleaseEntry {
    private final  String hash;
    private final Path target;
    private final boolean executable;

    public ReleaseEntry(String hash, Path target, boolean executable) {
        this.hash = hash;
        this.target = target;
        this.executable = executable;
    }

    public String getHash() {
        return hash;
    }

    public Path getTarget() {
        return target;
    }

    public boolean isExecutable() {
        return executable;
    }

    @Override
    public String toString() {
        return "ReleaseEntry{" +
            "hash='" + hash + '\'' +
            ", target=" + target +
            ", executable=" + executable +
            '}';
    }
}

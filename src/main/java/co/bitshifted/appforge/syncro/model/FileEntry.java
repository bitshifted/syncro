/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.syncro.model;

import java.io.File;
import java.nio.file.Path;

public class FileEntry {

    private final String hash;
    private final Path target;

    public FileEntry(String hash, Path target) {
        this.hash = hash;
        this.target = target;
    }

    public String getHash() {
        return hash;
    }

    public Path getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "FileEntry{" +
            "hash='" + hash + '\'' +
            ", target=" + target +
            '}';
    }
}

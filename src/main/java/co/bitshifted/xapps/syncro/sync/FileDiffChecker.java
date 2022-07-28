/*
 *
 *  * Copyright (c) 2022-2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.xapps.syncro.sync;

import co.bitshifted.xapps.syncro.model.FileEntry;
import co.bitshifted.xapps.syncro.model.ReleaseEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileDiffChecker {

    private final Path baseDir;
    private final MessageDigest digest;
    private List<ReleaseEntry> updateList = new ArrayList<>();
    private List<FileEntry> deleteList = new ArrayList<>();
    private List<ReleaseEntry> newEntries = new ArrayList<>();

    public FileDiffChecker(Path baseDir) throws NoSuchAlgorithmException {
        this.baseDir = baseDir;
        this.digest = MessageDigest.getInstance("SHA-256");
    }


    public  void process(List<ReleaseEntry> releaseEntries) throws IOException {
        List<ReleaseEntry> copyList = new ArrayList<>(releaseEntries);
        Files.walk(baseDir).forEach(file -> {
            try {
                if(Files.isRegularFile(file)) {
                    byte[] bytes = digest.digest(Files.readAllBytes(file));
                    String hash = bytesToHex(bytes);
                    Optional<ReleaseEntry> entry = copyList.stream().filter(e -> e.getTarget().toString().equals(file.toString())).findFirst();
                    if(entry.isPresent()) {
                        if(!entry.get().getHash().equals(hash)) {
                            updateList.add(entry.get());
                        }
                        copyList.remove(entry.get());
                    } else {
                        deleteList.add(new FileEntry(hash, file));
                    }
                }

            } catch(IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
        newEntries.addAll(copyList);
    }

    public List<ReleaseEntry> getUpdateList() {
        return updateList;
    }

    public List<FileEntry> getDeleteList() {
        return deleteList;
    }

    public List<ReleaseEntry> getNewEntries() {
        return newEntries;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

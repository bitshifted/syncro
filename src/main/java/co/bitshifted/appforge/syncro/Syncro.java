/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.appforge.syncro;

import co.bitshifted.appforge.syncro.http.DownloadHandler;
import co.bitshifted.appforge.syncro.http.SyncroHttpClient;
import co.bitshifted.appforge.syncro.launch.LaunchArgs;
import co.bitshifted.appforge.syncro.launch.ReleaseProcessor;
import co.bitshifted.appforge.syncro.model.ReleaseEntry;
import co.bitshifted.appforge.syncro.model.UpdateCheckStatus;
import co.bitshifted.appforge.syncro.model.UpdateInfo;
import co.bitshifted.appforge.syncro.sync.FileDiffChecker;
import co.bitshifted.appforge.syncro.ui.UpdateWindow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vladimir Djurovic
 */
public class Syncro {

	public static void main(String... args) throws Exception {

		UpdateWindow window = new UpdateWindow();
		window.init();
		window.pack();
		window.setVisible(true);

	}
}

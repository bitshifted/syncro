/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.xapps.syncro.model;

import java.nio.file.Path;

/**
 * @author Vladimir Djurovic
 */
public class DownloadResult {
	public enum Result{SUCCESS, FAILURE};

	private final Result result;
	private final Path output;

	public DownloadResult(Result result, Path path) {
		this.result = result;
		this.output = path;
	}

	public Result getResult() {
		return result;
	}

	public Path getOutput() {
		return output;
	}
}

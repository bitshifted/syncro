/*
 * Copyright (c) 2020. Bitshift (http://bitshifted.co)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package co.bitshifted.appforge.syncro.model;

/**
 * @author Vladimir Djurovic
 */
public class UpdateInfo {
	private final UpdateCheckStatus status;
	private  String content;

	public UpdateInfo(UpdateCheckStatus status, String content) {
		this.status = status;
		this.content = content;
	}

	public UpdateInfo(UpdateCheckStatus status) {
		this.status = status;
	}

	public UpdateCheckStatus getStatus() {
		return status;
	}

	public String getContent() {
		return content;
	}

}

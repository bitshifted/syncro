/*
 *
 *  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
 *  *
 *  * This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package co.bitshifted.appforge.syncro.ui;

import javax.swing.*;

public class UpdateWorker extends SwingWorker<Integer, String> {

    private final JProgressBar progressBar;

    public UpdateWorker(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        System.out.println("Running in background");
        return null;
    }

    @Override
    protected void done() {
        super.done();
    }
}

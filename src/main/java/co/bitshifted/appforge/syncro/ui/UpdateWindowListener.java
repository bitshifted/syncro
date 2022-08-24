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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UpdateWindowListener extends WindowAdapter {

    private final JProgressBar progressBar;

    public UpdateWindowListener(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        new UpdateWorker(progressBar).execute();
    }
}

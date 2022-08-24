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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class UpdateWindow extends JFrame {

    private final JPanel content;
    private final JProgressBar progressBar;

    public UpdateWindow() {
        super("UpdateWindow");
        setUndecorated(true);
        this.content = new JPanel();
        content.setLayout(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        setLocationRelativeTo(null);

        addWindowListener(new UpdateWindowListener(progressBar));
    }

    public void init() throws IOException {
        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if(splashScreen != null) {
            BufferedImage img = ImageIO.read(splashScreen.getImageURL());
            ImageIcon icon = new ImageIcon(img);
            content.add(new JLabel(icon), BorderLayout.CENTER);
            content.add(progressBar, BorderLayout.SOUTH);
            setBounds(splashScreen.getBounds());
        } else {
            content.add(new JLabel("Updating..."), BorderLayout.CENTER);
            content.add(progressBar, BorderLayout.SOUTH);
        }
        add(content);
    }
}

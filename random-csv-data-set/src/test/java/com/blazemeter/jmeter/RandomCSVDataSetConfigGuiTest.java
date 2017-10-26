package com.blazemeter.jmeter;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;


public class RandomCSVDataSetConfigGuiTest {

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }


    @Test
    public void showGui() throws Exception {
        if (!GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance()) {
            RandomCSVDataSetConfigGui gui = new RandomCSVDataSetConfigGui();
            JDialog frame = new JDialog();
            frame.add(gui);

            frame.setPreferredSize(new Dimension(800, 600));
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            while (frame.isVisible()) {
                Thread.sleep(100);
            }
        }
    }
}
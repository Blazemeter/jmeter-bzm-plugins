package com.blazemeter.jmeter;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.assertEquals;


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

    @Test
    public void testGui() throws Exception {
        RandomCSVDataSetConfigGui gui = new RandomCSVDataSetConfigGui();

        RandomCSVDataSetConfig element1 = (RandomCSVDataSetConfig) gui.createTestElement();
        RandomCSVDataSetConfig element2 = (RandomCSVDataSetConfig) gui.createTestElement();

        element1.setFilename("filename");
        element1.setFileEncoding("fileEncoding");
        element1.setDelimiter("delimiter");
        element1.setVariableNames("vars");

        element1.setIgnoreFirstLine(true);
        element1.setRandomOrder(true);
        element1.setRewindOnTheEndOfList(true);
        element1.setIndependentListPerThread(true);

        gui.configure(element1);
        gui.modifyTestElement(element2);

        assertEquals(element1.getFilename(), element2.getFilename());
        assertEquals(element1.getFileEncoding(), element2.getFileEncoding());
        assertEquals(element1.getDelimiter(), element2.getDelimiter());
        assertEquals(element1.getVariableNames(), element2.getVariableNames());

        assertEquals(element1.isRandomOrder(), element2.isRandomOrder());
        assertEquals(element1.isIndependentListPerThread(), element2.isIndependentListPerThread());
        assertEquals(element1.isIgnoreFirstLine(), element2.isIgnoreFirstLine());
        assertEquals(element1.isRewindOnTheEndOfList(), element2.isRewindOnTheEndOfList());

        gui.clearGui();
        gui.modifyTestElement(element2);

        assertEquals("", element2.getFilename());
        assertEquals("UTF-8", element2.getFileEncoding());
        assertEquals(",", element2.getDelimiter());
        assertEquals("", element2.getVariableNames());
        assertEquals(true, element2.isRandomOrder());
        assertEquals(false, element2.isIgnoreFirstLine());
        assertEquals(false, element2.isIndependentListPerThread());
        assertEquals(true, element2.isRewindOnTheEndOfList());
    }
}
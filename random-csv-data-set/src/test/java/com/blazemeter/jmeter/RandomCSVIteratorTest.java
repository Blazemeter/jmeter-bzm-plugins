package com.blazemeter.jmeter;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class RandomCSVIteratorTest {


    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void testFlow() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();
        RandomCSVIterator iterator = new RandomCSVIterator(path, "UTF-8", ",", "", true, true, true, true);

        System.out.println();

    }
}
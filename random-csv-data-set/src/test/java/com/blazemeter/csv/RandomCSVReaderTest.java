package com.blazemeter.csv;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.*;

public class RandomCSVReaderTest {

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void testRead() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(new File(path), true);

        System.out.println("==========");
        System.out.println(reader.readLine(3));
        System.out.println(reader.readLine(2));
        System.out.println(reader.readLine(1));
        System.out.println(reader.readLine(0));

    }
}
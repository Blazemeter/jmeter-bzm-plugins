package com.blazemeter.csv;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class RandomCSVReaderTest {

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void testRead() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();
//        String path = "/home/artem/home/res.csv";

        long st = System.currentTimeMillis();
        RandomCSVReader reader = new RandomCSVReader(new File(path), "UTF-8", ',', true);
        System.out.println("Init finished: " + (System.currentTimeMillis() - st));

        System.out.println("==========");
        System.out.println(Arrays.toString(reader.getNextRecord()));
        System.out.println(Arrays.toString(reader.getNextRecord()));
        System.out.println(Arrays.toString(reader.getNextRecord()));
        System.out.println(Arrays.toString(reader.getNextRecord()));

    }
}
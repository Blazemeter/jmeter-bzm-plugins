package com.blazemeter.jmeter;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class RandomCSVDataSetConfigTest {

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }


    @Test
    public void testProperties() throws Exception {
        RandomCSVDataSetConfig randomCSV = new RandomCSVDataSetConfig();

        String testString = "testString";

        randomCSV.setFilename(testString);
        assertEquals(testString, randomCSV.getFilename());

        randomCSV.setFileEncoding(testString);
        assertEquals(testString, randomCSV.getFileEncoding());

        randomCSV.setDelimiter(testString);
        assertEquals(testString, randomCSV.getDelimiter());

        randomCSV.setVariableNames(testString);
        assertEquals(testString, randomCSV.getVariableNames());

        randomCSV.setRandomOrder(true);
        assertTrue(randomCSV.isRandomOrder());
        randomCSV.setRandomOrder(false);
        assertFalse(randomCSV.isRandomOrder());

        randomCSV.setIgnoreFirstLine(true);
        assertTrue(randomCSV.isIgnoreFirstLine());
        randomCSV.setIgnoreFirstLine(false);
        assertFalse(randomCSV.isIgnoreFirstLine());

        randomCSV.setRewindOnTheEndOfList(true);
        assertTrue(randomCSV.isRewindOnTheEndOfList());
        randomCSV.setRewindOnTheEndOfList(false);
        assertFalse(randomCSV.isRewindOnTheEndOfList());

        randomCSV.setIndependentListPerThread(true);
        assertTrue(randomCSV.isIndependentListPerThread());
        randomCSV.setIndependentListPerThread(false);
        assertFalse(randomCSV.isIndependentListPerThread());
    }

    // User set variables count less than columns count in file
    @Test
    public void testPutVariables1() throws Exception {
        String path = this.getClass().getResource("/SpaceDelimiter.csv").getPath();

        JMeterVariables jMeterVariables = new JMeterVariables();
        JMeterContextService.getContext().setVariables(jMeterVariables);

        RandomCSVDataSetConfig config = new RandomCSVDataSetConfig();

        config.setFilename(path);
        config.setFileEncoding("UTF-8");
        config.setDelimiter(" ");
        config.setVariableNames("column1,column2");
        config.setRandomOrder(false);
        config.setRewindOnTheEndOfList(false);
        config.setIndependentListPerThread(false);
        config.setIgnoreFirstLine(true);

        config.testStarted();

        config.iterationStart(null);
        assertEquals(2, jMeterVariables.entrySet().size());
        assertEquals("1", jMeterVariables.get("column1"));
        assertEquals("2", jMeterVariables.get("column2"));
    }

    // User set variables count more than columns count in file
    @Test
    public void testPutVariables2() throws Exception {
        String path = this.getClass().getResource("/SpaceDelimiter.csv").getPath();

        JMeterVariables jMeterVariables = new JMeterVariables();
        JMeterContextService.getContext().setVariables(jMeterVariables);

        RandomCSVDataSetConfig config = new RandomCSVDataSetConfig();

        config.setFilename(path);
        config.setFileEncoding("UTF-8");
        config.setDelimiter(" ");
        config.setVariableNames("column1,column2,column3,column4");
        config.setRandomOrder(false);
        config.setRewindOnTheEndOfList(false);
        config.setIndependentListPerThread(false);
        config.setIgnoreFirstLine(true);

        config.testStarted();

        config.iterationStart(null);
        assertEquals(3, jMeterVariables.entrySet().size());
        assertEquals("1", jMeterVariables.get("column1"));
        assertEquals("2", jMeterVariables.get("column2"));
        assertEquals("3", jMeterVariables.get("column3"));
    }

    // User set the same variables count like in file columns count
    @Test
    public void testPutVariables3() throws Exception {
        String path = this.getClass().getResource("/SpaceDelimiter.csv").getPath();

        JMeterVariables jMeterVariables = new JMeterVariables();
        JMeterContextService.getContext().setVariables(jMeterVariables);

        RandomCSVDataSetConfig config = new RandomCSVDataSetConfig();

        config.setFilename(path);
        config.setFileEncoding("UTF-8");
        config.setDelimiter(" ");
        config.setVariableNames("column1,column2,column3,column4");
        config.setRandomOrder(false);
        config.setRewindOnTheEndOfList(false);
        config.setIndependentListPerThread(false);
        config.setIgnoreFirstLine(true);

        config.testStarted();

        config.iterationStart(null);
        assertEquals(3, jMeterVariables.entrySet().size());
        assertEquals("1", jMeterVariables.get("column1"));
        assertEquals("2", jMeterVariables.get("column2"));
        assertEquals("3", jMeterVariables.get("column3"));
    }
}
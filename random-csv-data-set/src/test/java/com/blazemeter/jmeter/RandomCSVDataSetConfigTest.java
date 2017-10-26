package com.blazemeter.jmeter;

import org.junit.Test;

import static org.junit.Assert.*;

public class RandomCSVDataSetConfigTest {

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

        randomCSV.setAllowQuotedData(true);
        assertTrue(randomCSV.isAllowQuotedData());
        randomCSV.setAllowQuotedData(false);
        assertFalse(randomCSV.isAllowQuotedData());

        randomCSV.setRewindOnTheEndOfList(true);
        assertTrue(randomCSV.isRewindOnTheEndOfList());
        randomCSV.setRewindOnTheEndOfList(false);
        assertFalse(randomCSV.isRewindOnTheEndOfList());

        randomCSV.setIndependentListPerThread(true);
        assertTrue(randomCSV.isIndependentListPerThread());
        randomCSV.setIndependentListPerThread(false);
        assertFalse(randomCSV.isIndependentListPerThread());
    }
}
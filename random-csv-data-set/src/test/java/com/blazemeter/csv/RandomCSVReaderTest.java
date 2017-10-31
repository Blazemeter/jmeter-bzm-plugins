package com.blazemeter.csv;

import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class RandomCSVReaderTest {

    @BeforeClass
    public static void setUpClass()
            throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void testRandomReadWithHeaderAndWithoutRepeat() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ",", true, true, false, false);

        assertEquals("[timeStamp, elapsed, label, responseCode, responseMessage, threadName, dataType, success, bytes]",
                Arrays.toString(reader.getHeader()));

        String timeStamps = "1393227741256,1393227741257,1393227741258";

        assertTrue(reader.hasNextRecord());
        assertTrue(timeStamps.contains(reader.getNextRecord()[0]));

        assertTrue(reader.hasNextRecord());
        assertTrue(timeStamps.contains(reader.getNextRecord()[0]));

        assertTrue(reader.hasNextRecord());
        assertTrue(timeStamps.contains(reader.getNextRecord()[0]));

        assertFalse(reader.hasNextRecord());
    }

    @Test
    public void testReadWithHeaderAndRepeat() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ",", false, false, true, true);

        assertEquals("[timeStamp, elapsed, label, responseCode, responseMessage, threadName, dataType, success, bytes]",
                Arrays.toString(reader.getHeader()));

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741256", reader.getNextRecord()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741257", reader.getNextRecord()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741258", reader.getNextRecord()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741256", reader.getNextRecord()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741257", reader.getNextRecord()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741258", reader.getNextRecord()[0]);
    }

    @Test
    public void testRecordsCount() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();

        // test random
        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ",", true, false, false, false);
        assertEquals(3, getRecordsCount(reader, 10));
        reader = new RandomCSVReader(path, "UTF-8", ",", true, true, true, false);
        assertEquals(3, getRecordsCount(reader, 10));
        reader = new RandomCSVReader(path, "UTF-8", ",", true, false, true, false);
        assertEquals(3, getRecordsCount(reader, 10));
        reader = new RandomCSVReader(path, "UTF-8", ",", true, true, false, false);
        assertEquals(4, getRecordsCount(reader, 10));


        // test consistent
        reader = new RandomCSVReader(path, "UTF-8", ",", false, false, false, false);
        assertEquals(3, getRecordsCount(reader, 3));
        reader = new RandomCSVReader(path, "UTF-8", ",", false, true, true, false);
        assertEquals(3, getRecordsCount(reader, 3));
        reader = new RandomCSVReader(path, "UTF-8", ",", false, false, true, false);
        assertEquals(3, getRecordsCount(reader, 3));
        reader = new RandomCSVReader(path, "UTF-8", ",", false, true, false, false);
        assertEquals(4, getRecordsCount(reader, 4));
    }

    private int getRecordsCount(RandomCSVReader reader, int maxRecordsCount) {
        int i = 0;
        while (reader.hasNextRecord()) {
            i++;
            reader.getNextRecord();
            if (i > maxRecordsCount) {
                throw new AssertionError("File contains no more than " + maxRecordsCount);
            }
        }
        return i;
    }

    @Test
    public void testTabDelimiter() throws Exception {
        String path = this.getClass().getResource("/TabDelimiter.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", "\t", false, false, false, false);
        assertEquals("Expected 16 columns in csv", 16, reader.getHeader().length);
        assertEquals("elapsed", reader.getHeader()[1]);
        assertEquals(10, getRecordsCount(reader, 10));
    }

    @Test
    public void testSpaceDelimiter() throws Exception {
        String path = this.getClass().getResource("/SpaceDelimiter.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", " ", false, false, false, false);
        assertEquals("Expected 3 columns in csv", 3, reader.getHeader().length);
        assertEquals("second", reader.getHeader()[1]);
        assertEquals(3, getRecordsCount(reader, 3));
    }

}
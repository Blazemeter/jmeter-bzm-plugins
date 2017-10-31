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

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ",", false, true, true, false);

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
}
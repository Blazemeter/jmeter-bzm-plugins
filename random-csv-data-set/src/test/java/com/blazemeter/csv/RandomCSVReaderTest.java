package com.blazemeter.csv;

import com.mchange.util.AssertException;
import kg.apc.emulators.TestJMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ",", true, false, false, false);

        assertEquals("[timeStamp, elapsed, label, responseCode, responseMessage, threadName, dataType, success, bytes]",
                Arrays.toString(reader.getHeader()));

        String timeStamps = "1393227741256,1393227741257,1393227741258";
        long lineAddr;
        assertTrue(reader.hasNextRecord());
        lineAddr = reader.getNextLineAddr();
        assertTrue(timeStamps.contains(reader.readLineWithSeek(lineAddr)[0]));

        assertTrue(reader.hasNextRecord());
        lineAddr = reader.getNextLineAddr();
        assertTrue(timeStamps.contains(reader.readLineWithSeek(lineAddr)[0]));

        assertTrue(reader.hasNextRecord());
        lineAddr = reader.getNextLineAddr();
        assertTrue(timeStamps.contains(reader.readLineWithSeek(lineAddr)[0]));

        assertFalse(reader.hasNextRecord());
    }

    @Test
    public void testReadWithHeaderAndRepeat() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ",", false, false, true, true);

        assertEquals("[timeStamp, elapsed, label, responseCode, responseMessage, threadName, dataType, success, bytes]",
                Arrays.toString(reader.getHeader()));

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741256", reader.readNextLine()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741257", reader.readNextLine()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741258", reader.readNextLine()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741256", reader.readNextLine()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741257", reader.readNextLine()[0]);

        assertTrue(reader.hasNextRecord());
        assertEquals("1393227741258", reader.readNextLine()[0]);
    }

    @Test
    public void testMultiBytes() throws Exception {
        String path = this.getClass().getResource("/text.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ";", true, false, false, false);

        assertEquals("[firstname, lastname, street, city]",
                Arrays.toString(reader.getHeader()));

        String[] record;

        for (int i = 0; i < 4; i++) {
            assertTrue(reader.hasNextRecord());
            long addr = reader.getNextLineAddr();
            record = reader.readLineWithSeek(addr);
            assertRecord(record);
        }

        assertFalse(reader.hasNextRecord());
    }

    private void assertRecord(String[] record) {
        switch (record[0]) {
            case "Hänsel" :
                assertEquals("Mustermann", record[1]);
                assertEquals("Einbahnstraße", record[2]);
                assertEquals("Hamburg", record[3]);
                break;
            case "André" :
                assertEquals("Lecompte", record[1]);
                assertEquals("Rue du marché", record[2]);
                assertEquals("Moÿ-de-l'Aisne", record[3]);
                break;
            case "Ἀλέξανδρος" :
                assertEquals("Павлов", record[1]);
                assertEquals("Большая Пироговская улица", record[2]);
                assertEquals("Москва́", record[3]);
                break;
            case "בנימין" : // idea shows incorrect this line. firstname is real Benjamin -> 'בנימין'
                assertEquals("يعقوب", record[1]);
                assertEquals("Street", record[2]);
                assertEquals("Megapolis", record[3]);
                break;
            default:
                throw new AssertException("No such firstname in csv file " + record[0]);
        }
    }

    @Test
    public void testRecordsCount() throws Exception {
        String path = this.getClass().getResource("/JMeterCsvResults.csv").getPath();

        // test random
        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", ",", true, false, false, false);
        assertEquals(3, getRecordsCount(reader, 10, true));
        reader = new RandomCSVReader(path, "UTF-8", ",", true, true, true, false);
        assertEquals(3, getRecordsCount(reader, 10, true));
        reader = new RandomCSVReader(path, "UTF-8", ",", true, false, true, false);
        assertEquals(3, getRecordsCount(reader, 10, true));
        reader = new RandomCSVReader(path, "UTF-8", ",", true, true, false, false);
        assertEquals(4, getRecordsCount(reader, 10, true));


        // test consistent
        reader = new RandomCSVReader(path, "UTF-8", ",", false, false, false, false);
        assertEquals(3, getRecordsCount(reader, 3, false));
        reader = new RandomCSVReader(path, "UTF-8", ",", false, true, true, false);
        assertEquals(3, getRecordsCount(reader, 3, false));
        reader = new RandomCSVReader(path, "UTF-8", ",", false, false, true, false);
        assertEquals(3, getRecordsCount(reader, 3, false));
        reader = new RandomCSVReader(path, "UTF-8", ",", false, true, false, false);
        assertEquals(4, getRecordsCount(reader, 4, false));
    }

    private int getRecordsCount(RandomCSVReader reader, int maxRecordsCount, boolean isRandom) {
        int i = 0;
        while (reader.hasNextRecord()) {
            i++;
            if (isRandom) {
                reader.readLineWithSeek(reader.getNextLineAddr());
            } else {
                reader.readNextLine();
            }
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
        assertEquals(10, getRecordsCount(reader, 10, false));
    }

    @Test
    public void testSpaceDelimiter() throws Exception {
        String path = this.getClass().getResource("/SpaceDelimiter.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", " ", false, false, false, false);
        assertEquals("Expected 3 columns in csv", 3, reader.getHeader().length);
        assertEquals("second", reader.getHeader()[1]);
        assertEquals(3, getRecordsCount(reader, 3, false));
    }


    @Test
    public void testEmptyLastLine() throws Exception {
        String path = this.getClass().getResource("/EmptyLastLine.csv").getPath();

        RandomCSVReader reader = new RandomCSVReader(path, "UTF-8", " ", true, true, true, false);

        List<String> results = new ArrayList<>();
        results.add(Arrays.toString(new String[] {"1","2","3"}));
        results.add(Arrays.toString(new String[] {"4","5","6"}));
        results.add(Arrays.toString(new String[] {"7","8","9"}));

        long addr;
        assertTrue(reader.hasNextRecord());
        addr = reader.getNextLineAddr();
        String record = Arrays.toString(reader.readLineWithSeek(addr));
        assertTrue(results.contains(record));
        results.remove(record);

        assertTrue(reader.hasNextRecord());
        addr = reader.getNextLineAddr();
        record = Arrays.toString(reader.readLineWithSeek(addr));
        assertTrue(results.contains(record));
        results.remove(record);

        assertTrue(reader.hasNextRecord());
        addr = reader.getNextLineAddr();
        record = Arrays.toString(reader.readLineWithSeek(addr));
        assertTrue(results.contains(record));
        results.remove(record);

        assertFalse(reader.hasNextRecord());
        assertEquals(0, results.size());
    }
}
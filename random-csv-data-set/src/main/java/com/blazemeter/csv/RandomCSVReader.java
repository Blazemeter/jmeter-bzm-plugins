package com.blazemeter.csv;

import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;

public class RandomCSVReader {
    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    private File file;
    private String encoding;
    private char delim;
    private boolean randomOrder;
    private boolean isRewindOnEndOfList;

    private ArrayList<Integer> offsets;
    private int curPos = 0;
    private RandomBufferedReader rbr;
    private Random random;

    private BufferedReader consistentReader;
    private String[] header;

    private boolean isSkipFirstLine;


    public RandomCSVReader(String filename, String encoding,
                           String delim, boolean randomOrder,
                           boolean hasVariableNames, boolean firstLineIsHeader,
                           boolean isRewindOnEndOfList) {
        File f = new File(filename);
        System.out.println((f.isAbsolute() || f.exists()));
        this.file = (f.isAbsolute() || f.exists()) ? f : new File(FileServer.getFileServer().getBaseDir(), filename);
        this.encoding = encoding;
        this.delim = checkDelimiter(delim).charAt(0);
        this.isSkipFirstLine = !(!firstLineIsHeader && hasVariableNames);
        this.randomOrder = randomOrder;
        this.isRewindOnEndOfList = isRewindOnEndOfList;
        try {
            initOffsets();
            if (randomOrder) {
                rbr = new RandomBufferedReader(createReader(), new RandomAccessFile(file, "r"));
                initRandom();
            } else {
                initConsistentReader();
            }
            initHeader();
        } catch (IOException ex) {
            LOGGER.error("Cannot initialize RandomCSVReader, because of error: ", ex);
            throw new RuntimeException("Cannot initialize RandomCSVReader, because of error: " + ex.getMessage(), ex);
        }
    }

    private String checkDelimiter(String delim) {
        if ("\\t".equals(delim)) {
            return "\t";
        } else if (delim.isEmpty()){
            LOGGER.debug("Empty delimiter, will use ','");
            return ",";
        }
        return delim;
    }

    private void initHeader() {
        try (BufferedReader reader = new BufferedReader(createReader())) {
            header = CSVSaveService.csvReadFile(reader, delim);
        } catch (IOException ex) {
            LOGGER.error("Cannot read CSV header ", ex);
            throw new RuntimeException("Cannot read CSV header: " + ex.getMessage(), ex);
        }
    }

    private void initConsistentReader() throws IOException {
        consistentReader = new BufferedReader(createReader());
        if (isSkipFirstLine && !this.offsets.isEmpty()) {
            consistentReader.skip(this.offsets.get(0)); //TODO: any other ideas how skip header?
        }
    }

    private void closeConsistentReader() throws IOException {
        consistentReader.close();
    }

    public String[] getNextRecord() {
        try {
            if (randomOrder) {
                int pos = getRandomPos();
                swap(curPos + pos);
                return readCurrentLineWithSeek();
            } else {
                return readCurrentLine();
            }
        } catch (IOException ex) {
            LOGGER.error("Cannot get next record from csv file: ", ex);
            throw new RuntimeException("Cannot get next record from csv file: " + ex.getMessage(), ex);
        }
    }

    public boolean hasNextRecord() {
        if (!(curPos < offsets.size()) && isRewindOnEndOfList) {
            reInitialize();
            LOGGER.debug("Reset cursor position");
            curPos = 0;
            return true;
        } else if (curPos < offsets.size()) {
            return true;
        }
        return false;
    }

    private void reInitialize() {
        if (randomOrder) {
            initRandom();
        } else {
            try {
                closeConsistentReader();
                initConsistentReader();
            } catch (IOException ex) {
                LOGGER.error("Cannot reInitialize consistent reader ", ex);
                throw new RuntimeException("Cannot reInitialize consistent reader: " + ex.getMessage(), ex);
            }
        }
    }

    public String[] getHeader() {
        return header;
    }

    private String[] readCurrentLine() throws IOException {
        curPos++;
        return CSVSaveService.csvReadFile(consistentReader, delim);
    }

    private void initRandom() {
        this.random = new Random(System.currentTimeMillis());
    }

    private void initOffsets() throws IOException {
        offsets = new ArrayList<>();
        if (!isSkipFirstLine) {
            offsets.add(0);
        }
        LOGGER.info("Start reading the file: " + file.getAbsolutePath());
        try (BufferedReaderExt reader = new BufferedReaderExt(createReader())) {
            long fileSize = file.length();
            while (reader.getPos() <= fileSize) {
                CSVSaveService.csvReadFile(reader, delim);
                if (reader.getPos() <= fileSize) {
                    offsets.add(reader.getPos());
                }
            }
        }
        LOGGER.info("Reading finished. Found " + offsets.size() + " records in your csv file");
    }

    private void swap(int pos) {
        Integer tmp = offsets.get(curPos);
        offsets.set(curPos, offsets.get(pos));
        offsets.set(pos, tmp);
    }

    private String[] readCurrentLineWithSeek() throws IOException {
        long lineAddr = offsets.get(curPos);
        curPos++;
        rbr.seek(lineAddr);
        return CSVSaveService.csvReadFile(rbr, delim);
    }

    private int getRandomPos() {
        return random.nextInt(offsets.size() - curPos);
    }

    private Reader createReader() throws IOException {
        return new InputStreamReader(new FileInputStream(file), encoding);
    }
}

package com.blazemeter.csv;

import org.apache.jmeter.save.CSVSaveService;

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

    private File file;
    private String encoding;
    private char delim;
    private boolean randomOrder;
    private boolean firstLineIsHeader;
    private boolean isRewindOnEndOfList;

    private ArrayList<Integer> offsets;
    private int curPos = 0;
    private RandomBufferedReader rbr;
    private Random random;

    private BufferedReader consistentReader;

    public RandomCSVReader(File file, String encoding, char delim,
                           boolean randomOrder, boolean firstLineIsHeader,
                           boolean isRewindOnEndOfList) throws IOException {
        this.file = file;
        this.encoding = encoding;
        this.delim = delim;
        this.randomOrder = randomOrder;
        this.firstLineIsHeader = firstLineIsHeader;
        this.isRewindOnEndOfList = isRewindOnEndOfList;
        initOffsets();
        if (randomOrder) {
            rbr = new RandomBufferedReader(createReader(), new RandomAccessFile(file, "r"));
            initRandom();
        } else {
            initConsistentReader();
        }
    }

    private void initConsistentReader() throws IOException {
        consistentReader = new BufferedReader(createReader());
        if (firstLineIsHeader && !this.offsets.isEmpty()) {
            consistentReader.skip(this.offsets.get(0)); //TODO: any other ideas how skip header?
        }
    }

    public String[] getNextRecord() throws IOException {
        if (randomOrder) {
            int pos = getRandomPos();
            swap(curPos + pos);
            return readCurrentLineWithSeek();
        } else {
            return readCurrentLine();
        }
    }

    public boolean hasNextRecord() throws IOException {
        if (!(curPos < offsets.size()) && isRewindOnEndOfList) {
            reInitialize();
            curPos = 0;
            return true;
        } else if (curPos < offsets.size()) {
            return true;
        }
        return false;
    }

    private void reInitialize() throws IOException {
        if (randomOrder) {
            initRandom();
        } else {
            initConsistentReader();
        }
    }

    public String[] getHeader() throws IOException {
        if (firstLineIsHeader) {
            BufferedReader reader = new BufferedReader(createReader());
            return CSVSaveService.csvReadFile(reader, delim);
        }
        return null;
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
        if (!firstLineIsHeader) {
            offsets.add(0);
        }
        BufferedReaderExt reader = new BufferedReaderExt(createReader());
        long fileSize = file.length();
        while (reader.getPos() <= fileSize) {
            CSVSaveService.csvReadFile(reader, delim);
            if (reader.getPos() <= fileSize) {
                offsets.add(reader.getPos());
            }
        }
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

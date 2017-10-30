package com.blazemeter.csv;

import org.apache.jmeter.save.CSVSaveService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

    private ArrayList<Integer> offsets;
    private int curPos = 0;
    private RandomBufferedReader rbr;
    private Random random;

    public RandomCSVReader(File file, String encoding, char delim, boolean randomOrder) throws IOException {
        this.file = file;
        this.encoding = encoding;
        this.delim = delim;
        this.randomOrder = randomOrder;
        if (randomOrder) {
            rbr = new RandomBufferedReader(createReader(), new RandomAccessFile(file, "r"));
            offsets = new ArrayList<>();
            initOffsets();
            initRandom();
        }
    }

    private void initRandom() {
        this.random = new Random(System.currentTimeMillis());
    }

    private void initOffsets() throws IOException {
        BufferedReaderExt reader = new BufferedReaderExt(createReader());
        long fileSize = file.length();
        offsets.add(0);
        while (reader.getPos() <= fileSize) {
            CSVSaveService.csvReadFile(reader, delim);
            if (reader.getPos() <= fileSize) {
                offsets.add(reader.getPos());
            }
        }
    }

    public String[] getNextRecord() throws IOException {
        if (randomOrder) {
            int pos = getRandomPos();
            swap(curPos + pos);
            return readLine();
        } else {
            //TODO
            return null;
        }
    }

    private void swap(int pos) {
        Integer tmp = offsets.get(curPos);
        offsets.set(curPos, offsets.get(pos));
        offsets.set(pos, tmp);
    }

    //         TODO: how here read multi-line record?
    private String[] readLine() throws IOException {
        long lineAddr = offsets.get(curPos);
        curPos++;
        rbr.seek(lineAddr);
        return CSVSaveService.csvReadFile(rbr, delim);
    }


    public int getRandomPos() {
        int rand = random.nextInt(offsets.size() - curPos);
        System.out.println("Generate " + rand);
        return rand;
    }

    private Reader createReader() throws IOException {
        return new InputStreamReader(new FileInputStream(file), encoding);
    }
}

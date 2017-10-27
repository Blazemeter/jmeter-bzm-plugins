package com.blazemeter.csv;

import org.apache.jmeter.save.CSVSaveService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

public class RandomCSVReader {

    private File file;
    private boolean randomOrder;

    private ArrayList<Integer> offsets;
    private RandomAccessFile raf;


    public RandomCSVReader(File file, boolean randomOrder) throws IOException {
        this.file = file;
        this.randomOrder = randomOrder;
        if (randomOrder) {
            raf = new RandomAccessFile(file, "r");
            offsets = new ArrayList<>();
            initOffsets();
        }
    }

    // TODO: if no header add '0' offset
    // TODO: remove last position
    private void initOffsets() throws  IOException {
        BufferedReaderExt reader = new BufferedReaderExt(new FileReader(file));
        long fileSize = file.length();
        while (reader.getPos() <= fileSize) {
            String[] records = CSVSaveService.csvReadFile(reader, ',');
            offsets.add(reader.getPos());
            System.out.println(Arrays.toString(records));
            System.out.println(reader.getPos());
        }
    }


    public String readLine(int line) throws IOException{
        long lineAddr = offsets.get(line);
        raf.seek(lineAddr);
        // TODO: how here read multi-line record?
        return raf.readLine();
    }



}

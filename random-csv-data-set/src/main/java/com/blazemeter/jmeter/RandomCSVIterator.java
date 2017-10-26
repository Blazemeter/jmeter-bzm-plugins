package com.blazemeter.jmeter;


import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.services.FileServer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RandomCSVIterator implements Iterator<Map<String, String>> {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final String filename;
    private final String fileEncoding;
    private final String delimiter;
    private final boolean isRandomOrder;
    private final boolean isIgnoreFirstLine;
    private final boolean isAllowQuotedData;
    private final boolean isRewindOnEndOfList;

    private List<String> csv;
    private List<String> variables;

    public RandomCSVIterator(String filename,
                             String fileEncoding,
                             String delimiter,
                             String variableNames,
                             boolean isRandomOrder,
                             boolean isIgnoreFirstLine,
                             boolean isAllowQuotedData,
                             boolean isRewindOnEndOfList) {
        this.filename = filename;
        this.fileEncoding = fileEncoding;
        this.delimiter = delimiter;
        this.isRandomOrder = isRandomOrder;
        this.isIgnoreFirstLine = isIgnoreFirstLine;
        this.isAllowQuotedData = isAllowQuotedData;
        this.isRewindOnEndOfList = isRewindOnEndOfList;


        variables = initVariables(variableNames);
    }

    private List<String> initVariables(String variableNames) {
        if (StringUtils.isEmpty(variableNames)) {
            try {
                String header = readHeader();
                return Arrays.asList(CSVSaveService.csvSplitString(header, delimiter.charAt(0)));
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not split CSV header line from file:" + filename, e);
            }
        } else {
            return Arrays.asList(JOrphanUtils.split(variableNames, ","));
        }
    }

    private String readHeader() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            return (line != null) ? line : "";
        }
    }


    @Override
    public boolean hasNext() {
        return false;
    }



    @Override
    public Map<String, String> next() {
        return null;
    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException("Removing is not supported for this iterator");
    }
}

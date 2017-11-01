package com.blazemeter.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;

public class RandomBufferedReader extends BufferedReader {

    private final RandomAccessFile raf;
    private long markedPosition;

    public RandomBufferedReader(Reader in, RandomAccessFile raf) {
        super(in);
        this.raf = raf;
    }

    public void seek(long pos) throws IOException {
        this.raf.seek(pos);
    }

    @Override
    public int read() throws IOException {
        return raf.read();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        markedPosition = raf.getFilePointer();
    }

    @Override
    public void reset() throws IOException {
        raf.seek(markedPosition);
    }
}

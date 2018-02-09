package com.blazemeter.csv;

import java.io.*;
import java.nio.channels.Channels;

public class RandomBufferedReader extends BufferedReader {

    private final RandomAccessFile raf;

    private InputStream is;
    private BufferedReader reader;
    private String encoding;
    private long markedPosition;

    public RandomBufferedReader(Reader in, RandomAccessFile raf, String encoding) throws UnsupportedEncodingException {
        super(in);
        this.raf = raf;
        this.encoding = encoding;
        this.is = Channels.newInputStream(raf.getChannel());
        initBufferedReader();
    }

    private void initBufferedReader() throws UnsupportedEncodingException {
        InputStreamReader isr = new InputStreamReader(is, encoding);
        reader = new BufferedReader(isr);
    }

    public void seek(long pos) throws IOException {
        this.raf.seek(pos);
        initBufferedReader();
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        markedPosition = raf.getFilePointer();
    }

    @Override
    public void reset() throws IOException {
        raf.seek(markedPosition);
    }

    @Override
    public void close() throws IOException {
        super.close();
        is.close();
    }

}

package com.blazemeter.csv;

import java.io.*;

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
        for (;;) {
            int b = raf.read();
            // Single byte character starting with binary 0.
            if ((b & 0x80) == 0)
                return (char) b;
            // 2-byte character starting with binary 110.
            if ((b & 0xE0) == 0xC0)
                return (char) ((b & 0x1F) << 6 | raf.read() & 0x3F);
            // 3 and 4 byte encodings left as an exercise...
            // 2nd, 3rd, or 4th byte of a multibyte char starting with 10.
            // Back up and loop.
            if ((b & 0xC0) == 0xF0)
                raf.seek(raf.getFilePointer() - 2);
        }
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

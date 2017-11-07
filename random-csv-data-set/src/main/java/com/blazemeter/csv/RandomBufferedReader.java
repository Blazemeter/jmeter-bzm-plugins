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
        return readUtf8Char(raf);
    }

    public static int readUtf8Char(final DataInput dataInput) throws IOException {
        int char1, char2, char3;

        try {
            char1 = dataInput.readByte() & 0xff;
            switch (char1 >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
            /* 0xxxxxxx*/
                    return (char) char1;
                case 12:
                case 13:
            /* 110x xxxx   10xx xxxx*/
                    char2 = dataInput.readByte() & 0xff;
                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException("malformed input");
                    }
                    return (char) (((char1 & 0x1F) << 6) | (char2 & 0x3F));
                case 14:
            /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    char2 = dataInput.readByte() & 0xff;
                    char3 = dataInput.readByte() & 0xff;
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException("malformed input");
                    }
                    return (char) (((char1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                default:
            /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException("malformed input");
            }
        } catch (EOFException ex) {
            return -1;
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

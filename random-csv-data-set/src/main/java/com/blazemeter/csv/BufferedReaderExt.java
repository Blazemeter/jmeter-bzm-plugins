package com.blazemeter.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public class BufferedReaderExt extends BufferedReader {
    private int pos;

    public BufferedReaderExt(Reader in, int sz) {
        super(in, sz);
    }

    public BufferedReaderExt(Reader in) {
        super(in);
    }

    public int getPos() {
        return pos;
    }

    @Override
    public int read() throws IOException {
        int res = super.read();
        if (res <= Byte.MAX_VALUE) {
            pos++;
        } else {
            byte[] buf = new String(new char[]{(char) res}).getBytes();
            pos += buf.length;
        }
        return res;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int count = super.read(cbuf, off, len);
        pos += count;
        return count;
    }

    @Override
    public String readLine() throws IOException {
        String res = super.readLine();
        pos += res.length();
        return res;
    }

    @Override
    public long skip(long n) throws IOException {
        long res = super.skip(n);
        pos += res;
        return res;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        pos = 0;
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        int res = super.read(target);
        pos += res;
        return res;
    }



    @Override
    public int read(char[] cbuf) throws IOException {
        int res = super.read(cbuf);
        pos += res;
        return res;
    }
}

package com.blazemeter.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class BufferedReaderExt extends BufferedReader {

    private int pos;
    private Charset charset;


    public BufferedReaderExt(Reader in, String encoding) {
        super(in);
        charset = Charset.forName(encoding);
    }

    public int getPos() {
        return pos;
    }

    // JMeter used just this read() method.
    @Override
    public int read() throws IOException {
        int res = super.read();
        if (res <= Byte.MAX_VALUE) {
            pos++;
        } else {
            byte[] buf = new String(new char[]{(char) res}).getBytes(charset);
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

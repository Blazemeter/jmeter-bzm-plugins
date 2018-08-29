package com.blazemeter.csv;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    private enum ParserState {INITIAL, PLAIN, QUOTED, EMBEDDEDQUOTE}

    public static final char QUOTING_CHAR = '"';

    public static String[] csvReadFile(BufferedReader infile, char delim)
            throws IOException {
        int ch;
        ParserState state = ParserState.INITIAL;
        List<String> list = new ArrayList<>();
        CharArrayWriterExt baos = new CharArrayWriterExt(200);
        boolean push = false;
        while (-1 != (ch = infile.read())) {
            push = false;
            switch (state) {
                case INITIAL:
                    if (ch == QUOTING_CHAR) {
                        state = ParserState.QUOTED;
                    } else if (isDelimOrEOL(delim, ch)) {
                        push = true;
                    } else {
                        baos.write(ch);
                        state = ParserState.PLAIN;
                    }
                    break;
                case PLAIN:
                    if (isDelimOrEOL(delim, ch)) {
                        push = true;
                        state = ParserState.INITIAL;
                    } else {
                        baos.write(ch);
                    }
                    break;
                case QUOTED:
                    if (ch == QUOTING_CHAR) {
                        state = ParserState.EMBEDDEDQUOTE;
                    }
                    baos.write(ch);
                    break;
                case EMBEDDEDQUOTE:
                    if (ch == QUOTING_CHAR) {
                        baos.write(QUOTING_CHAR); // doubled quote => quote
                        state = ParserState.QUOTED;
                    } else if (isDelimOrEOL(delim, ch)) {
                        push = true;
                        baos.shiftLeft(1); // if current ch == delimiter -> previous char was QUOTING
                        state = ParserState.INITIAL;
                    } else {
                        baos.write(ch);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected state " + state);
            } // switch(state)
            if (push) {
                if (ch == '\r') {// Remove following \n if present
                    infile.mark(1);
                    if (infile.read() != '\n') {
                        infile.reset(); // did not find \n, put the character
                        // back
                    }
                }
                String s = baos.toString();
                list.add(s);
                baos.reset();
            }
            if ((ch == '\n' || ch == '\r') && state != ParserState.QUOTED) {
                break;
            }
        } // while not EOF
        if (ch == -1) {// EOF (or end of string) so collect any remaining data
            if (state == ParserState.QUOTED) {
                throw new IOException("Missing trailing quote-char in quoted field:[\""
                        + baos.toString() + "]");
            }
            // Do we have some data, or a trailing empty field?
            if (baos.size() > 0 // we have some data
                    || push // we've started a field
                    || state == ParserState.EMBEDDEDQUOTE // Just seen ""
                    ) {
                list.add(baos.toString());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    private static boolean isDelimOrEOL(char delim, int ch) {
        return ch == delim || ch == '\n' || ch == '\r';
    }

    protected static class CharArrayWriterExt extends CharArrayWriter {
        public CharArrayWriterExt(int initialSize) {
            super(initialSize);
        }

        public void shiftLeft(int pos) {
            if (pos < 0 || pos > count) {
                throw new IllegalArgumentException("Cannot shift left for: " + pos + " positions");
            }
            count -= pos;
        }
    }
}

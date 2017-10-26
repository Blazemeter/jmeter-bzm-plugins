package com.blazemeter.jmeter;


import java.util.Iterator;
import java.util.Map;

public class RandomCSVIterator implements Iterator<Map<String, String>> {

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

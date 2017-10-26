package com.blazemeter.jmeter;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.TestStateListener;

public class RandomCSVDataSetConfig extends ConfigTestElement implements NoThreadClone, LoopIterationListener, TestStateListener {

    public static final String FILENAME = "filename";
    public static final String FILE_ENCODING = "fileEncoding";
    public static final String DELIMITED = "delimiter";
    public static final String VARIABLE_NAMES = "variableNames";

    public static final String RANDOM_ORDER = "randomOrder";
    public static final String IGNORE_FIRST_LINE = "ignoreFirstLine";
    public static final String ALLOW_QUOTED_DATA = "allowQuotedData";
    public static final String REWIND_ON_THE_END = "rewindOnTheEndOfList";
    public static final String INDEPENDENT_LIST_PER_THREAD = "independentListPerThread";

    @Override
    public void iterationStart(LoopIterationEvent loopIterationEvent) {

    }

    @Override
    public void testStarted() {
        testStarted("*local*");
    }

    @Override
    public void testStarted(String s) {

    }

    @Override
    public void testEnded() {
        testEnded("*local*");
    }

    @Override
    public void testEnded(String s) {

    }


    public String getFilename() {
        return getPropertyAsString(FILENAME);
    }

    public void setFilename(String filename) {
        setProperty(FILENAME, filename);
    }

    public String getFileEncoding() {
        return getPropertyAsString(FILE_ENCODING);
    }

    public void setFileEncoding(String fileEncoding) {
        setProperty(FILE_ENCODING, fileEncoding);
    }

    public String getDelimiter() {
        return getPropertyAsString(DELIMITED);
    }

    public void setDelimiter(String delimiter) {
        setProperty(DELIMITED, delimiter);
    }

    public String getVariableNames() {
        return getPropertyAsString(VARIABLE_NAMES);
    }

    public void setVariableNames(String variableNames) {
        setProperty(VARIABLE_NAMES, variableNames);
    }

    public boolean isRandomOrder() {
        return getPropertyAsBoolean(RANDOM_ORDER);
    }

    public void setRandomOrder(boolean randomOrder) {
        setProperty(RANDOM_ORDER, randomOrder);
    }

    public boolean isIgnoreFirstLine() {
        return getPropertyAsBoolean(IGNORE_FIRST_LINE);
    }

    public void setIgnoreFirstLine(boolean ignoreFirstLine) {
        setProperty(IGNORE_FIRST_LINE, ignoreFirstLine);
    }

    public boolean isAllowQuotedData() {
        return getPropertyAsBoolean(ALLOW_QUOTED_DATA);
    }

    public void setAllowQuotedData(boolean allowQuotedData) {
        setProperty(ALLOW_QUOTED_DATA, allowQuotedData);
    }

    public boolean isRewindOnTheEndOfList() {
        return getPropertyAsBoolean(REWIND_ON_THE_END);
    }

    public void setRewindOnTheEndOfList(boolean rewindOnTheEndOfList) {
        setProperty(REWIND_ON_THE_END, rewindOnTheEndOfList);
    }

    public boolean isIndependentListPerThread() {
        return getPropertyAsBoolean(INDEPENDENT_LIST_PER_THREAD);
    }

    public void setIndependentListPerThread(boolean independentListPerThread) {
        setProperty(INDEPENDENT_LIST_PER_THREAD, independentListPerThread);
    }
}

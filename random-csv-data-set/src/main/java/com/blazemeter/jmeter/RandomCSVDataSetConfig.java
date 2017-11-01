package com.blazemeter.jmeter;

import com.blazemeter.csv.RandomCSVReader;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.jorphan.util.JOrphanUtils;

public class RandomCSVDataSetConfig extends ConfigTestElement implements NoThreadClone, LoopIterationListener, TestStateListener {

    public static final String FILENAME = "filename";
    public static final String FILE_ENCODING = "fileEncoding";
    public static final String DELIMITED = "delimiter";
    public static final String VARIABLE_NAMES = "variableNames";

    public static final String RANDOM_ORDER = "randomOrder";
    public static final String IGNORE_FIRST_LINE = "ignoreFirstLine";
    public static final String REWIND_ON_THE_END = "rewindOnTheEndOfList";
    public static final String INDEPENDENT_LIST_PER_THREAD = "independentListPerThread";

    private final ThreadLocal<RandomCSVReader> threadLocalRandomCSVReader = new ThreadLocal<RandomCSVReader>() {
        @Override
        protected RandomCSVReader initialValue() {
            return createRandomCSVReader();
        }
    };

    private RandomCSVReader randomCSVReader;

    @Override
    public void iterationStart(LoopIterationEvent loopIterationEvent) {
        boolean isIndependentListPerThread = isIndependentListPerThread();

        if (!isIndependentListPerThread && randomCSVReader == null) {
            throw new JMeterStopThreadException("All records in the CSV file have been passed.");
        }

        if (getReader().hasNextRecord()) {
            JMeterVariables variables = JMeterContextService.getContext().getVariables();
            putVariables(variables, getDestinationVariableKeys(), getReader().getNextRecord());
        } else {
            // TODO: interrupt iteration
            randomCSVReader = null;
            throw new JMeterStopThreadException("All records in the CSV file have been passed.");
        }
    }

    public String[] getDestinationVariableKeys() {
        String vars = getVariableNames();
        return hasVariablesNames() ?
                JOrphanUtils.split(vars, ",") :
                getReader().getHeader();
    }

    private void putVariables(JMeterVariables variables, String[] keys, String[] values) {
        int minLen = (keys.length > values.length) ? values.length : keys.length;
        for (int i = 0; i < minLen; i++) {
            variables.put(keys[i], values[i]);
        }
    }

    private RandomCSVReader getReader() {
        return isIndependentListPerThread() ? threadLocalRandomCSVReader.get() : randomCSVReader;
    }

    private RandomCSVReader createRandomCSVReader() {
        return new RandomCSVReader(
                getFilename(),
                getFileEncoding(),
                getDelimiter(),
                isRandomOrder(),
                hasVariablesNames(),
                isIgnoreFirstLine(),
                isRewindOnTheEndOfList()
        );
    }

    private boolean hasVariablesNames() {
        String vars = getVariableNames();
        return (vars != null && !vars.isEmpty());
    }

    @Override
    public void testStarted() {
        testStarted("*local*");
    }

    @Override
    public void testStarted(String s) {
        randomCSVReader = createRandomCSVReader();
    }

    @Override
    public void testEnded() {
        testEnded("*local*");
    }

    @Override
    public void testEnded(String s) {
        randomCSVReader = null;
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

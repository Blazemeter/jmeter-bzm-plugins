package com.blazemeter.jmeter;

import com.blazemeter.csv.RandomCSVReader;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomCSVDataSetConfig extends ConfigTestElement implements NoThreadClone, LoopIterationListener, TestStateListener, ThreadListener {

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    public static final String FILENAME = "filename";
    public static final String FILE_ENCODING = "fileEncoding";
    public static final String DELIMITED = "delimiter";
    public static final String VARIABLE_NAMES = "variableNames";

    public static final String RANDOM_ORDER = "randomOrder";
    public static final String IGNORE_FIRST_LINE = "ignoreFirstLine";
    public static final String REWIND_ON_THE_END = "rewindOnTheEndOfList";
    public static final String INDEPENDENT_LIST_PER_THREAD = "independentListPerThread";

    private final ThreadLocal<RandomCSVReader> threadLocalRandomCSVReader = new ThreadLocalSerializable<RandomCSVReader>() {
        @Override
        protected RandomCSVReader initialValue() {
            return null;
        }
    };

    private static class ThreadLocalSerializable<T> extends ThreadLocal<T> implements Serializable {
    }

    private RandomCSVReader randomCSVReader = null;
    private String filename;    // Real filename, with substituted variables

    // Public: will be called from TestRandomCSVAction as well
    public void trySetFinalFilename() {
        if (filename == null) {
            filename = getFinalFilename();
            randomCSVReader = createRandomCSVReader();
            threadLocalRandomCSVReader.set(createRandomCSVReader());
        }
    }

    @Override
    public void iterationStart(LoopIterationEvent loopIterationEvent) {
        trySetFinalFilename();
        boolean isIndependentListPerThread = isIndependentListPerThread();

        if (!isIndependentListPerThread && randomCSVReader == null) {
            throw new JMeterStopThreadException("All records in the CSV file have been passed.");
        }

        if (isRandomOrder()) {
            readRandom();
        } else {
            readConsistent();
        }
    }

    private String getFinalFilename() {
        String ret = getFilename();

        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        Pattern pattern = Pattern.compile("\\$\\{([a-z]+)}");
        Matcher matcher = pattern.matcher(ret);
        while (matcher.find()) {
            String contents;
            try {
                contents = variables.get(matcher.group(1));
            } catch (NullPointerException e) {
                contents = null;
            }

            if (contents != null)
                ret = ret.replace(matcher.group(), contents);
        }

        return ret;
    }

    private void readRandom() {
        final RandomCSVReader reader = getReader();
        long lineAddr;
        synchronized (reader) {
            if (reader.hasNextRecord()) {
                lineAddr = reader.getNextLineAddr();
            } else {
                // TODO: interrupt iteration
                if (randomCSVReader != null) {
                    randomCSVReader.close();
                }
                randomCSVReader = null;
                throw new JMeterStopThreadException("All records in the CSV file have been passed.");
            }
        }

        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        putVariables(variables, getDestinationVariableKeys(), reader.readLineWithSeek(lineAddr));
    }


    private void readConsistent() {
        final RandomCSVReader reader = getReader();
        synchronized (reader) {
            if (reader.hasNextRecord()) {
                JMeterVariables variables = JMeterContextService.getContext().getVariables();
                putVariables(variables, getDestinationVariableKeys(), reader.readNextLine());
            } else {
                // TODO: interrupt iteration
                if (randomCSVReader != null) {
                    try {
                        randomCSVReader.closeConsistentReader();
                    } catch (IOException e) {
                        LOGGER.warn("Failed to close Consistent Reader", e);
                    }
                }
                randomCSVReader = null;
                throw new JMeterStopThreadException("All records in the CSV file have been passed.");
            }
        }
    }

    public String[] getDestinationVariableKeys() {
        String vars = getVariableNames();
        return hasVariablesNames() ?
                JOrphanUtils.split(vars, ",") :
                getReader().getHeader();
    }

    private void putVariables(JMeterVariables variables, String[] keys, String[] values) {
        int minLen = Math.min(keys.length, values.length);
        for (int i = 0; i < minLen; i++) {
            variables.put(keys[i], values[i]);
        }
    }

    private RandomCSVReader getReader() {
        return isIndependentListPerThread() ? threadLocalRandomCSVReader.get() : randomCSVReader;
    }

    private RandomCSVReader createRandomCSVReader() {
        return new RandomCSVReader(
                filename,
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
    public void threadStarted() {

    }

    @Override
    public void threadFinished() {
        RandomCSVReader reader = getReader();
        if (reader != null) {
            reader.close();
            if (!isRandomOrder() && isIndependentListPerThread()) {
                try {
                    reader.closeConsistentReader();
                } catch (IOException e) {
                    LOGGER.warn("Failed to close Consistent Reader", e);
                }
            }
        }
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
        try {
            if (randomCSVReader != null && !isRandomOrder()) {
                randomCSVReader.closeConsistentReader();
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to close Consistent Reader", e);
        }
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

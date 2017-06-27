package com.blazemeter.jmeter.controller;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class ParallelSamplerTest {
    @BeforeClass
    public static void setUp() throws Exception {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        ctx.updateLoggers();
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void sample() throws Exception {
        ParallelSampler obj = new ParallelSampler();
        GenericController ctl = new GenericController();
        EmulSampler sam = new EmulSampler();
        addToContext(sam);
        ctl.addTestElement(sam);
        obj.addTestElement(ctl);
        obj.sample(null);
        assertEquals(1, sam.count);
    }

    public void addToContext(TestElement te) throws NoSuchFieldException, IllegalAccessException {
        Field field = JMeterThread.class.getDeclaredField("compiler");
        field.setAccessible(true);
        JMeterThread parentThread = JMeterContextService.getContext().getThread();
        TestCompiler parentCompiler = (TestCompiler) field.get(parentThread);
        parentCompiler.addNode(te, null);
        parentCompiler.subtractNode();
    }

    private class EmulSampler extends DebugSampler {
        private int count = 0;

        @Override
        public SampleResult sample(Entry e) {
            count++;
            return super.sample(e);
        }
    }
}
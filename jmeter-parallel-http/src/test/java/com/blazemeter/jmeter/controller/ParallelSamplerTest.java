package com.blazemeter.jmeter.controller;

import kg.apc.emulators.TestJMeterUtils;
import kg.apc.jmeter.samplers.DummySampler;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jorphan.collections.HashTree;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ParallelSamplerTest {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);

    @BeforeClass
    public static void setUpClass() throws Exception {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        ctx.updateLoggers();
        TestJMeterUtils.createJmeterEnv();
    }

    @Before
    public void setUp() {
        EmulSampler.instances = 0;
        EmulSampler.count.set(0);
    }

    @Test
    public void sample() throws Exception {
        JMeterThread thr = JMeterContextService.getContext().getThread();
        for (int n = 0; n < 1000; n++) {// we're doing good check here because of multi-threads
            log.debug("\n\n\nTry #" + n);
            EmulSampler.instances = 0;
            EmulSampler.count.set(0);
            ParallelSampler obj = new ParallelSampler();

            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));

            obj.sample(null);
            assertEquals(5, EmulSampler.count.get());
        }
    }

    @Test
    public void underLoop() throws Exception {
        EmulSampler payload = new EmulSampler();
        payload.setName("payload");

        ParallelSampler sam = new ParallelSampler();
        sam.setName("Parallel Sampler");
        sam.addTestElement(payload);

        LoopController ctl = getLoopController(5);
        ctl.addTestElement(sam);

        JMeterThread thr = new JMeterThread(new HashTree(ctl), sam, sam.notifier);
        thr.setThreadName("root");
        thr.setThreadGroup(new DummyThreadGroup());
        JMeterContextService.getContext().setThread(thr);

        addToContext(sam, thr);
        addToContext(payload, thr);

        sam.setRunningVersion(true);
        ctl.setRunningVersion(true);
        payload.setRunningVersion(true);
        thr.run();
        assertEquals(5, EmulSampler.count.get());
    }

    private LoopController getLoopController(int loops) {
        LoopController ctl = new LoopControllerTracked();
        ctl.setName("Top Loop");
        ctl.setLoops(loops);
        ctl.setContinueForever(false);
        return ctl;
    }

    private TestElement getContextedSampler(JMeterThread thr) throws NoSuchFieldException, IllegalAccessException {
        EmulSampler sam = new EmulSampler();
        sam.setName(String.valueOf(EmulSampler.instances));
        addToContext(sam, thr);
        return sam;
    }

    public void addToContext(TestElement te, JMeterThread parentThread) throws NoSuchFieldException, IllegalAccessException {
        Field field = JMeterThread.class.getDeclaredField("compiler");
        field.setAccessible(true);
        TestCompiler parentCompiler = (TestCompiler) field.get(parentThread);
        parentCompiler.addNode(te, null);
        parentCompiler.subtractNode();
    }

    public static class EmulSampler extends DummySampler {
        private volatile transient static int instances = 0;
        private volatile transient static AtomicInteger count = new AtomicInteger();

        public EmulSampler() {
            instances++;
        }

        @Override
        public SampleResult sample(Entry e) {
            count.addAndGet(1);
            log.debug("Sample #" + count.get());
            ThreadLocalRandom.current().nextInt(10);
            return super.sample(e);
        }
    }

    private class LoopControllerTracked extends LoopController {
        @Override
        public Sampler next() {
            Sampler next = super.next();
            log.debug(getName() + " => " + next);
            return next;
        }

        @Override
        public String toString() {
            return getName() + " " + super.toString();
        }
    }
}
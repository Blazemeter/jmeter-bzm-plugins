package com.blazemeter.jmeter.controller;

import kg.apc.emulators.EmulatorThreadMonitor;
import kg.apc.emulators.TestJMeterUtils;
import kg.apc.jmeter.samplers.DummySampler;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.ThreadSafeCookieManager;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParallelSamplerTest {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);

    @BeforeClass
    public static void setUpClass() throws Exception {
        /*
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        ctx.updateLoggers();
        */
        TestJMeterUtils.createJmeterEnv();
    }

    @Before
    public void setUp() {
        EmulSampler.instances = 0;
        EmulSampler.count.set(0);
    }

    @Test
    public void sample() throws Exception {
        JMeterThread dummy = new JMeterThread(new HashTree(new GenericController()), null, null);
        JMeterContextService.getContext().setEngine(new StandardJMeterEngine());
        JMeterContextService.getContext().setThread(dummy);
        JMeterThread thr = JMeterContextService.getContext().getThread();
        for (int n = 0; n < 1000; n++) {// we're doing good check here because of multi-threads
            log.debug("\n\n\nTry #" + n);
            EmulSampler.instances = 0;
            EmulSampler.count.set(0);
            ParallelSampler obj = new ParallelSampler();
            obj.threadStarted();
            obj.setGenerateParent(true);

            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));
            obj.addTestElement(getContextedSampler(thr));

            SampleResult res = obj.sample(null);
            assertEquals(5, EmulSampler.count.get());
            if (res.getSubResults().length < 5) {
                throw new AssertionError();
            }

            assertEquals(5, res.getSubResults().length);
        }
    }

    @Test
    public void underLoop() throws Exception {
        EmulSampler payload = new EmulSampler();
        payload.setName("payload");

        ParallelSampler sam = new ParallelSampler();
        sam.threadStarted();
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


    @Test
    public void testThreadSafeCookieManager() throws Exception {
        CookieManager cookieManager = new CookieManager();
        Cookie c = new Cookie();
        c.setName("name");
        c.setValue("value");
        c.setDomain("blazedemo.com");
        c.setPath("");
        cookieManager.add(c);
        HashTree hashtree = createTestTree(cookieManager);

        TestPlan testPlan = new TestPlan();
        HashTree testPlanHashTree = new HashTree();
        testPlanHashTree.put(testPlan, hashtree);

        StandardJMeterEngine engine = new StandardJMeterEngine();
        engine.configure(testPlanHashTree);

        EmulatorThreadMonitor monitor = new EmulatorThreadMonitor();
        JMeterThread thread = new JMeterThread(hashtree, monitor, null);
        thread.setThreadName("test thread");
        JMeterContextService.getContext().setThread(thread);

        ParallelSampler parallel = new ParallelSampler();
        parallel.testStarted();

        Field field = StandardJMeterEngine.class.getDeclaredField("test");
        field.setAccessible(true);
        HashTree testTree = (HashTree) field.get(engine);

        assertTrue("CookieManager should be changed to ThreadSafeCookieManager", testTree.toString().contains("ThreadSafeCookieManager"));
        ListedHashTree loop = (ListedHashTree) (testTree.values().toArray()[0]);
        ListedHashTree threadSafeManager = ((ListedHashTree) (loop.values().toArray()[0]));

        CookieManager mgr = (CookieManager) threadSafeManager.getArray()[0];
        assertTrue(mgr instanceof ThreadSafeCookieManager);
        assertEquals(1, mgr.getCookieCount());
        JMeterProperty property = mgr.getCookies().get(0);
        assertEquals("name", property.getName());
        assertEquals("blazedemo.com\tTRUE\t\tFALSE\t0\tname\tvalue", property.getStringValue());
    }

    private HashTree createTestTree(ConfigTestElement configTestElement) {
        HashTree hashTree = new HashTree();

        LoopController loopController = new LoopController();

        HashTree loopNode = new HashTree();
        loopNode.add(loopController, configTestElement);

        hashTree.add(loopNode);
        return hashTree;
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

    @Test(timeout=3000)
    public void testStartNextIteration() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
        TestSampleListener listener = new TestSampleListener();

        TestAction action = new TestAction();
        action.setAction(3);

        DebugSampler samplerBefore = new DebugSampler();
        samplerBefore.setName("samplerBefore");

        DebugSampler samplerAfter = new DebugSampler();
        samplerAfter.setName("samplerAfter");

        WhileController whileController = new WhileController();

        ParallelSampler sampler = new ParallelSampler();
        sampler.setGenerateParent(true);
        LoopController loop = new LoopController();
        loop.setLoops(2);
        loop.setContinueForever(false);

        // parallel subtree
        ListedHashTree parallelTree = new ListedHashTree();
        parallelTree.add(samplerBefore);
        parallelTree.add(action);

        // while subtree
        ListedHashTree whileTree = new ListedHashTree();
        whileTree.add(whileController, parallelTree);

        // parallel Sampler subtree
        ListedHashTree parallelSamplerTree = new ListedHashTree();
        parallelSamplerTree.add(sampler, whileTree);

        // TG sub tree
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);
        threadGroup.setSamplerController(loop);

        ListedHashTree loopTree = new ListedHashTree();
        loopTree.add(threadGroup, parallelSamplerTree);
        loopTree.add(threadGroup, samplerAfter);
        loopTree.add(threadGroup, listener);

        TestCompiler compiler = new TestCompiler(loopTree);
        loopTree.traverse(compiler);

        ListenerNotifier notifier = new ListenerNotifier();

        JMeterThread thread = new JMeterThread(loopTree, threadGroup, notifier);
        thread.setThreadGroup(threadGroup);
        thread.setEngine(new StandardJMeterEngine());
        thread.setOnErrorStopThread(true);
        thread.run();

        assertEquals(2, listener.events.size());
    }

    @Test(timeout=3000)
    public void testInfinityStopTest() {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
        TestSampleListener listener = new TestSampleListener();

        TestAction action = new TestAction();
        action.setAction(0);
        action.setTarget(2);

        WhileController whileController = new WhileController();

        ParallelSampler sampler = new ParallelSampler();
        sampler.setGenerateParent(true);
        LoopController loop = new LoopController();
        loop.setLoops(1);
        loop.setContinueForever(false);

        // test tree
        ListedHashTree hashTree = new ListedHashTree();
        hashTree.add(loop);
        hashTree.add(loop, sampler);
        hashTree.add(sampler, listener);
        hashTree.add(sampler, whileController);
        hashTree.add(whileController, action);
        hashTree.add(whileController, listener);

        TestCompiler compiler = new TestCompiler(hashTree);
        hashTree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);

        ListenerNotifier notifier = new ListenerNotifier();

        JMeterThread thread = new JMeterThread(hashTree, threadGroup, notifier);
        thread.setThreadGroup(threadGroup);
        thread.setOnErrorStopThread(true);
        thread.setEngine(new StandardJMeterEngine());
        thread.run();

        assertEquals(1, listener.events.size());
    }

    public class TestSampleListener extends ResultCollector implements SampleListener {
        public List<SampleEvent> events = new ArrayList<>();

        @Override
        public void sampleOccurred(SampleEvent e) {
            events.add(e);
        }

        @Override
        public void sampleStarted(SampleEvent e) {
            events.add(e);
        }

        @Override
        public void sampleStopped(SampleEvent e) {
            events.add(e);
        }
    }
}
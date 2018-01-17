package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextServiceAccessorParallel;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

// we implement Controller only to enable GUI to add child elements into it
public class ParallelSampler extends AbstractSampler implements Controller, ThreadListener, Interruptible, JMeterThreadMonitor, Serializable {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);
    private static final String GENERATE_PARENT = "PARENT_SAMPLE";
    protected List<TestElement> controllers = new ArrayList<>();
    protected final ParallelListenerNotifier notifier = new ParallelListenerNotifier();
    private ExecutorService executorService;

    @Override
    public void addTestElement(TestElement te) {
        if (te instanceof Controller || te instanceof Sampler) {
            controllers.add(te);
        }
        log.debug("Added {}, list size: {}", te, controllers.size());
    }

    @Override
    // prevents property reset for wrapper controllers
    public void setRunningVersion(boolean runningVersion) {
        super.setRunningVersion(runningVersion);
        for (TestElement ctl : controllers) {
            ctl.setRunningVersion(runningVersion);
        }
    }

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setResponseCode("200");
        res.setResponseMessage("OK");
        res.setSuccessful(true);
        res.setSampleLabel(getName());
        res.setResponseData("".getBytes());

        notifier.setContainer(res);

        final List<JMeterThread> jMeterThreads = new LinkedList<>();

        StringBuilder reqText = new StringBuilder("Parallel items:\n");
        for (TestElement ctl : controllers) {
            reqText.append(ctl.getName()).append("\n");
            JMeterThread jmThread = new JMeterThreadParallel(getTestTree(ctl), this, notifier, getGenerateParent());
            jmThread.setThreadName("parallel " + this.getName());
            jmThread.setThreadGroup(new DummyThreadGroup());
            injectVariables(jmThread, this.getThreadContext());
            jMeterThreads.add(jmThread);
        }


        res.setSamplerData(reqText.toString());
        res.sampleStart();

        Collection<Future<?>> futures = new LinkedList<>();
        for (JMeterThread jmThread : jMeterThreads) {
            futures.add(executorService.submit(jmThread));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
                log.debug("Thread is done {}", future.isDone());
            } catch (InterruptedException | ExecutionException e1) {
                log.debug("Interrupted {}", future.isCancelled());
            }
        }

        if (res.getEndTime() == 0) {
            res.sampleEnd();
        }
        return getGenerateParent() ? res : null;
    }

    private HashTree getTestTree(TestElement te) {
        LoopController wrapper = new LoopController(); // can't use GenericController because of infinite looping
        wrapper.setLoops(1);
        wrapper.setContinueForever(false);

        wrapper.addTestElement(te);
        wrapper.setName("wrapped " + te.getName());
        wrapper.setRunningVersion(isRunningVersion());

        HashTree tree = new HashTree();
        tree.add(wrapper);
        return tree;
    }

    @Override
    public boolean interrupt() {
        executorService.shutdown();
        return true;
    }

    @Override
    public Sampler next() {
        return null;
    }

    @Override
    public boolean isDone() {
        return true; // most likely having true here is ruining things
    }

    @Override
    public void initialize() {
        log.debug("Initialize");
    }

    @Override
    public void triggerEndOfLoop() {
        log.debug("Trigger End of loop");
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        JMeterContextServiceAccessorParallel.incrNumberOfThreads();
        try {
            Field field = AbstractTestElement.class.getDeclaredField("threadContext");
            field.setAccessible(true);
            if (thread instanceof JMeterThreadParallel) {
                JMeterThreadParallel pthr = (JMeterThreadParallel) thread;
                for (TestElement te : pthr.getParallelCompiler().getKnownSamplers()) {
                    field.set(te, null);
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            log.warn("Failed to reset context", e);
        }
    }

    @Override
    public void addIterationListener(LoopIterationListener listener) {

    }

    @Override
    public void removeIterationListener(LoopIterationListener iterationListener) {

    }

    public boolean getGenerateParent() {
        return getPropertyAsBoolean(GENERATE_PARENT);
    }

    public void setGenerateParent(boolean value) {
        setProperty(GENERATE_PARENT, value);
    }

    private void injectVariables(JMeterThread jmThread, JMeterContext threadContext) {
        if (threadContext != null && threadContext.getVariables() != null) {
            try {
                Class<JMeterThread> cls = JMeterThread.class;
                Field vars = cls.getDeclaredField("threadVars");
                vars.setAccessible(true);
                vars.set(jmThread, threadContext.getVariables());
            } catch (Throwable ex) {
                log.warn("Cannot inject variables into parallel thread ", ex);
            }
        }
    }

    private void changeVariablesMap() {
        try {
            JMeterContext context = this.getThreadContext();
            if (context != null && context.getVariables() != null) {
                JMeterVariables jMeterVariables = context.getVariables();
                Class<JMeterVariables> cls = JMeterVariables.class;
                Field variablesField = cls.getDeclaredField("variables");
                variablesField.setAccessible(true);
                Object obj = variablesField.get(jMeterVariables);
                synchronized (obj) {
                    if (obj instanceof Map) {
                        Map variables = (Map) obj;
                        if (!(variables instanceof ConcurrentHashMap)) {
                            variablesField.set(jMeterVariables, new ConcurrentHashMap(variables));
                        }
                    } else {
                        log.warn("Unexpected variables map type " + obj.getClass().getName());
                    }
                }
            }
        } catch (Throwable ex) {
            log.warn("Cannot change variables map ", ex);
        }
    }

    @Override
    public void threadStarted() {
        changeVariablesMap();
        executorService = Executors.newCachedThreadPool(new ParallelThreadFactory(this.getName()));
    }

    @Override
    public void threadFinished() {
        executorService.shutdown();
    }

    public static class ParallelThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final String namePrefix;

        public ParallelThreadFactory(String controllerName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "parallel " + controllerName;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix, 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }


}

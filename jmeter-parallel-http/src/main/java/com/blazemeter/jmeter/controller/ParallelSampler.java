package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// we implement Controller only to enable GUI to add child elements into it
public class ParallelSampler extends AbstractSampler implements Controller, Interruptible, JMeterThreadMonitor, Serializable {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);
    protected transient List<TestElement> controllers = new ArrayList<>();
    protected final ParallelListenerNotifier notifier = new ParallelListenerNotifier();
    private Map<JMeterThread, Thread> threads = new HashMap<>();

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

        threads = new HashMap<>(controllers.size());
        StringBuilder reqText = new StringBuilder("Parallel items:\n");
        for (TestElement ctl : controllers) {
            reqText.append(ctl.getName()).append("\n");
            JMeterThread jmThread = new JMeterThreadParallel(getTestTree(ctl), this, notifier);
            jmThread.setThreadName("parallel " + ctl.getName());
            jmThread.setThreadGroup(new DummyThreadGroup());
            Thread osThread = new Thread(jmThread, "jmeter-parallel " + ctl.getName());
            threads.put(jmThread, osThread);
        }
        res.setSamplerData(reqText.toString());
        res.sampleStart();
        for (Thread thr : threads.values()) {
            log.debug("Starting thread {}", thr);
            thr.start();
        }

        for (Thread thr : threads.values()) {
            try {
                thr.join();
                log.debug("Thread is done {}", thr);
            } catch (InterruptedException e1) {
                log.debug("Interrupted {}", thr);
            }
        }

        if (res.getEndTime() == 0) {
            res.sampleEnd();
        }
        return res;
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
        boolean interrupted = true;
        for (JMeterThread thr : threads.keySet()) {
            log.debug("Interrupting thread {}", thr);
            interrupted &= thr.interrupt();
        }
        return interrupted;
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
}

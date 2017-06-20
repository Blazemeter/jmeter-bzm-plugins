package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParallelSampler extends AbstractSampler implements Sampler, Controller, Interruptible, JMeterThreadMonitor {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);
    protected transient List<Controller> controllers = new ArrayList<>();
    private final ParallelListenerNotifier notifier = new ParallelListenerNotifier();

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        res.setSuccessful(true);
        res.sampleStart();

        notifier.setContainer(res);
        log.debug("Parallel controllers size: {}", controllers.size());

        // TODO: make this use executor pool or whatever
        Map<JMeterThread, Thread> threads = new HashMap<>(controllers.size());
        for (Controller ctl : controllers) {
            HashTree test = new HashTree();
            test.add(ctl);
            JMeterThread jmThread = new JMeterThreadParallel(test, this, notifier);
            jmThread.setThreadGroup(new DummyThreadGroup());
            Thread osThread = new Thread(jmThread, "parallel " + ctl.getName());
            threads.put(jmThread, osThread);
        }

        for (Thread thr : threads.values()) {
            log.debug("Starting thread {}", thr);
            thr.start();
        }

        for (Thread thr : threads.values()) {
            try {
                thr.join();
                log.debug("Thread is done {}", thr);
            } catch (InterruptedException e1) {
                log.debug("Interrupted");
            }
        }

        if (res.getEndTime() == 0) {
            res.sampleEnd();
        }
        return res;
    }

    @Override
    public boolean interrupt() {
        return false; // TOOD
    }

    public void add(TestElement sampler) {
        LoopController wrapper = new LoopController();
        wrapper.setLoops(1);
        wrapper.setContinueForever(false);
        wrapper.addTestElement(sampler);
        wrapper.setName(sampler.getName());
        controllers.add(wrapper);
    }

    @Override
    public Sampler next() {
        return null;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void addIterationListener(LoopIterationListener listener) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void removeIterationListener(LoopIterationListener iterationListener) {

    }

    @Override
    public void triggerEndOfLoop() {

    }

    @Override
    public void addTestElement(TestElement te) {
        log.debug("Add test element into controller: {}", te);
        if (te instanceof Controller) {
            controllers.add((Controller) te);
        } else if (te instanceof Sampler) {
            GenericController wrapper = new GenericController();
            wrapper.addTestElement(te);
            wrapper.setName(te.getName());
            controllers.add(wrapper);
        }
        log.debug("List size: {}", controllers.size());
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        log.debug("Parallel thread finished: {}", thread);
    }
}

package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
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
    private final ListenerNotifier notifier = new ListenerNotifier();
    private Map<JMeterThread, Thread> threads = new HashMap<>();

    @Override
    public void addTestElement(TestElement te) {
        if (te instanceof Controller || te instanceof Sampler) {
            LoopController wrapper = new LoopController();
            wrapper.setLoops(1);
            wrapper.addTestElement(te);
            wrapper.setName("wrapped "+te.getName());
            controllers.add(wrapper);
        }
        log.debug("Added {}, list size: {}", te, controllers.size());
    }

    @Override
    public SampleResult sample(Entry e) {
        log.debug("Parallel controllers size: {}", controllers.size());

        // TODO: make this use executor pool or whatever
        threads = new HashMap<>(controllers.size());
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
            /*
            try {
                thr.join();
                log.debug("Thread is done {}", thr);
            } catch (InterruptedException e1) {
                log.debug("Interrupted");
            }
            */
        }

        for (Thread thr : threads.values()) {
            try {
                thr.join();
                log.debug("Thread is done {}", thr);
            } catch (InterruptedException e1) {
                log.debug("Interrupted {}", thr);
            }
        }

        return null;
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
        return true;
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
    public void threadFinished(JMeterThread thread) {
        log.debug("Parallel thread finished: {}", thread);
    }
}

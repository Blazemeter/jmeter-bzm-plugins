package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.threads.*;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParallelSampler extends AbstractSampler implements Sampler, Interruptible, JMeterThreadMonitor {
    private static final Logger log = LoggerFactory.getLogger(ParallelController.class);
    protected transient List<Controller> controllers = new ArrayList<>();
    private final ListenerNotifier notifier = new ListenerNotifier();

    @Override
    public SampleResult sample(Entry e) {
        SampleResult res = new SampleResult();
        res.setSuccessful(true);
        res.sampleStart();

        Map<JMeterThread, Thread> threads = new HashMap<>(controllers.size());
        for (Controller ctl : controllers) {
            HashTree test = new HashTree();
            test.add(ctl);
            JMeterThread jmThread = new JMeterThread(test, this, notifier);
            jmThread.setThreadGroup(new DummyThreadGroup());
            Thread osThread = new Thread(jmThread, "parallel " + ctl.getName());
            threads.put(jmThread, osThread);
        }

        for (Thread thr : threads.values()) {
            thr.start();
        }

        for (Thread thr : threads.values()) {
            try {
                thr.join();
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

    @Override
    public void threadFinished(JMeterThread thread) {
        // TODO
    }

    protected int getParallelCount() {
        return controllers.size();
    }

    public void addItems(List<Controller> controllers) {
        this.controllers.addAll(controllers);
    }

    public void addSampler(Sampler sampler) {
        GenericController wrapper = new GenericController();
        wrapper.addTestElement(sampler);
        wrapper.setName(sampler.getName());
        controllers.add(wrapper);
    }

    public void addController(Controller ctl) {
        controllers.add(ctl);
    }

    private class DummyThreadGroup extends AbstractThreadGroup {
        @Override
        public boolean stopThread(String s, boolean b) {
            return false;
        }

        @Override
        public int numberOfActiveThreads() {
            return 0;
        }

        @Override
        public void start(int i, ListenerNotifier listenerNotifier, ListedHashTree listedHashTree, StandardJMeterEngine standardJMeterEngine) {

        }

        @Override
        public JMeterThread addNewThread(int i, StandardJMeterEngine standardJMeterEngine) {
            return null;
        }

        @Override
        public boolean verifyThreadsStopped() {
            return false;
        }

        @Override
        public void waitThreadsStopped() {

        }

        @Override
        public void tellThreadsToStop() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void threadFinished(JMeterThread jMeterThread) {

        }
    }
}

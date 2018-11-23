package com.blazemeter.jmeter.controller;

import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class DummyThreadGroup extends AbstractThreadGroup {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);
    private final List<JMeterThread> jMeterThreads = new LinkedList<>();

    public void reset() {
        this.jMeterThreads.clear();
    }

    public void addThread(JMeterThread thread) {
        this.jMeterThreads.add(thread);
    }

    public void stopAllThreads() {
        for (JMeterThread thread : jMeterThreads) {
            thread.stop();
        }
    }

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
    public boolean verifyThreadsStopped() {
        return false;
    }

    @Override
    public void waitThreadsStopped() {

    }

    @Override
    public void tellThreadsToStop() {
        stopAllThreads();
    }

    @Override
    public void stop() {
        stopAllThreads();
    }

    @Override
    public void threadFinished(JMeterThread jMeterThread) {
        log.debug("BG thread finished");
    }
}

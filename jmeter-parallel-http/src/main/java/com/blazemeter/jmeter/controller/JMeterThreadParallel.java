package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.*;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class JMeterThreadParallel extends JMeterThread {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);

    public JMeterThreadParallel(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier notifier) {
        super(test, monitor, notifier);
        setThreadGroup(new DummyThreadGroup());
        try {
            copyCompilerFromParent();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected void copyCompilerFromParent() throws IllegalAccessException, NoSuchFieldException {
        Field field = JMeterThread.class.getDeclaredField("compiler");
        field.setAccessible(true);
        JMeterThread parentThread = JMeterContextService.getContext().getThread();
        TestCompiler parentCompiler = (TestCompiler) field.get(parentThread);
        field.set(this, parentCompiler);
    }
}

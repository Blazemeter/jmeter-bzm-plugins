package com.blazemeter.jmeter.controller;

import org.apache.jmeter.threads.*;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;

public class JMeterThreadParallel extends JMeterThread {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);
    private TestCompilerParallel parallelCompiler;
    private boolean generateParent;

    public JMeterThreadParallel(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier notifier, boolean generateParent) {
        super(test, monitor, notifier);
        this.generateParent = generateParent;
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
        if (parentThread == null) {
            throw new NullPointerException();
        }
        TestCompiler parentCompiler = (TestCompiler) field.get(parentThread);
        parallelCompiler = cloneTestCompiler(parentCompiler);
        field.set(this, parallelCompiler);
    }

    private TestCompilerParallel cloneTestCompiler(TestCompiler parent) throws NoSuchFieldException, IllegalAccessException {
        TestCompilerParallel cloned = new TestCompilerParallel(new HashTree(), generateParent);

        Field samplerConfigMap = TestCompiler.class.getDeclaredField("samplerConfigMap");
        samplerConfigMap.setAccessible(true);
        samplerConfigMap.set(cloned, ((HashMap) (samplerConfigMap.get(parent))).clone());

        Field transactionControllerConfigMap = TestCompiler.class.getDeclaredField("transactionControllerConfigMap");
        transactionControllerConfigMap.setAccessible(true);
        transactionControllerConfigMap.set(cloned, ((HashMap) (transactionControllerConfigMap.get(parent))).clone());

        return cloned;
    }

    public TestCompilerParallel getParallelCompiler() {
        return parallelCompiler;
    }

    @Override
    public void run() {
        JMeterContextServiceAccessorParallel.decrNumberOfThreads();
        super.run();
    }
}

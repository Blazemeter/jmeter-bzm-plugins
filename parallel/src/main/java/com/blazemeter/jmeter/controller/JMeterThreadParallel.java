package com.blazemeter.jmeter.controller;

import org.apache.jmeter.threads.*;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class JMeterThreadParallel extends JMeterThread {
    private static final Logger log = LoggerFactory.getLogger(ParallelSampler.class);
    private TestCompilerParallel parallelCompiler;
    private boolean generateParent;
    private boolean isStopped = false;
    private final JMeterThread parentThread;

    public JMeterThreadParallel(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier notifier, boolean generateParent) {
        super(test, monitor, notifier);
        this.generateParent = generateParent;
        parentThread = JMeterContextService.getContext().getThread();
        if (parentThread == null) {
            throw new NullPointerException();
        }
        try {
            copyCompilerFromParent();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected void copyCompilerFromParent() throws IllegalAccessException, NoSuchFieldException {
        Field field = JMeterThread.class.getDeclaredField("compiler");
        field.setAccessible(true);
        TestCompiler parentCompiler = (TestCompiler) field.get(parentThread);
        parallelCompiler = cloneTestCompiler(parentCompiler);
        field.set(this, parallelCompiler);
    }

    private TestCompilerParallel cloneTestCompiler(final TestCompiler parent) throws NoSuchFieldException, IllegalAccessException {
        final TestCompilerParallel cloned = new TestCompilerParallel(new HashTree(), generateParent);

        final Field samplerConfigMapField = TestCompiler.class.getDeclaredField("samplerConfigMap");
        samplerConfigMapField.setAccessible(true);

        final Field transactionControllerConfigMapField = TestCompiler.class.getDeclaredField("transactionControllerConfigMap");
        transactionControllerConfigMapField.setAccessible(true);

        final Map<?, ?> sampleConfigMap;
        final Map<?, ?> transactionMap;

        if (Objects.equals(IdentityHashMap.class, samplerConfigMapField.getType())) {
            // JMeter >= 5.6
            sampleConfigMap = new IdentityHashMap<>((Map<?, ?>) samplerConfigMapField.get(parent));
            transactionMap = new IdentityHashMap((Map<?, ?>) transactionControllerConfigMapField.get(parent));
        } else {
            // Backward compatibility with JMeter <= 5.5
            sampleConfigMap = new HashMap<>((Map<?, ?>) samplerConfigMapField.get(parent));
            transactionMap = new HashMap<>((Map<?, ?>) transactionControllerConfigMapField.get(parent));
        }

        samplerConfigMapField.set(cloned, sampleConfigMap);
        transactionControllerConfigMapField.set(cloned, transactionMap);

        return cloned;
    }

    public TestCompilerParallel getParallelCompiler() {
        return parallelCompiler;
    }

    @Override
    public void run() {
        JMeterContextServiceAccessorParallel.decrNumberOfThreads();
        super.run();
        if (isStopped) {
            log.info("Stopping current thread");
            parentThread.stop();
        }
    }

    @Override
    public void stop() {
        isStopped = true;
        log.debug("Parallel Thread was stopped. Parent thread will be stopped after parallel sampler.");
        super.stop();
    }

    public void softStop() {
        log.debug("Parallel Thread was stopped. Parent thread will NOT be stopped after parallel sampler.");
        super.stop();
    }
}

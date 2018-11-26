package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.HashTree;
import org.junit.Test;

import static org.junit.Assert.*;

public class JMeterThreadParallelTest {

    @Test
    public void testStopParentThread() {
        DummyThreadGroup monitor = new DummyThreadGroup();
        ListenerNotifier listenerNotifier = new ListenerNotifier();

        HashTree tree = new HashTree();
        LoopControllerExt loopControllerExt = new LoopControllerExt();
        tree.add(loopControllerExt);

        JMeterThreadExt parentThread = new JMeterThreadExt(tree, monitor, listenerNotifier);
        parentThread.setThreadGroup(monitor);
        JMeterContextService.getContext().setThread(parentThread);

        JMeterThreadParallel parallel = new JMeterThreadParallel(tree, monitor, listenerNotifier, true);
        parallel.setThreadGroup(monitor);
        loopControllerExt.thread = parallel;
        parallel.run();

        assertTrue(parentThread.isStopped);
    }

    private static class LoopControllerExt extends LoopController {
        JMeterThread thread;

        @Override
        public Sampler next() {
            thread.stop();
            return super.next();
        }
    }

    private static class JMeterThreadExt extends JMeterThread {
        boolean isStopped = false;

        public JMeterThreadExt(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note) {
            super(test, monitor, note);
        }

        @Override
        public void stop() {
            isStopped = true;
            super.stop();
        }
    }
}
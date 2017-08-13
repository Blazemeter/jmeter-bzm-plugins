package org.apache.jmeter.threads;

// you did not see this, ok?
public class JMeterContextServiceAccessorParallel { // "Parallel" is to not clash with Debugger's class
    public static void decrNumberOfThreads() {
        JMeterContextService.decrNumberOfThreads();
    }

    public static void incrNumberOfThreads() {
        JMeterContextService.incrNumberOfThreads();
    }
}

package org.apache.jmeter.threads;

// you did not see this, ok?
public class JMeterContextServiceAccessor {
    public static void decrNumberOfThreads() {
        JMeterContextService.decrNumberOfThreads();
    }

    public static void incrNumberOfThreads() {
        JMeterContextService.incrNumberOfThreads();
    }
}

package com.blazemeter.jmeter.controller;


import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.ListenerNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class ParallelListenerNotifier extends ListenerNotifier implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(ParallelListenerNotifier.class);
    private SampleResult container = new SampleResult();

    @Override
    public void notifyListeners(SampleEvent res, List<SampleListener> listeners) {
        log.debug("Adding subresult " + res.getResult());
        synchronized (this) {
            container.addSubResult(res.getResult());
            if (!res.getResult().isSuccessful()) {
                container.setSuccessful(false);
            }
        }
        super.notifyListeners(res, listeners);
        log.debug("Added subresult " + res.getResult());
    }

    public void setContainer(SampleResult container) {
        this.container = container;
    }

    public SampleResult getContainer() {
        return container;
    }
}
package com.blazemeter.jmeter.controller;


import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.ListenerNotifier;

import java.util.List;

public class ParallelListenerNotifier extends ListenerNotifier {
    private SampleResult container = new SampleResult();

    @Override
    public void notifyListeners(SampleEvent res, List<SampleListener> listeners) {
        container.addSubResult(res.getResult());
        super.notifyListeners(res, listeners);
    }

    public void setContainer(SampleResult container) {
        this.container = container;
    }
}
package com.blazemeter.jmeter.controller;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParallelListenerNotifierTest {

    @Test
    public void testSubResultsLabels() {
        ParallelListenerNotifier notifier = new ParallelListenerNotifier();
        SampleResult container = new SampleResult() {
            @Override
            public void addSubResult(SampleResult subResult) {
                // generate JMeter 5.0 behaviour
                // https://bz.apache.org/bugzilla/show_bug.cgi?id=62550
                subResult.setSampleLabel("new Label");
            }
        };


        notifier.setContainer(container);

        SampleResult subSampler = new SampleResult();
        subSampler.setSampleLabel("label");

        SampleListenerExt sampleListenerExt = new SampleListenerExt();
        List<SampleListener> list = new ArrayList<>();
        list.add(sampleListenerExt);


        notifier.notifyListeners(new SampleEvent(subSampler, ""), list);
        assertEquals("label", subSampler.getSampleLabel());
        assertEquals(1, sampleListenerExt.count);
    }

    private static class SampleListenerExt extends AbstractTestElement implements SampleListener {
        int count = 0;
        @Override
        public void sampleOccurred(SampleEvent sampleEvent) {
            assertEquals("label", sampleEvent.getResult().getSampleLabel());
            count++;
        }

        @Override
        public void sampleStarted(SampleEvent sampleEvent) {

        }

        @Override
        public void sampleStopped(SampleEvent sampleEvent) {

        }
    }


}
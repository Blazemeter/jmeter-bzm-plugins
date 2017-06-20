package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ParallelControllerTest {
    @Test
    public void next() throws Exception {
        ParallelController obj = new ParallelController();
        GenericController ctl = new GenericController();
        ctl.addTestElement(new DebugSampler());
        obj.addTestElement(ctl);
        obj.addTestElement(new DebugSampler());
        ParallelSampler sam = (ParallelSampler) obj.next();
        assertEquals(2, sam.controllers.size());

        SampleResult res = sam.sample(null);
        assertEquals(2, res.getSubResults().length);
    }
}
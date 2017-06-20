package com.blazemeter.jmeter.controller;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ParallelSamplerTest {
    @BeforeClass
    public static void setUp() throws Exception {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void sample() throws Exception {
        ParallelSampler obj = new ParallelSampler();
        ///obj.addSampler(new DebugSampler());
        GenericController ctl = new GenericController();
        ctl.addTestElement(new DebugSampler());
        obj.addController(ctl);
        SampleResult res = obj.sample(null);
        assertTrue(res.isSuccessful());
    }
}
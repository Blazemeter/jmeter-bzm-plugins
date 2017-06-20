package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

import java.util.ArrayList;
import java.util.List;

public class ParallelSampler extends AbstractSampler implements Sampler {
    protected transient List<Controller> controllers = new ArrayList<>();

    @Override
    public SampleResult sample(Entry e) {
        return null;
    }

    protected int getParallelCount() {
        return controllers.size();
    }

    public void addItems(List<Controller> controllers) {
        this.controllers.addAll(controllers);
    }

    public void addSampler(Sampler sampler) {
        GenericController wrapper = new GenericController();
        wrapper.addTestElement(sampler);
        controllers.add(wrapper);
    }

    public void addController(Controller ctl) {
        controllers.add(ctl);
    }
}

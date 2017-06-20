package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParallelController extends AbstractTestElement implements Controller {
    private static final Logger log = LoggerFactory.getLogger(ParallelController.class);
    protected transient List<Controller> subControllersAndSamplers = new ArrayList<>();
    private boolean done = false;

    @Override
    public Sampler next() {
        done = true;
        ParallelSampler parallelSampler = new ParallelSampler();
        parallelSampler.addItems(subControllersAndSamplers);
        return parallelSampler;
    }

    @Override
    public void initialize() {
        done = false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public void addIterationListener(LoopIterationListener loopIterationListener) {
        // ignore
    }

    @Override
    public void removeIterationListener(LoopIterationListener loopIterationListener) {
        // ignore
    }

    @Override
    public void triggerEndOfLoop() {
        // ignore
    }

    @Override
    public void addTestElement(TestElement child) {
        if (child instanceof Controller) {
            subControllersAndSamplers.add((Controller) child);
        } else if (child instanceof Sampler) {
            GenericController wrapper = new GenericController();
            wrapper.addTestElement(child);
            wrapper.setName(child.getName());
            subControllersAndSamplers.add(wrapper);
        }
    }
}

package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParallelController extends AbstractTestElement implements Controller {
    private static final Logger log = LoggerFactory.getLogger(ParallelController.class);
    protected transient List<Controller> subControllersAndSamplers = new ArrayList<>();
    private boolean done = false;
    private ParallelSampler sampler = new ParallelSampler();

    @Override
    public Sampler next() {
        log.debug("get next {}", Arrays.toString(subControllersAndSamplers.toArray()));
        done = true;

        HashTree test = new HashTree();
        test.add(sampler);
        JMeterThread jmThread = new JMeterThread(test, sampler, new ListenerNotifier());

        sampler.addItems(subControllersAndSamplers);
        sampler.setName(getName());
        return sampler;
    }

    @Override
    public void initialize() {

        log.debug("Initialize");
        done = false;
        subControllersAndSamplers.clear();
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
    public void addTestElement(TestElement te) {
        log.debug("Add test element into controller: {}", te);
        if (te instanceof Controller) {
            subControllersAndSamplers.add((Controller) te);
        } else if (te instanceof Sampler) {
            GenericController wrapper = new GenericController();
            wrapper.addTestElement(te);
            wrapper.setName(te.getName());
            subControllersAndSamplers.add(wrapper);
        }
        log.debug("List size: {}", subControllersAndSamplers.size());
    }
}

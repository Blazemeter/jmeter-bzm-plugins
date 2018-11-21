package com.blazemeter.jmeter.controller;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.TransactionSampler;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.HashTree;

import java.util.HashSet;
import java.util.Set;

public class TestCompilerParallel extends TestCompiler {
    private final boolean suppressListeners;
    private Set<AbstractTestElement> knownElements = new HashSet<>();

    public TestCompilerParallel(HashTree hashTree, boolean suppressListeners) {
        super(hashTree);
        this.suppressListeners = suppressListeners;
    }

    @Override
    public SamplePackage configureSampler(Sampler sampler) {
        SamplePackage samplePackage = super.configureSampler(sampler);
        addSamplePackage(sampler, samplePackage);
        if (suppressListeners) {
            samplePackage.getSampleListeners().clear();
        }
        return samplePackage;
    }

    private void addSamplePackage(Sampler sampler, SamplePackage samplePackage) {
        if (sampler instanceof AbstractTestElement) {
            knownElements.add((AbstractTestElement) sampler);
        }

        for (Assertion assertion : samplePackage.getAssertions()) {
            if (assertion instanceof AbstractTestElement) {
                knownElements.add((AbstractTestElement) assertion);
            }
        }

        for (ConfigTestElement config : samplePackage.getConfigs()) {
            knownElements.add(config);
        }

        for (PostProcessor postProcessor : samplePackage.getPostProcessors()) {
            if (postProcessor instanceof AbstractTestElement) {
                knownElements.add((AbstractTestElement) postProcessor);
            }
        }

        for (PreProcessor preProcessor : samplePackage.getPreProcessors()) {
            if (preProcessor instanceof AbstractTestElement) {
                knownElements.add((AbstractTestElement) preProcessor);
            }
        }

        for (Timer timer : samplePackage.getTimers()) {
            if (timer instanceof AbstractTestElement) {
                knownElements.add((AbstractTestElement) timer);
            }
        }
    }


    @Override
    public SamplePackage configureTransactionSampler(TransactionSampler transactionSampler) {
        SamplePackage samplePackage = super.configureTransactionSampler(transactionSampler);
        addSamplePackage(transactionSampler, samplePackage);
        if (suppressListeners) {
            samplePackage.getSampleListeners().clear();
        }
        return samplePackage;
    }

    public Set<AbstractTestElement> getKnownElements() {
        return knownElements;
    }

    @Override
    public void addNode(Object node, HashTree subTree) {
        // Override, because in JMeterThread.run().initRun()
        // it will break SamplePackage config
    }

    @Override
    public void subtractNode() {
        // Override, because in JMeterThread.run().initRun()
        // it will break SamplePackage config
    }
}

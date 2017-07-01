package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.TransactionSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jorphan.collections.HashTree;

import java.util.HashSet;
import java.util.Set;

public class TestCompilerParallel extends TestCompiler {
    private Set<Sampler> knownSamplers = new HashSet<>();

    public TestCompilerParallel(HashTree hashTree) {
        super(hashTree);
    }

    @Override
    public SamplePackage configureSampler(Sampler sampler) {
        knownSamplers.add(sampler);
        SamplePackage samplePackage = super.configureSampler(sampler);
        samplePackage.getSampleListeners().clear();
        return samplePackage;
    }

    @Override
    public SamplePackage configureTransactionSampler(TransactionSampler transactionSampler) {
        knownSamplers.add(transactionSampler);
        SamplePackage samplePackage = super.configureTransactionSampler(transactionSampler);
        samplePackage.getSampleListeners().clear();
        return samplePackage;
    }

    public Set<Sampler> getKnownSamplers() {
        return knownSamplers;
    }
}

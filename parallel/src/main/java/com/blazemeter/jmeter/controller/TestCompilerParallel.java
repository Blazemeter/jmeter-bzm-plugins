package com.blazemeter.jmeter.controller;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.TransactionSampler;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestCompilerParallel extends TestCompiler {
    private static final Logger log = LoggerFactory.getLogger(TestCompilerParallel.class);
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
            makeAllPropertiesThreadSafe(config);
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

    private void makeAllPropertiesThreadSafe(ConfigTestElement config) {
        Map<String, JMeterProperty> propMap = getProperties(config);
        for (String key : propMap.keySet()) {
            JMeterProperty property = propMap.get(key);
            if (property instanceof CollectionProperty) {
               synchronizedCollectionProperty((CollectionProperty) property);
            }
        }
    }

    private void synchronizedCollectionProperty(CollectionProperty property) {
        try {
            Field field = CollectionProperty.class.getDeclaredField("value");
            field.setAccessible(true);
            field.set(property, Collections.synchronizedList((List<JMeterProperty>) field.get(property)));
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException e) {
            log.warn("Failed to make synchronized Collection Property", e);
        }
    }

    private Map<String, JMeterProperty> getProperties(ConfigTestElement config) {
        try {
            Field propMapField = AbstractTestElement.class.getDeclaredField("propMap");
            propMapField.setAccessible(true);
            return (Map<String, JMeterProperty>) propMapField.get(config);
        } catch (IllegalAccessException | NoSuchFieldException | ClassCastException e) {
            log.warn("Failed to get propMap from config element", e);
            return Collections.emptyMap();
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
}

package com.blazemeter.jmeter.control;

import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class WeightedSwitchController extends GenericController implements Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();
    public static final String WEIGHTS = "Weights";
    public static final String IS_RANDOM_CHOICE = "IsRandomChoice";
    private boolean chosen = false;
    protected long[] counts = null;
    protected long totalCount = 0;
    protected transient int currentCopy;

    public void setData(PowerTableModel model) {
        CollectionProperty prop = JMeterPluginsUtils.tableModelRowsToCollectionProperty(model, WEIGHTS);
        // log.warn("Set prop from model: " + prop);
        setProperty(prop);
    }

    public CollectionProperty getData() {
        JMeterProperty prop = getProperty(WEIGHTS);
        // log.info("Weights prop: " + prop);
        if (prop instanceof CollectionProperty) {
            return setEnabledSubGroups((CollectionProperty) prop);
        } else {
            log.warn("Returning empty collection");
            return new CollectionProperty();
        }
    }

    public void setIsRandomChoice(boolean value) {
        setProperty(IS_RANDOM_CHOICE, value);
    }

    public boolean isRandomChoice() {
        return getPropertyAsBoolean(IS_RANDOM_CHOICE, false);
    }

    @Override
    public Sampler next() {
        if (chosen) {
            Sampler result = super.next();

            if (result == null || currentCopy != current) {
                reset();
                for (TestElement element : super.getSubControllers()) {
                    if (element instanceof Controller) {
                        resetController((Controller) element);
                    }
                }
                return null;
            }
            return result;
        } else {
            chosen = true;
            choose();
            return super.next();
        }
    }

    private void resetController(Controller element) {
        if (element instanceof TransactionController) {
            if (element.getPropertyAsBoolean("TransactionController.parent")) {
                // we should skip org.apache.jmeter.control.TransactionController.triggerEndOfLoop(),
                // because org.apache.jmeter.control.TransactionController.transactionSampler is NULL,
                // but we should call GenericController.triggerEndOfLoop() or GenericController.reInitialize()
                // for reInit this controller
                reInitializeController((TransactionController) element);
                return;
            } else {
                // when currentCopy != current && result != null we should
                // set org.apache.jmeter.control.TransactionController.res = null
                // because if it is not null org.apache.jmeter.control.TransactionController.triggerEndOfLoop()
                // will generate new parent Sample
                nullifyRes((TransactionController) element);
            }
        } else if (element instanceof GenericController) {
            // reset all nested controllers
            GenericController ctrl = (GenericController) element;
            List<TestElement> subControllersAndSamplers = getSubControllersAndSamplers(ctrl);
            for (TestElement te : subControllersAndSamplers) {
                if (te instanceof Controller) {
                    resetController((Controller) te);
                }
            }
        }
        element.triggerEndOfLoop();
    }

    private List<TestElement> getSubControllersAndSamplers(GenericController ctrl) {
        try {
            Field subControllersAndSamplers = GenericController.class.getDeclaredField("subControllersAndSamplers");
            subControllersAndSamplers.setAccessible(true);
            return (List<TestElement>) subControllersAndSamplers.get(ctrl);
        } catch (Throwable ex) {
            log.warn("Failed to get SubControllers And Samplers", ex);
            return Collections.emptyList();
        }
    }

    private void reInitializeController(TransactionController element) {
        try {
            Method reInitialize = GenericController.class.getDeclaredMethod("reInitialize");
            reInitialize.setAccessible(true);
            reInitialize.invoke(element);
        } catch (Throwable ex) {
            log.warn("Failed to reInitialize TransactionController", ex);
        }
    }

    private void nullifyRes(TransactionController element) {
        try {
            Field res = TransactionController.class.getDeclaredField("res");
            res.setAccessible(true);
            res.set(element, null);
        } catch (Throwable ex) {
            log.warn("Failed to nullify TransactionController.res field", ex);
        }
    }

    private void choose() {
        CollectionProperty data = removeDisableSubGroups(getData());

        double[] weights = getWeights(data);
        int index = isRandomChoice() ? getRandomIndex(weights) : getIndex(weights);
        current = currentCopy = index;
    }

    private int getRandomIndex(double[] weights) {
        // Compute the total weight of all items together
        double totalWeight = 0.0d;
        for (double i : weights) {
            totalWeight += i;
        }

        // Now choose a random item
        int randomIndex = 0;
        double random = Math.random() * totalWeight;
        for (int i = 0; i < weights.length; i++) {
            random -= weights[i];
            if (random <= 0.0d) {
                randomIndex = i;
                break;
            }
        }
        return randomIndex;
    }

    private int getIndex(double[] weights) {
        if (counts == null) {
            log.debug("Creating array: " + weights.length);
            counts = new long[weights.length];
        }

        double maxDiff = Double.MIN_VALUE;
        int maxDiffIndex = Integer.MIN_VALUE;
        for (int n = 0; n < weights.length; n++) {
            double factWeight = totalCount > 0 ? ((double) counts[n] / totalCount) : 0;
            double diff = weights[n] - factWeight;
            if (diff > maxDiff) {
                maxDiff = diff;
                maxDiffIndex = n;
            }
        }

        if (maxDiffIndex == Integer.MIN_VALUE) {
            for (int n = 0; n < weights.length; n++) {
                double diff = weights[n];
                if (diff > maxDiff) {
                    maxDiff = diff;
                    maxDiffIndex = n;
                }
            }
        }


        totalCount++;
        counts[maxDiffIndex]++;
        return maxDiffIndex;
    }

    private CollectionProperty removeDisableSubGroups(CollectionProperty data) {
        CollectionProperty result = new CollectionProperty();
        for (int i = 0; i < data.size(); i++) {
            JMeterProperty property = data.get(i);
            if (property instanceof CollectionProperty &&
                    ((CollectionProperty) property).size() == 3 &&
                    "true".equals(((CollectionProperty) property).get(2).getStringValue())) {
                result.addProperty(property);
            }
        }
        return result;
    }

    private CollectionProperty setEnabledSubGroups(CollectionProperty data) {
        for (int i = 0; i < data.size(); i++) {
            JMeterProperty property = data.get(i);
            if (property instanceof CollectionProperty) {
                CollectionProperty prop = (CollectionProperty) property;
                if (prop.size() == 2) {
                    prop.addItem("true");
                }
            }
        }

        setProperty(data);
        return data;
    }

    private double[] getWeights(CollectionProperty data) {
        long sum = 0;
        double[] weights = new double[data.size()];
        for (int n = 0; n < data.size(); n++) {
            CollectionProperty row = (CollectionProperty) data.get(n);
            weights[n] = Long.parseLong(row.get(1).getStringValue());
            sum += weights[n];
        }

        for (int n = 0; n < weights.length; n++) {
            weights[n] /= sum;
        }
        //log.info("Weights: " + Arrays.toString(weights));
        return weights;
    }


    public void reset() {
        this.chosen = false;
        // reset child WSC
        for (TestElement controller : this.getSubControllers()) {
            if (controller instanceof WeightedSwitchController) {
                ((WeightedSwitchController) controller).reset();
            }
        }
    }
}

package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.testelement.TestElement;

import java.awt.*;

public class ParallelControllerGui extends LogicControllerGui {
    private static final String MSG = "All direct child elements of this controller" +
            " will be executed as parallel.";

    public ParallelControllerGui() {
        super();
        Label lbl = new Label(MSG, Label.CENTER);
        add(lbl);
    }

    @Override
    public String getStaticLabel() {
        return "bzm - Parallel Controller";
    }

    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public TestElement createTestElement() {
        ParallelSampler te = new ParallelSampler();
        modifyTestElement(te);
        return te;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);
    }
}

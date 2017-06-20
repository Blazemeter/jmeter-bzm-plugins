package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.testelement.TestElement;

public class ParallelControllerGui extends AbstractControllerGui {
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
        ParallelController te = new ParallelController();
        modifyTestElement(te);
        return te;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);
    }
}

package com.blazemeter.jmeter.controller;

import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.testelement.TestElement;

public class ParallelControllerGui extends AbstractControllerGui {
    @Override
    public String getLabelResource() {
        return getClass().getCanonicalName();
    }

    @Override
    public TestElement createTestElement() {
        return new ParallelController();
    }

    @Override
    public void modifyTestElement(TestElement testElement) {
        // TODO?
    }
}

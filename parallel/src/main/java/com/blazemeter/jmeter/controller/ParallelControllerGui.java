package com.blazemeter.jmeter.controller;

import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class ParallelControllerGui extends LogicControllerGui {
    private static final String MSG = "All direct child elements of this controller" +
            " will be executed as parallel.";
    public static final String WIKIPAGE = "https://github.com/Blazemeter/jmeter-bzm-plugins/tree/master/jmeter-parallel-http/Parallel.md#parallel-controller";

    private JCheckBox generateParentSamples;

    public ParallelControllerGui() {
        super();
        init();
    }


    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), WIKIPAGE), BorderLayout.NORTH);

        Container topPanel = makeTitlePanel();

        add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new HorizontalPanel();

        JLabel lbl = new JLabel(MSG, JLabel.CENTER);
        topPanel.add(lbl);

        generateParentSamples = new JCheckBox();
        mainPanel.add(generateParentSamples);
        mainPanel.add(new JLabel("Generate parent sample", JLabel.RIGHT));

        topPanel.add(mainPanel);
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
        if (te instanceof ParallelSampler) {
            ParallelSampler parallelSampler = (ParallelSampler) te;
            parallelSampler.setGenerateParent(this.generateParentSamples.isSelected());
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (element instanceof ParallelSampler) {
            generateParentSamples.setSelected(((ParallelSampler) element).getGenerateParent());
        }
    }
}

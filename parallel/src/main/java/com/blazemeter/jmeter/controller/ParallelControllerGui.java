package com.blazemeter.jmeter.controller;

import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParallelControllerGui extends LogicControllerGui {
    private static final String MSG = "All direct child elements of this controller" +
            " will be executed as parallel.";
    public static final String WIKIPAGE = "https://github.com/Blazemeter/jmeter-bzm-plugins/tree/master/jmeter-parallel-http/Parallel.md#parallel-controller";

    private JCheckBox generateParentSamples;

    private JCheckBox limitMaxThreadNumber;

    private JSpinner maxThreadNumber;
    private SpinnerNumberModel model;

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

        JPanel parentSamplePanel = new HorizontalPanel();

        JLabel lbl = new JLabel(MSG, JLabel.CENTER);
        topPanel.add(lbl);

        generateParentSamples = new JCheckBox();
        parentSamplePanel.add(generateParentSamples);
        parentSamplePanel.add(new JLabel("Generate parent sample", JLabel.RIGHT));

        JPanel limitPanel = new HorizontalPanel();
        limitMaxThreadNumber = new JCheckBox();
        limitPanel.add(limitMaxThreadNumber);
        limitPanel.add(new JLabel("Limit max thread number", JLabel.RIGHT));
        limitMaxThreadNumber.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxThreadNumber.setEnabled(limitMaxThreadNumber.isSelected());
            }
        });


        JPanel threadsPanel = new HorizontalPanel();
        model = new SpinnerNumberModel(6, 1, 10, 1);
        maxThreadNumber = new JSpinner(model);
        maxThreadNumber.setEnabled(false);
        threadsPanel.add(new JLabel("Max threads: ", JLabel.RIGHT));
        threadsPanel.add(maxThreadNumber);
        HorizontalPanel limitThreadsWrap = new HorizontalPanel();
        limitThreadsWrap.add(limitPanel);
        limitThreadsWrap.add(threadsPanel);

        topPanel.add(parentSamplePanel);
        topPanel.add(limitThreadsWrap);
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
            parallelSampler.setMaxThreadNumber(model.getNumber().intValue());
            parallelSampler.setGenerateParent(this.generateParentSamples.isSelected());
            parallelSampler.setLimitMaxThreadNumber(this.limitMaxThreadNumber.isSelected());
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (element instanceof ParallelSampler) {
            model.setValue(((ParallelSampler) element).getMaxThreadNumber());
            generateParentSamples.setSelected(((ParallelSampler) element).getGenerateParent());
            limitMaxThreadNumber.setSelected(((ParallelSampler) element).getLimitMaxThreadNumber());
            maxThreadNumber.setEnabled(limitMaxThreadNumber.isSelected());
        }
    }
}

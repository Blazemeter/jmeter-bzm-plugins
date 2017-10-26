package com.blazemeter.jmeter;

import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.BrowseAction;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RandomCSVDataSetConfigGui extends AbstractConfigGui {

    public static final String WIKIPAGE = "RandomCSVDataSetConfig";

    private JTextField filenameField;
    private JButton browseButton;

    private JTextField fileEncodingField;
    private JTextField variableNamesField;
    private JTextField delimiterField;

    private JCheckBox isRandomOrderCheckBox;
    private JCheckBox isIgnoreFirstLineCheckBox;
    private JCheckBox isAllowQuotedDataCheckBox;
    private JCheckBox isRewindOnTheEndCheckBox;
    private JCheckBox isIndependentListCheckBox;

    private JButton checkButton;
    private JTextArea checkArea;

    public RandomCSVDataSetConfigGui() {
        initGui();
        initGuiValues();
    }

    private void initGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        Container topPanel = makeTitlePanel();

        add(JMeterPluginsUtils.addHelpLinkToPanel(topPanel, WIKIPAGE), BorderLayout.NORTH);
        add(topPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Filename: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, filenameField = new JTextField(20));
        addToPanel(mainPanel, labelConstraints, 2, row, browseButton = new JButton("Browse..."));
        row++;
        GuiBuilderHelper.strechItemToComponent(filenameField, browseButton);

        editConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        labelConstraints.insets = new java.awt.Insets(2, 0, 0, 0);

        browseButton.addActionListener(new BrowseAction(filenameField, true));

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("File encoding: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, fileEncodingField = new JTextField(20));
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Delimiter (use '\\t' for tab): ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, delimiterField = new JTextField(20));
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Variable names (comma-delimited): ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, variableNamesField = new JTextField(20));
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Random order: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isRandomOrderCheckBox = new JCheckBox());
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Ignore first line: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isIgnoreFirstLineCheckBox = new JCheckBox());
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Allow quoted data: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isAllowQuotedDataCheckBox = new JCheckBox());
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Rewind on end of list: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isRewindOnTheEndCheckBox = new JCheckBox());
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Independent list per thread: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isIndependentListCheckBox = new JCheckBox());
        row++;

        editConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        labelConstraints.insets = new java.awt.Insets(4, 0, 0, 2);

        addToPanel(mainPanel, labelConstraints, 0, row, checkButton = new JButton("Test Random CSV Config"));

        labelConstraints.insets = new java.awt.Insets(4, 0, 0, 0);

        checkArea = new JTextArea();
        addToPanel(mainPanel, editConstraints, 1, row, GuiBuilderHelper.getTextAreaScrollPaneContainer(checkArea, 10));
//        checkButton.addActionListener(new TestDirectoryListingAction(this));
        checkArea.setEditable(false);
        checkArea.setOpaque(false);


        JPanel container = new JPanel(new BorderLayout());
        container.add(mainPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
    }

    private void initGuiValues() {
//        sourceDirectoryField.setText("");
//        destinationVariableField.setText("");
//        isUseFullPathCheckBox.setSelected(false);
//        isRandomOrderCheckBox.setSelected(false);
//        isRecursiveListing.setSelected(false);
//        isRewindOnTheEndCheckBox.setSelected(true);
//        isIndependentListCheckBox.setSelected(false);
//        isReReadDirectoryCheckBox.setSelected(false);
//        checkArea.setText("");
    }

    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row, JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }

    @Override
    public String getLabelResource() {
        return "random_csv_data_set_config";
    }

    @Override
    public String getStaticLabel() {
        return "bmz - Random CSV Data Set Config";
    }

    @Override
    public TestElement createTestElement() {
        RandomCSVDataSetConfig element = new RandomCSVDataSetConfig();
        modifyTestElement(element);
        return element;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if (element instanceof RandomCSVDataSetConfig) {
            RandomCSVDataSetConfig randomCSV = (RandomCSVDataSetConfig) element;
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (element instanceof RandomCSVDataSetConfig) {
            RandomCSVDataSetConfig randomCSV = (RandomCSVDataSetConfig) element;

//            sourceDirectoryField.setText(directoryListingConfig.getSourceDirectory());
//            destinationVariableField.setText(directoryListingConfig.getDestinationVariableName());
//            isUseFullPathCheckBox.setSelected(directoryListingConfig.getUseFullPath());
//            isRandomOrderCheckBox.setSelected(directoryListingConfig.getRandomOrder());
//            isRecursiveListing.setSelected(directoryListingConfig.getRecursiveListing());
//            isRewindOnTheEndCheckBox.setSelected(directoryListingConfig.getRewindOnTheEnd());
//            isReReadDirectoryCheckBox.setSelected(directoryListingConfig.getReReadDirectoryOnTheEndOfList());
//            isIndependentListCheckBox.setSelected(directoryListingConfig.getIndependentListPerThread());
//
//            isReReadDirectoryCheckBox.setEnabled(isRewindOnTheEndCheckBox.isSelected());

        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initGuiValues();
    }

    public JTextArea getCheckArea() {
        return checkArea;
    }
}

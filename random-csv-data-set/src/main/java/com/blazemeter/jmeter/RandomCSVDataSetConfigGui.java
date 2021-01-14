package com.blazemeter.jmeter;

import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.BrowseAction;
import kg.apc.jmeter.gui.GuiBuilderHelper;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;

import javax.swing.*;
import java.awt.*;

public class RandomCSVDataSetConfigGui extends AbstractConfigGui {

    // TODO: use full URL and change cmn version to 0.6 after it has been released
    public static final String WIKIPAGE = "RandomCSVDataSetConfig";

    private JTextField filenameField;

    private JTextField fileEncodingField;
    private JTextField variableNamesField;
    private JTextField delimiterField;

    private JCheckBox isRandomOrderCheckBox;
    private JCheckBox isIgnoreFirstLineCheckBox;
    private JCheckBox isRewindOnTheEndCheckBox;
    private JCheckBox isIndependentListCheckBox;

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
        JButton browseButton;
        addToPanel(mainPanel, labelConstraints, 2, row, browseButton = new JButton("Browse..."));
        row++;
        GuiBuilderHelper.strechItemToComponent(filenameField, browseButton);

        editConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        labelConstraints.insets = new java.awt.Insets(2, 0, 0, 0);

        browseButton.addActionListener(new BrowseAction(filenameField, false));

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

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Rewind on end of list: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isRewindOnTheEndCheckBox = new JCheckBox());
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("First line is CSV header: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isIgnoreFirstLineCheckBox = new JCheckBox());
        row++;

        addToPanel(mainPanel, labelConstraints, 0, row, new JLabel("Independent list per thread: ", JLabel.RIGHT));
        addToPanel(mainPanel, editConstraints, 1, row, isIndependentListCheckBox = new JCheckBox());
        row++;

        editConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        labelConstraints.insets = new java.awt.Insets(4, 0, 0, 2);

        JButton checkButton;
        addToPanel(mainPanel, labelConstraints, 0, row, checkButton = new JButton("Test CSV Reading"));

        labelConstraints.insets = new java.awt.Insets(4, 0, 0, 0);

        checkArea = new JTextArea();
        addToPanel(mainPanel, editConstraints, 1, row, GuiBuilderHelper.getTextAreaScrollPaneContainer(checkArea, 10));
        checkButton.addActionListener(new TestRandomCSVAction(this));
        checkArea.setEditable(false);
        checkArea.setOpaque(false);


        JPanel container = new JPanel(new BorderLayout());
        container.add(mainPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
    }

    private void initGuiValues() {
        filenameField.setText("");
        fileEncodingField.setText("UTF-8");
        delimiterField.setText(",");
        variableNamesField.setText("");

        isRandomOrderCheckBox.setSelected(true);
        isIgnoreFirstLineCheckBox.setSelected(false);
        isRewindOnTheEndCheckBox.setSelected(true);
        isIndependentListCheckBox.setSelected(false);

        checkArea.setText("");
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
        return "bzm - Random CSV Data Set Config";
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

            randomCSV.setFilename(this.filenameField.getText());
            randomCSV.setFileEncoding(this.fileEncodingField.getText());
            randomCSV.setDelimiter(this.delimiterField.getText());
            randomCSV.setVariableNames(this.variableNamesField.getText());

            randomCSV.setRandomOrder(this.isRandomOrderCheckBox.isSelected());
            randomCSV.setIgnoreFirstLine(this.isIgnoreFirstLineCheckBox.isSelected());
            randomCSV.setRewindOnTheEndOfList(this.isRewindOnTheEndCheckBox.isSelected());
            randomCSV.setIndependentListPerThread(this.isIndependentListCheckBox.isSelected());
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);

        if (element instanceof RandomCSVDataSetConfig) {
            RandomCSVDataSetConfig randomCSV = (RandomCSVDataSetConfig) element;

            filenameField.setText(randomCSV.getFilename());
            fileEncodingField.setText(randomCSV.getFileEncoding());
            delimiterField.setText(randomCSV.getDelimiter());
            variableNamesField.setText(randomCSV.getVariableNames());

            isRandomOrderCheckBox.setSelected(randomCSV.isRandomOrder());
            isIgnoreFirstLineCheckBox.setSelected(randomCSV.isIgnoreFirstLine());
            isRewindOnTheEndCheckBox.setSelected(randomCSV.isRewindOnTheEndOfList());
            isIndependentListCheckBox.setSelected(randomCSV.isIndependentListPerThread());

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

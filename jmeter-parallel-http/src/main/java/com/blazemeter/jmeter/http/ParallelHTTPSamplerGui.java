package com.blazemeter.jmeter.http;

import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.gui.ButtonPanelAddCopyRemove;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;

public class ParallelHTTPSamplerGui extends AbstractSamplerGui implements TableModelListener, CellEditorListener {

    public static final String WIKIPAGE = "https://github.com/Blazemeter/jmeter-bzm-plugins/tree/master/jmeter-parallel-http/Parallel.md#parallel-sampler";
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static String[] defaultValues = new String[]{
            "",
    };
    protected PowerTableModel tableModel;
    protected JTable grid;
    protected ButtonPanelAddCopyRemove buttons;

    public ParallelHTTPSamplerGui() {
        super();
        init();
    }

    protected final void init() {
        setBorder(makeBorder());
        setLayout(new BorderLayout());
        add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), WIKIPAGE), BorderLayout.NORTH);
        JPanel containerPanel = new VerticalPanel();

        containerPanel.add(createParamsPanel(), BorderLayout.CENTER);
        add(containerPanel, BorderLayout.CENTER);
    }

    private JPanel createParamsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("URLs to Retrieve"));
        panel.setPreferredSize(new Dimension(200, 200));

        JScrollPane scroll = new JScrollPane(createGrid());
        scroll.setPreferredSize(scroll.getMinimumSize());
        panel.add(scroll, BorderLayout.CENTER);
        buttons = new ButtonPanelAddCopyRemove(grid, tableModel, defaultValues);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JTable createGrid() {
        grid = new JTable();
        grid.getDefaultEditor(String.class).addCellEditorListener(this);
        createTableModel();
        grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        grid.setMinimumSize(new Dimension(200, 100));
        return grid;
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return JMeterPluginsUtils.prefixLabel("Parallel HTTP Requests");
    }

    @Override
    public TestElement createTestElement() {
        ParallelHTTPSampler tg = new ParallelHTTPSampler();
        modifyTestElement(tg);
        tg.setComment(JMeterPluginsUtils.getWikiLinkText(WIKIPAGE));
        return tg;
    }

    @Override
    public void modifyTestElement(TestElement tg) {
        super.configureTestElement(tg);

        if (grid.isEditing()) {
            grid.getCellEditor().stopCellEditing();
        }

        if (tg instanceof ParallelHTTPSampler) {
            ParallelHTTPSampler utg = (ParallelHTTPSampler) tg;
            CollectionProperty rows = JMeterPluginsUtils.tableModelRowsToCollectionProperty(tableModel, ParallelHTTPSampler.DATA_PROPERTY);
            utg.setData(rows);
        }
    }

    @Override
    public void configure(TestElement tg) {
        super.configure(tg);
        ParallelHTTPSampler utg = (ParallelHTTPSampler) tg;
        JMeterProperty threadValues = utg.getData();
        if (threadValues instanceof NullProperty) {
            log.warn("Received null property instead of collection");
            return;
        }

        CollectionProperty columns = (CollectionProperty) threadValues;

        tableModel.removeTableModelListener(this);
        JMeterPluginsUtils.collectionPropertyToTableModelRows(columns, tableModel);
        tableModel.addTableModelListener(this);
        buttons.checkDeleteButtonStatus();
        updateUI();
    }

    @Override
    public void updateUI() {
        super.updateUI();

        if (tableModel != null) {
            ParallelHTTPSampler utgForPreview = new ParallelHTTPSampler();
            utgForPreview.setData(JMeterPluginsUtils.tableModelRowsToCollectionPropertyEval(tableModel, ParallelHTTPSampler.DATA_PROPERTY));
        }
    }

    private void createTableModel() {
        tableModel = new PowerTableModel(ParallelHTTPSampler.columnIdentifiers, ParallelHTTPSampler.columnClasses);
        tableModel.addTableModelListener(this);
        grid.setModel(tableModel);
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        updateUI();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        // no action needed
    }

    @Override
    public void clearGui() {
        super.clearGui();
        tableModel.clearData();
        tableModel.fireTableDataChanged();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        updateUI();
    }
}

package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordInputPanel extends JPanel implements ActionListener {

  private static final long serialVersionUID = -6184904133375045201L;
  private static final Logger LOG = LoggerFactory.getLogger(CoordInputPanel.class);
  private static final String ADD_ACTION = "add";
  private static final String ADD_FROM_CLIPBOARD_ACTION = "addFromClipboard";
  private static final String DELETE_ACTION = "delete";
  private static final String UP_ACTION = "up";
  private static final String DOWN_ACTION = "down";
  private static final String CLIPBOARD_LINE_DELIMITERS = "\n";
  private static final String CLIPBOARD_ARG_DELIMITERS = "\t";

  private transient ObjectTableModel tableModel;
  private transient JTable table;
  private JButton addButton;
  private JButton addFromClipboardButton;
  private JButton deleteButton;
  private JButton upButton;
  private JButton downButton;

  public CoordInputPanel() {
    setLayout(new BorderLayout());
    add(makeMainPanel(), BorderLayout.CENTER);
    add(makeButtonPanel(), BorderLayout.SOUTH);
    table.revalidate();
  }

  private static int getNumberOfVisibleRows(JTable table) {
    Rectangle vr = table.getVisibleRect();
    int first = table.rowAtPoint(vr.getLocation());
    vr.translate(0, vr.height);
    return table.rowAtPoint(vr.getLocation()) - first;
  }

  private Component makeMainPanel() {
    initializeTableModel();
    table = SwingUtils.createComponent("table", new JTable(tableModel));
    table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer() {
      @Override
      protected String getText(Object value, int row, int column) {
        return (value == null) ? "" : value.toString();
      }
    });
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JMeterUtils.applyHiDPI(table);
    JScrollPane pane = new JScrollPane(table);
    pane.setPreferredSize(pane.getMinimumSize());
    return pane;
  }

  private void initializeTableModel() {
    if (tableModel == null) {
      tableModel = new ObjectTableModel(new String[]{"Row", "Column", "Value"},
          CoordInputRowGUI.class,
          new Functor[]{new Functor("getRow"), new Functor("getColumn"), new Functor("getInput")},
          new Functor[]{new Functor("setRow"), new Functor("setColumn"), new Functor("setInput")},
          new Class[]{Integer.class, Integer.class, String.class});
    }
  }

  private JPanel makeButtonPanel() {

    addButton = SwingUtils
        .createComponent("addButton", new JButton(JMeterUtils.getResString("add")));
    addButton.setActionCommand(ADD_ACTION);
    addButton.setEnabled(true);

    addFromClipboardButton = SwingUtils.createComponent("addFromClipboardButton",
        new JButton(JMeterUtils.getResString("add_from_clipboard")));
    addFromClipboardButton.setActionCommand("addFromClipboardButton");
    addFromClipboardButton.setEnabled(true);

    deleteButton = SwingUtils
        .createComponent("deleteButton", new JButton(JMeterUtils.getResString("delete")));
    deleteButton.setActionCommand(DELETE_ACTION);

    upButton = SwingUtils.createComponent("upButton", new JButton(JMeterUtils.getResString("up")));
    upButton.setActionCommand(UP_ACTION);

    downButton = SwingUtils
        .createComponent("downButton", new JButton(JMeterUtils.getResString("down")));
    downButton.setActionCommand(DOWN_ACTION);

    updateEnabledButtons();

    JPanel buttonPanel = SwingUtils.createComponent("buttonPanel", new JPanel());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    addButton.addActionListener(this);
    addFromClipboardButton.addActionListener(this);
    deleteButton.addActionListener(this);
    upButton.addActionListener(this);
    downButton.addActionListener(this);
    buttonPanel.add(addButton);
    buttonPanel.add(addFromClipboardButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(upButton);
    buttonPanel.add(downButton);
    return buttonPanel;
  }

  private void updateEnabledButtons() {
    int rowCount = tableModel.getRowCount();
    deleteButton.setEnabled(isEnabled() && rowCount != 0);
    upButton.setEnabled(isEnabled() && rowCount > 1);
    downButton.setEnabled(isEnabled() && rowCount > 1);
  }

  public TestElement createTestElement() {
    Inputs inputs = new Inputs();
    modifyTestElement(inputs);
    return inputs;
  }

  private void modifyTestElement(TestElement element) {
    GuiUtils.stopTableEditing(table);
    if (element instanceof Inputs) {
      Inputs inputs = (Inputs) element;
      inputs.clear();
      @SuppressWarnings("unchecked")
      Iterator<CoordInputRowGUI> modelData = (Iterator<CoordInputRowGUI>) tableModel.iterator();
      while (modelData.hasNext()) {
        CoordInputRowGUI input = modelData.next();
        if (!StringUtils.isEmpty(input.getInput())) {
          inputs.addCoordInput(input);
        }
      }
    }
  }

  public void configure(TestElement el) {
    if (el instanceof Inputs) {
      tableModel.clearData();
      for (JMeterProperty jMeterProperty : (Inputs) el) {
        CoordInputRowGUI input = (CoordInputRowGUI) jMeterProperty.getObjectValue();
        tableModel.addRow(input);
      }
    }
    updateEnabledButtons();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case ADD_ACTION:
        addArgument();
        break;
      case ADD_FROM_CLIPBOARD_ACTION:
        addFromClipboard();
        break;
      case DELETE_ACTION:
        deleteArgument();
        break;
      case UP_ACTION:
        moveUp();
        break;
      case DOWN_ACTION:
        moveDown();
        break;
      default:
        throw new UnsupportedOperationException(action);
    }
  }

  private void addArgument() {
    // If a table cell is being edited, we should accept the current value
    // and stop the editing before adding a new row.
    GuiUtils.stopTableEditing(table);

    tableModel.addRow(new CoordInputRowGUI());

    updateEnabledButtons();

    // Highlight (select) and scroll to the appropriate row.
    int rowToSelect = tableModel.getRowCount() - 1;
    table.setRowSelectionInterval(rowToSelect, rowToSelect);
    table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
  }

  private void addFromClipboard() {
    GuiUtils.stopTableEditing(table);
    int rowCount = table.getRowCount();
    try {
      String clipboardContent = GuiUtils.getPastedText();
      if (clipboardContent == null) {
        return;
      }
      String[] clipboardLines = clipboardContent.split(CLIPBOARD_LINE_DELIMITERS);
      for (String clipboardLine : clipboardLines) {
        String[] clipboardCols = clipboardLine.split(CLIPBOARD_ARG_DELIMITERS);
        if (clipboardCols.length > 0) {
          CoordInputRowGUI input = createArgumentFromClipboard(clipboardCols);
          tableModel.addRow(input);
        }
      }
      if (table.getRowCount() > rowCount) {
        updateEnabledButtons();

        // Highlight (select) and scroll to the appropriate rows.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowCount, rowToSelect);
        table.scrollRectToVisible(table.getCellRect(rowCount, 0, true));
      }
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this,
          "Could not add read arguments from clipboard:\n" + ioe.getLocalizedMessage(), "Error",
          JOptionPane.ERROR_MESSAGE);
    } catch (UnsupportedFlavorException ufe) {
      JOptionPane
          .showMessageDialog(this,
              "Could not add retrieve " + DataFlavor.stringFlavor.getHumanPresentableName()
                  + " from clipboard" + ufe.getLocalizedMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private CoordInputRowGUI createArgumentFromClipboard(String[] clipboardCols) {
    CoordInputRowGUI argument = new CoordInputRowGUI();
    argument.setInput(clipboardCols[0]);
    if (clipboardCols.length > 1) {
      argument.setColumn(parseCoordIndex(clipboardCols[1]));
      if (clipboardCols.length > 2) {
        argument.setRow(parseCoordIndex(clipboardCols[2]));
      }
    }
    return argument;
  }

  private int parseCoordIndex(String val) {
    try {
      return Integer.valueOf(val);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid value ({}) for coordinate index.", val);
      return 1;
    }
  }

  private void deleteArgument() {
    GuiUtils.cancelEditing(table);

    int[] rowsSelected = table.getSelectedRows();
    int anchorSelection = table.getSelectionModel().getAnchorSelectionIndex();
    table.clearSelection();
    if (rowsSelected.length > 0) {
      for (int i = rowsSelected.length - 1; i >= 0; i--) {
        tableModel.removeRow(rowsSelected[i]);
      }

      // Table still contains one or more rows, so highlight (select)
      // the appropriate one.
      if (tableModel.getRowCount() > 0) {
        if (anchorSelection >= tableModel.getRowCount()) {
          anchorSelection = tableModel.getRowCount() - 1;
        }
        table.setRowSelectionInterval(anchorSelection, anchorSelection);
      }

      updateEnabledButtons();
    }
  }

  private void moveUp() {
    // get the selected rows before stopping editing
    // or the selected rows will be unselected
    int[] rowsSelected = table.getSelectedRows();
    GuiUtils.stopTableEditing(table);

    if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
      table.clearSelection();
      for (int rowSelected : rowsSelected) {
        tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
      }

      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] - 1);
    }
  }

  private void scrollToRowIfNotVisible(int rowIndx) {
    if (table.getParent() instanceof JViewport) {
      Rectangle visibleRect = table.getVisibleRect();
      final int cellIndex = 0;
      Rectangle cellRect = table.getCellRect(rowIndx, cellIndex, false);
      if (visibleRect.y > cellRect.y) {
        table.scrollRectToVisible(cellRect);
      } else {
        Rectangle rect2 = table
            .getCellRect(rowIndx + getNumberOfVisibleRows(table), cellIndex, true);
        int width = rect2.y - cellRect.y;
        table.scrollRectToVisible(
            new Rectangle(cellRect.x, cellRect.y, cellRect.width, cellRect.height + width));
      }
    }
  }

  private void moveDown() {
    // get the selected rows before stopping editing
    // or the selected rows will be unselected
    int[] rowsSelected = table.getSelectedRows();
    GuiUtils.stopTableEditing(table);

    if (rowsSelected.length > 0
        && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
      table.clearSelection();
      for (int i = rowsSelected.length - 1; i >= 0; i--) {
        int rowSelected = rowsSelected[i];
        tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
      }
      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] + 1);
    }
  }

  public void clear() {
    GuiUtils.stopTableEditing(table);
    tableModel.clearData();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    table.setEnabled(enabled);
    addButton.setEnabled(enabled);
    addFromClipboardButton.setEnabled(enabled);
    updateEnabledButtons();
  }

}

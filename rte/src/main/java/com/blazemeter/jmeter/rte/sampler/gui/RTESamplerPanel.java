package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.sampler.Mode;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

public class RTESamplerPanel extends JPanel {

  private static final long serialVersionUID = 4739160923223292835L;
  private static final int INDEX_WIDTH = 30;
  private static final int TIME_WIDTH = 60;
  private static final String TIMEOUT_LABEL = "Timeout (millis): ";

  private ButtonGroup modesGroup = new ButtonGroup();
  private Map<Mode, JRadioButton> modes = new EnumMap<>(Mode.class);
  private final JPanel requestPanel;
  private CoordInputPanel payloadPanel;
  private ButtonGroup actionsGroup = new ButtonGroup();
  private Map<Action, JRadioButton> actions = new EnumMap<>(Action.class);
  private final JPanel waitPanel;
  private JPanel waitSyncPanel;
  private JCheckBox waitSync = SwingUtils.createComponent("waitSync", new JCheckBox("Sync?"));
  private JTextField waitSyncTimeout = SwingUtils
      .createComponent("waitSyncTimeout", new JTextField());
  private JPanel waitCursorPanel;
  private JCheckBox waitCursor = SwingUtils.createComponent("waitCursor", new JCheckBox("Cursor?"));
  private JTextField waitCursorRow = SwingUtils.createComponent("waitCursorRow", new JTextField());
  private JTextField waitCursorColumn = SwingUtils
      .createComponent("waitCursorColumn", new JTextField());
  private JTextField waitCursorTimeout = SwingUtils
      .createComponent("waitCursorTimeout", new JTextField());
  private JPanel waitSilentPanel;
  private JCheckBox waitSilent = SwingUtils.createComponent("waitSilent", new JCheckBox("Silent?"));
  private JTextField waitSilentTime = SwingUtils
      .createComponent("waitSilentTime", new JTextField());
  private JTextField waitSilentTimeout = SwingUtils
      .createComponent("waitSilentTimeout", new JTextField());
  private JPanel waitTextPanel;
  private JCheckBox waitText = SwingUtils.createComponent("waitText", new JCheckBox("Text?"));
  private JTextField waitTextRegex = SwingUtils.createComponent("waitTextRegex", new JTextField());
  private JTextField waitTextTimeout = SwingUtils
      .createComponent("waitTextTimeout", new JTextField());
  private JTextField waitTextAreaTop = SwingUtils
      .createComponent("waitTextAreaTop", new JTextField());
  private JTextField waitTextAreaLeft = SwingUtils
      .createComponent("waitTextAreaLeft", new JTextField());
  private JTextField waitTextAreaBottom = SwingUtils
      .createComponent("waitTextAreaBottom", new JTextField());
  private JTextField waitTextAreaRight = SwingUtils
      .createComponent("waitTextAreaRight", new JTextField());

  public RTESamplerPanel() {
    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateGaps(true);
    this.setLayout(layout);

    JPanel modePanel = buildModePanel();
    requestPanel = buildRequestPanel();
    waitPanel = buildWaitsPanel();

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(modePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            Short.MAX_VALUE)
        .addComponent(requestPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            Short.MAX_VALUE)
        .addComponent(waitPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            Short.MAX_VALUE));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(modePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(requestPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.DEFAULT_SIZE)
        .addComponent(waitPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.DEFAULT_SIZE)
    );
  }

  private JPanel buildModePanel() {
    JPanel panel = SwingUtils.createComponent("modePanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Mode"));
    panel.setLayout(new FlowLayout(FlowLayout.LEFT));

    Arrays.stream(Mode.values()).forEach(t -> {
      JRadioButton r = SwingUtils.createComponent(t.toString(), new JRadioButton(t.getLabel()));
      r.setActionCommand(t.name());
      panel.add(r);
      modes.put(t, r);
      modesGroup.add(r);
    });

    modes.get(Mode.SEND_INPUT).addItemListener(e -> {
      requestPanel.setVisible(e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    modes.get(Mode.DISCONNECT).addItemListener(e -> {
      waitPanel.setVisible(e.getStateChange() != ItemEvent.SELECTED);
      validate();
      repaint();
    });

    return panel;
  }

  private JPanel buildRequestPanel() {
    JPanel panel = SwingUtils.createComponent("requestPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("RTE Message"));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    panel.setLayout(layout);

    JLabel payloadLabel = SwingUtils.createComponent("payloadLabel", new JLabel("Payload: "));
    payloadPanel = SwingUtils.createComponent("payloadPanel", new CoordInputPanel());
    JPanel actionsPanel = buildActionsPanel();

    JLabel warningLabel = SwingUtils
        .createComponent("warningLabel", new JLabel("Warning: Action buttons ATTN, " +
            "RESET, ROLL_UP and ROLL_DN are only supported for TN5250 protocol. " +
            "Action buttons PA1, PA2 and PA3 are only supported for TN3270 protocol."));
    warningLabel.setFont(new Font(null, Font.ITALIC, 11));

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(payloadLabel)
        .addComponent(payloadPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addComponent(actionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            Short.MAX_VALUE)
        .addComponent(warningLabel));

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(payloadLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(payloadPanel, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(actionsPanel, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(warningLabel)
        .addPreferredGap(ComponentPlacement.UNRELATED));

    return panel;
  }

  private JPanel buildActionsPanel() {
    JPanel panel = SwingUtils.createComponent("actionPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Actions"));
    panel.setLayout(new GridLayout(0, 12));

    Arrays.stream(Action.values()).forEach(t -> {
      JRadioButton r = SwingUtils.createComponent(t.toString(), new JRadioButton(t.toString()));
      r.setActionCommand(t.toString());
      panel.add(r);
      actions.put(t, r);
      actionsGroup.add(r);
    });

    return panel;
  }

  private JPanel buildWaitsPanel() {
    JPanel panel = SwingUtils.createComponent("waitsPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Wait for:"));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    layout.setAutoCreateGaps(true);
    panel.setLayout(layout);

    waitSyncPanel = buildWaitSyncPanel();
    waitCursorPanel = buildWaitCursorPanel();
    waitSilentPanel = buildWaitSilentPanel();
    waitTextPanel = buildWaitTextPanel();

    JLabel warningLabel = SwingUtils
        .createComponent("warningLabel", new JLabel("Warning: if Timeout value " +
            "is shorter than Stable time, or Silent interval, " +
            "the sampler will return a Timeout exception. " +
            "For more information see sampler documentation."));
    warningLabel.setFont(new Font(null, Font.ITALIC, 11));

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(waitSyncPanel)
        .addComponent(waitCursorPanel)
        .addComponent(waitSilentPanel)
        .addComponent(waitTextPanel)
        .addComponent(warningLabel));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(waitSyncPanel)
        .addComponent(waitCursorPanel)
        .addComponent(waitSilentPanel)
        .addComponent(waitTextPanel)
        .addComponent(warningLabel));

    return panel;
  }

  private JPanel buildWaitSyncPanel() {
    JPanel panel = SwingUtils.createComponent("waitSyncPanel", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitSync.addItemListener(e -> {
      updateWait(waitSync, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel timeoutLabel = SwingUtils.createComponent("timeoutLabel", new JLabel(TIMEOUT_LABEL));
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitSync)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitSyncTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitSync)
        .addComponent(timeoutLabel)
        .addComponent(waitSyncTimeout));

    return panel;
  }

  private void updateWait(JCheckBox waitCheck, JPanel panel, boolean checked) {
    setEnabled(panel, checked);
    waitCheck.setEnabled(true);
  }

  private void setEnabled(Component component, boolean enabled) {
    component.setEnabled(enabled);
    if (component instanceof Container) {
      for (Component child : ((Container) component).getComponents()) {
        setEnabled(child, enabled);
      }
    }
  }

  private JPanel buildWaitCursorPanel() {
    JPanel panel = SwingUtils.createComponent("waitCursorPanel", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitCursor.addItemListener(e -> {
      updateWait(waitCursor, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel rowLabel = SwingUtils.createComponent("rowLabel", new JLabel("Row: "));
    JLabel columnLabel = SwingUtils.createComponent("columnLabel", new JLabel("Column: "));
    JLabel timeoutLabel = SwingUtils.createComponent("timeoutLabel", new JLabel(TIMEOUT_LABEL));
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitCursor)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(rowLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitCursorRow, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(columnLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitCursorColumn, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitCursorTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitCursor)
        .addComponent(rowLabel)
        .addComponent(waitCursorRow)
        .addComponent(columnLabel)
        .addComponent(waitCursorColumn)
        .addComponent(timeoutLabel)
        .addComponent(waitCursorTimeout));

    return panel;
  }

  private JPanel buildWaitSilentPanel() {
    JPanel panel = SwingUtils.createComponent("waitSilentPanel", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitSilent.addItemListener(e -> {
      updateWait(waitSilent, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel timeLabel = SwingUtils
        .createComponent("timeLabel", new JLabel("Silent interval (millis): "));
    JLabel timeoutLabel = SwingUtils.createComponent("timeoutLabel", new JLabel(TIMEOUT_LABEL));
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitSilent)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitSilentTime, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitSilentTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitSilent)
        .addComponent(timeoutLabel)
        .addComponent(waitSilentTimeout)
        .addComponent(timeLabel)
        .addComponent(waitSilentTime));
    return panel;
  }

  private JPanel buildWaitTextPanel() {
    JPanel panel = SwingUtils.createComponent("waitTextPanel", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitText.addItemListener(e -> {
      updateWait(waitText, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel regexLabel = SwingUtils.createComponent("regexLabel", new JLabel("Regex: "));
    JLabel timeoutLabel = SwingUtils.createComponent("timeoutLabel", new JLabel(TIMEOUT_LABEL));
    JPanel searchAreaPanel = buildSearchAreaPanel();
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitText)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addComponent(regexLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(waitTextRegex, GroupLayout.PREFERRED_SIZE, 200,
                    GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(timeoutLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(waitTextTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
                    GroupLayout.PREFERRED_SIZE))
            .addComponent(searchAreaPanel))
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(waitText)
            .addComponent(regexLabel)
            .addComponent(waitTextRegex)
            .addComponent(timeoutLabel)
            .addComponent(waitTextTimeout))
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(searchAreaPanel));

    return panel;
  }

  private JPanel buildSearchAreaPanel() {
    JPanel panel = SwingUtils.createComponent("searchAreaPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Search area: "));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    panel.setLayout(layout);

    JLabel topLabel = SwingUtils.createComponent("topLabel", new JLabel("Top row: "));
    JLabel leftLabel = SwingUtils.createComponent("leftLabel", new JLabel("Left column: "));
    JLabel bottomLabel = SwingUtils.createComponent("bottomLabel", new JLabel("Bottom row: "));
    JLabel rightLabel = SwingUtils.createComponent("rightLabel", new JLabel("Right column: "));
    layout.setHorizontalGroup(
        layout.createSequentialGroup()
            .addComponent(leftLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(waitTextAreaLeft, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(topLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(waitTextAreaTop, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                        GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(bottomLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(waitTextAreaBottom, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                        GroupLayout.PREFERRED_SIZE)
                )
            )
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(rightLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(waitTextAreaRight, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
            .addComponent(topLabel)
            .addComponent(waitTextAreaTop))
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
            .addComponent(leftLabel)
            .addComponent(waitTextAreaLeft)
            .addComponent(rightLabel)
            .addComponent(waitTextAreaRight))
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
            .addComponent(bottomLabel)
            .addComponent(waitTextAreaBottom)));

    return panel;
  }

  public void resetFields() {
    payloadPanel.clear();
  }

  public Mode getMode() {
    String mode = modesGroup.getSelection().getActionCommand();
    return Mode.valueOf(mode);
  }

  public void setMode(Mode mode) {
    if (modes.containsKey(mode)) {
      modes.get(mode).setSelected(true);
    } else {
      modes.get(RTESampler.DEFAULT_MODE).setSelected(true);
    }
  }

  public CoordInputPanel getPayload() {
    return this.payloadPanel;
  }

  public Action getAction() {
    String action = actionsGroup.getSelection().getActionCommand();
    return Action.valueOf(action);
  }

  public void setAction(Action action) {
    if (actions.containsKey(action)) {
      actions.get(action).setSelected(true);
    } else {
      actions.get(RTESampler.DEFAULT_ACTION).setSelected(true);
    }
  }

  public boolean getWaitSync() {
    return this.waitSync.isSelected();
  }

  public void setWaitSync(boolean waitSync) {
    this.waitSync.setSelected(waitSync);
    updateWait(this.waitSync, waitSyncPanel, waitSync);
  }

  public String getWaitSyncTimeout() {
    return this.waitSyncTimeout.getText();
  }

  public void setWaitSyncTimeout(String waitSyncTimeout) {
    this.waitSyncTimeout.setText(waitSyncTimeout);
  }

  public boolean getWaitCursor() {
    return this.waitCursor.isSelected();
  }

  public void setWaitCursor(boolean waitCursor) {
    this.waitCursor.setSelected(waitCursor);
    updateWait(this.waitCursor, waitCursorPanel, waitCursor);
  }

  public String getWaitCursorRow() {
    return this.waitCursorRow.getText();
  }

  public void setWaitCursorRow(String waitCursorRow) {
    this.waitCursorRow.setText(waitCursorRow);
  }

  public String getWaitCursorColumn() {
    return this.waitCursorColumn.getText();
  }

  public void setWaitCursorColumn(String waitCursorColumn) {
    this.waitCursorColumn.setText(waitCursorColumn);
  }

  public String getWaitCursorTimeout() {
    return this.waitCursorTimeout.getText();
  }

  public void setWaitCursorTimeout(String waitCursorTimeout) {
    this.waitCursorTimeout.setText(waitCursorTimeout);
  }

  public boolean getWaitSilent() {
    return this.waitSilent.isSelected();
  }

  public void setWaitSilent(boolean waitSilent) {
    this.waitSilent.setSelected(waitSilent);
    updateWait(this.waitSilent, waitSilentPanel, waitSilent);
  }

  public String getWaitSilentTime() {
    return this.waitSilentTime.getText();
  }

  public void setWaitSilentTime(String waitSilentTime) {
    this.waitSilentTime.setText(waitSilentTime);
  }

  public String getWaitSilentTimeout() {
    return this.waitSilentTimeout.getText();
  }

  public void setWaitSilentTimeout(String waitSilentTimeout) {
    this.waitSilentTimeout.setText(waitSilentTimeout);
  }

  public boolean getWaitText() {
    return this.waitText.isSelected();
  }

  public void setWaitText(boolean waitText) {
    this.waitText.setSelected(waitText);
    updateWait(this.waitText, waitTextPanel, waitText);
  }

  public String getWaitTextRegex() {
    return this.waitTextRegex.getText();
  }

  public void setWaitTextRegex(String waitTextRegex) {
    this.waitTextRegex.setText(waitTextRegex);
  }

  public String getWaitTextAreaTop() {
    return this.waitTextAreaTop.getText();
  }

  public void setWaitTextAreaTop(String row) {
    this.waitTextAreaTop.setText(row);
  }

  public String getWaitTextAreaLeft() {
    return this.waitTextAreaLeft.getText();
  }

  public void setWaitTextAreaLeft(String column) {
    this.waitTextAreaLeft.setText(column);
  }

  public String getWaitTextAreaBottom() {
    return this.waitTextAreaBottom.getText();
  }

  public void setWaitTextAreaBottom(String row) {
    this.waitTextAreaBottom.setText(row);
  }

  public String getWaitTextAreaRight() {
    return this.waitTextAreaRight.getText();
  }

  public void setWaitTextAreaRight(String column) {
    this.waitTextAreaRight.setText(column);
  }

  public String getWaitTextTimeout() {
    return this.waitTextTimeout.getText();
  }

  public void setWaitTextTimeout(String waitTextTimeout) {
    this.waitTextTimeout.setText(waitTextTimeout);
  }

}

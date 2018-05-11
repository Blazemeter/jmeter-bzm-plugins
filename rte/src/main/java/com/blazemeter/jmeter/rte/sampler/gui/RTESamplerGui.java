package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.BorderLayout;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

public class RTESamplerGui extends AbstractSamplerGui {

  private static final long serialVersionUID = 4024916662489960067L;
  private RTESamplerPanel rteSamplerPanel;

  public RTESamplerGui() {
    rteSamplerPanel = new RTESamplerPanel();
    rteSamplerPanel.resetFields();

    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());

    add(makeTitlePanel(), BorderLayout.NORTH);
    add(rteSamplerPanel, BorderLayout.CENTER);
  }

  @VisibleForTesting
  protected RTESamplerGui(RTESamplerPanel panel) {
    rteSamplerPanel = panel;
  }

  @Override
  public String getStaticLabel() {
    return "RTE Sampler";
  }

  @Override
  public String getLabelResource() {
    throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
  }

  @Override
  public void configure(TestElement element) {
    super.configure(element);
    if (element instanceof RTESampler) {
      RTESampler sampler = (RTESampler) element;
      Inputs payload = sampler.getInputs();
      if (payload != null) {
        rteSamplerPanel.getPayload().configure(payload);
      }
      rteSamplerPanel.setAction(sampler.getAction());
      rteSamplerPanel.setMode(sampler.getMode());
      rteSamplerPanel.setWaitSync(sampler.getWaitSync());
      rteSamplerPanel.setWaitSyncTimeout(sampler.getWaitSyncTimeout());
      rteSamplerPanel.setWaitCursor(sampler.getWaitCursor());
      rteSamplerPanel.setWaitCursorRow(sampler.getWaitCursorRow());
      rteSamplerPanel.setWaitCursorColumn(sampler.getWaitCursorColumn());
      rteSamplerPanel.setWaitCursorTimeout(sampler.getWaitCursorTimeout());
      rteSamplerPanel.setWaitSilent(sampler.getWaitSilent());
      rteSamplerPanel.setWaitSilentTime(sampler.getWaitSilentTime());
      rteSamplerPanel.setWaitSilentTimeout(sampler.getWaitSilentTimeout());
      rteSamplerPanel.setWaitText(sampler.getWaitText());
      rteSamplerPanel.setWaitTextRegex(sampler.getWaitTextRegex());
      rteSamplerPanel.setWaitTextAreaTop(sampler.getWaitTextAreaTop());
      rteSamplerPanel.setWaitTextAreaLeft(sampler.getWaitTextAreaLeft());
      rteSamplerPanel.setWaitTextAreaBottom(sampler.getWaitTextAreaBottom());
      rteSamplerPanel.setWaitTextAreaRight(sampler.getWaitTextAreaRight());
      rteSamplerPanel.setWaitTextTimeout(sampler.getWaitTextTimeout());
    }
  }

  @Override
  public TestElement createTestElement() {
    RTESampler preproc = new RTESampler();
    configureTestElement(preproc);
    return preproc;
  }

  @Override
  public void modifyTestElement(TestElement te) {
    configureTestElement(te);
    if (te instanceof RTESampler) {
      RTESampler sampler = (RTESampler) te;
      CoordInputPanel payload = rteSamplerPanel.getPayload();
      if (payload != null) {
        sampler.setPayload((Inputs) payload.createTestElement());
      }
      sampler.setAction(rteSamplerPanel.getAction());
      sampler.setMode(rteSamplerPanel.getMode());
      sampler.setWaitSync(rteSamplerPanel.getWaitSync());
      sampler.setWaitSyncTimeout(rteSamplerPanel.getWaitSyncTimeout());
      sampler.setWaitCursor(rteSamplerPanel.getWaitCursor());
      sampler.setWaitCursorRow(rteSamplerPanel.getWaitCursorRow());
      sampler.setWaitCursorColumn(rteSamplerPanel.getWaitCursorColumn());
      sampler.setWaitCursorTimeout(rteSamplerPanel.getWaitCursorTimeout());
      sampler.setWaitSilent(rteSamplerPanel.getWaitSilent());
      sampler.setWaitSilentTime(rteSamplerPanel.getWaitSilentTime());
      sampler.setWaitSilentTimeout(rteSamplerPanel.getWaitSilentTimeout());
      sampler.setWaitText(rteSamplerPanel.getWaitText());
      sampler.setWaitTextRegex(rteSamplerPanel.getWaitTextRegex());
      sampler.setWaitTextTimeout(rteSamplerPanel.getWaitTextTimeout());
      sampler.setWaitTextAreaTop(rteSamplerPanel.getWaitTextAreaTop());
      sampler.setWaitTextAreaLeft(rteSamplerPanel.getWaitTextAreaLeft());
      sampler.setWaitTextAreaBottom(rteSamplerPanel.getWaitTextAreaBottom());
      sampler.setWaitTextAreaRight(rteSamplerPanel.getWaitTextAreaRight());
    }
  }

  @Override
  public void clearGui() {
    super.clearGui();
    rteSamplerPanel.resetFields();
  }

}

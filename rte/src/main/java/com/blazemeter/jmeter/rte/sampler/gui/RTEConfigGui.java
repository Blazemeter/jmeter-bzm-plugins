package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.BorderLayout;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class RTEConfigGui extends AbstractConfigGui {

  private static final String SSL_SUPPORT_ENABLED = "RTEConnectionConfig.sslSupportEnabled";

  private static final long serialVersionUID = 8495980373764997386L;
  private RTEConfigPanel rteConfigPanelConfigPanel;

  public RTEConfigGui() {
    rteConfigPanelConfigPanel = new RTEConfigPanel();

    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());

    add(makeTitlePanel(), BorderLayout.NORTH);
    add(rteConfigPanelConfigPanel, BorderLayout.CENTER);
  }

  @VisibleForTesting
  protected RTEConfigGui(RTEConfigPanel panel) {
    rteConfigPanelConfigPanel = panel;
  }

  @Override
  public String getStaticLabel() {
    return "RTE Config";
  }

  @Override
  public String getLabelResource() {
    throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
  }

  @Override
  public void configure(TestElement element) {
    super.configure(element);
    if (element instanceof ConfigTestElement) {
      ConfigTestElement configTestElement = (ConfigTestElement) element;
      rteConfigPanelConfigPanel
          .setServer(configTestElement.getPropertyAsString(RTESampler.CONFIG_SERVER));
      rteConfigPanelConfigPanel.setPort(
          configTestElement.getPropertyAsString(RTESampler.CONFIG_PORT,
              String.valueOf(RTESampler.DEFAULT_PORT)));
      Protocol protocol = Protocol
          .valueOf(configTestElement.getPropertyAsString(RTESampler.CONFIG_PROTOCOL,
              RTESampler.DEFAULT_PROTOCOL.name()));
      rteConfigPanelConfigPanel.setProtocol(protocol);
      rteConfigPanelConfigPanel.setTerminalType(protocol.createProtocolClient().getTerminalTypeById(
          configTestElement.getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE,
              RTESampler.DEFAULT_TERMINAL_TYPE.getId())));
      rteConfigPanelConfigPanel.setSSLSupportEnabled(isSSLSupportEnabled());
      rteConfigPanelConfigPanel.setSSLType(
          SSLType.valueOf(configTestElement
              .getPropertyAsString(RTESampler.CONFIG_SSL_TYPE, RTESampler.DEFAULT_SSLTYPE.name())));
      rteConfigPanelConfigPanel
          .setConnectionTimeout(
              configTestElement.getPropertyAsString(RTESampler.CONFIG_CONNECTION_TIMEOUT,
                  String.valueOf(RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS)));
    }
  }

  private boolean isSSLSupportEnabled() {
    return JMeterUtils.getPropDefault(SSL_SUPPORT_ENABLED, false);
  }

  @VisibleForTesting
  protected void setSSLSupportEnabled(boolean enabled) {
    JMeterUtils.setProperty(SSL_SUPPORT_ENABLED, String.valueOf(enabled));
  }

  @Override
  public TestElement createTestElement() {
    ConfigTestElement config = new ConfigTestElement();
    configureTestElement(config);
    return config;
  }

  @Override
  public void modifyTestElement(TestElement te) {
    configureTestElement(te);
    if (te instanceof ConfigTestElement) {
      ConfigTestElement configTestElement = (ConfigTestElement) te;
      configTestElement
          .setProperty(RTESampler.CONFIG_SERVER, rteConfigPanelConfigPanel.getServer());
      configTestElement.setProperty(RTESampler.CONFIG_PORT, rteConfigPanelConfigPanel.getPort());
      configTestElement
          .setProperty(RTESampler.CONFIG_PROTOCOL, rteConfigPanelConfigPanel.getProtocol().name());
      configTestElement
          .setProperty(RTESampler.CONFIG_SSL_TYPE, rteConfigPanelConfigPanel.getSSLType().name());
      configTestElement.setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
          rteConfigPanelConfigPanel.getTerminalType().getId());
      configTestElement.setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT,
          rteConfigPanelConfigPanel.getConnectionTimeout());

    }
  }

}

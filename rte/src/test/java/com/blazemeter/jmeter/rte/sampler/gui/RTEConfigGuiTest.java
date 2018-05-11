package com.blazemeter.jmeter.rte.sampler.gui;

import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTEConfigGuiTest {

  private RTEConfigGui configGui;
  private TestElement testElement;

  @Mock
  private RTEConfigPanel panel;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Before
  public void setup() {
    configGui = new RTEConfigGui(panel);
    testElement = new ConfigTestElement();
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void shouldSetTheTestElementFromThePanelWhenModifyTestElement() {
    final String server = "Server";
    final String port = "80";
    final Protocol protocol = Protocol.TN5250;
    final TerminalType terminalType = protocol.createProtocolClient().getDefaultTerminalType();
    final SSLType sslType = SSLType.NONE;
    final String timeout = "10000";
    when(panel.getServer()).thenReturn(server);
    when(panel.getPort()).thenReturn(port);
    when(panel.getProtocol()).thenReturn(protocol);
    when(panel.getSSLType()).thenReturn(sslType);
    when(panel.getTerminalType()).thenReturn(terminalType);
    when(panel.getConnectionTimeout()).thenReturn(timeout);
    configGui.modifyTestElement(testElement);
    softly.assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_SERVER))
        .as(RTESampler.CONFIG_SERVER).isEqualTo(server);
    softly.assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_PORT))
        .as(RTESampler.CONFIG_PORT).isEqualTo(port);
    softly.assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_PROTOCOL))
        .as(RTESampler.CONFIG_PROTOCOL).isEqualTo(protocol.name());
    softly.assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_SSL_TYPE))
        .as(RTESampler.CONFIG_SSL_TYPE).isEqualTo(sslType.name());
    softly.assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE))
        .as(RTESampler.CONFIG_TERMINAL_TYPE).isEqualTo(terminalType.getId());
    softly.assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_CONNECTION_TIMEOUT))
        .as(RTESampler.CONFIG_CONNECTION_TIMEOUT).isEqualTo(timeout);
  }
}

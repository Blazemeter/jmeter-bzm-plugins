package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComboBox;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.swing.driver.WaitForComponentToShowCondition;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class RTEConfigPanelTest {

  private FrameFixture frame;

  @BeforeClass
  public static void setUpOnce() {
    TestJMeterUtils.createJmeterEnv();
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldChangeTheValuesOfTheTerminalComboBoxWhenChangeProtocolComboBox() {
    RTEConfigPanel panel = new RTEConfigPanel();
    frame = showInFrame(panel);
    JComboBoxFixture protocolCombo = frame.comboBox("protocolComboBox");
    JComboBox<TerminalType> terminalCombo = frame.comboBox("terminalTypeComboBox").target();
    protocolCombo.selectItem(Protocol.TN3270.name());
    pause(new Condition("TN3270 terminal type listed in terminal combo") {
      @Override
      public boolean test() {
        return Protocol.TN3270.createProtocolClient().getSupportedTerminalTypes().containsAll(getComboValues(terminalCombo));
      }
    });
  }

  private <T> List<T> getComboValues(JComboBox<T> combo) {
    List<T> ret = new ArrayList<>();
    for (int i = 0; i < combo.getItemCount(); i++) {
      ret.add(combo.getItemAt(i));
    }
    return ret;
  }

  @Test
  public void shouldNotShowSslPanelWhenSslSupportIsNotEnabled() {
    RTEConfigPanel panel = new RTEConfigPanel();
    panel.setSSLSupportEnabled(false);
    frame = showInFrame(panel);
    AssertJUtils.findInvisiblePanelByName(frame, "sslPanel").requireNotVisible();
  }

  @Test
  public void shouldShowSslPanelWhenSslSupportIsEnabled() {
    RTEConfigPanel panel = new RTEConfigPanel();
    panel.setSSLSupportEnabled(true);
    frame = showInFrame(panel);
    frame.panel("sslPanel");
  }

}

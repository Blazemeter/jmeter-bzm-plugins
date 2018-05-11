package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import com.blazemeter.jmeter.rte.sampler.Mode;
import java.util.ArrayList;
import java.util.List;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.swing.driver.WaitForComponentToShowCondition;
import org.assertj.swing.fixture.AbstractJComponentFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JCheckBoxFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.format.Formatting;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RTESamplerPanelTest {

  private static final String REQUEST_PANEL = "requestPanel";
  private static final String WAITS_PANEL = "requestPanel";
  private static final String WAIT_SYNC = "waitSync";
  private static final String WAIT_SYNC_TIMEOUT = "waitSyncTimeout";
  private static final String WAIT_CURSOR = "waitCursor";
  private static final String WAIT_CURSOR_ROW = "waitCursorRow";
  private static final String WAIT_CURSOR_COLUMN = "waitCursorColumn";
  private static final String WAIT_CURSOR_TIMEOUT = "waitCursorTimeout";
  private static final String WAIT_SILENT = "waitSilent";
  private static final String WAIT_SILENT_TIME = "waitSilentTime";
  private static final String WAIT_SILENT_TIMEOUT = "waitSilentTimeout";
  private static final String WAIT_TEXT = "waitText";
  private static final String WAIT_TEXT_REGEX = "waitTextRegex";
  private static final String WAIT_TEXT_TIMEOUT = "waitTextTimeout";
  private static final String WAIT_TEXT_AREA_LEFT = "waitTextAreaLeft";
  private static final String WAIT_TEXT_AREA_TOP = "waitTextAreaTop";
  private static final String WAIT_TEXT_AREA_BOTTOM = "waitTextAreaBottom";
  private static final String WAIT_TEXT_AREA_RIGHT = "waitTextAreaRight";
  private static final long CHANGE_TIMEOUT_MILLIS = 10000;

  private FrameFixture frame;

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  public void setup() {
    RTESamplerPanel panel = new RTESamplerPanel();
    frame = showInFrame(panel);
  }

  @Test
  public void shouldHideRequestPanelWhenNotSendInputModeMode() {
    switchMode(Mode.SEND_INPUT, Mode.CONNECT);
    assertPanelIsNotVisible(REQUEST_PANEL);
  }

  private void switchMode(Mode mode1, Mode mode2) {
    frame.radioButton(mode1.name()).check();
    frame.radioButton(mode2.name()).check();
  }

  private void assertPanelIsNotVisible(String panelName) {
    JPanelFixture panel = AssertJUtils.findInvisiblePanelByName(frame, panelName);
    pause(new Condition("Component " + Formatting.format(panel.target()) + " is not visible") {
      @Override
      public boolean test() {
        return !panel.target().isVisible();
      }
    }, CHANGE_TIMEOUT_MILLIS);
  }


  @Test
  public void shouldShowRequestPanelWhenSendInputMode() {
    switchMode(Mode.CONNECT, Mode.SEND_INPUT);
    JPanelFixture panel = frame.panel(REQUEST_PANEL);
    pause(WaitForComponentToShowCondition.untilIsShowing(panel.target()));
  }

  @Test
  public void shouldHideWaitsPanelWhenDisconnectMode() {
    switchMode(Mode.SEND_INPUT, Mode.DISCONNECT);
    assertPanelIsNotVisible(WAITS_PANEL);
  }

  @Test
  public void shouldShowWaitsPanelWhenNotDisconnectMode() {
    switchMode(Mode.DISCONNECT, Mode.SEND_INPUT);
    JPanelFixture panel = frame.panel(WAITS_PANEL);
    pause(WaitForComponentToShowCondition.untilIsShowing(panel.target()));
  }

  @Test
  public void shouldDisableSyncWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_SYNC);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(false, check);
    expected.add(frame.textBox(WAIT_SYNC_TIMEOUT));
    validateEnabled(false, expected);
  }

  @Test
  public void shouldEnableSyncWaitFieldsWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_SYNC);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(true, check);
    expected.add(frame.textBox(WAIT_SYNC_TIMEOUT));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldEnableCursorWaitFieldsWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_CURSOR);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(true, check);
    expected.add(frame.textBox(WAIT_CURSOR_ROW));
    expected.add(frame.textBox(WAIT_CURSOR_COLUMN));
    expected.add(frame.textBox(WAIT_CURSOR_TIMEOUT));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldDisableCursorWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_CURSOR);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(false, check);
    expected.add(frame.textBox(WAIT_CURSOR_ROW));
    expected.add(frame.textBox(WAIT_CURSOR_COLUMN));
    expected.add(frame.textBox(WAIT_CURSOR_TIMEOUT));
    validateEnabled(false, expected);
  }

  @Test
  public void shouldEnableSilentWaitFieldsWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_SILENT);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(true, check);
    expected.add(frame.textBox(WAIT_SILENT_TIME));
    expected.add(frame.textBox(WAIT_SILENT_TIMEOUT));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldDisableSilentWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_SILENT);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(false, check);
    expected.add(frame.textBox(WAIT_SILENT_TIME));
    expected.add(frame.textBox(WAIT_SILENT_TIMEOUT));
    validateEnabled(false, expected);
  }

  @Test
  public void shouldEnableTextWaitFieldWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_TEXT);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(true, check);
    expected.add(frame.textBox(WAIT_TEXT_REGEX));
    expected.add(frame.textBox(WAIT_TEXT_TIMEOUT));
    expected.add(frame.textBox(WAIT_TEXT_AREA_LEFT));
    expected.add(frame.textBox(WAIT_TEXT_AREA_TOP));
    expected.add(frame.textBox(WAIT_TEXT_AREA_BOTTOM));
    expected.add(frame.textBox(WAIT_TEXT_AREA_RIGHT));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldDisableTextWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox(WAIT_TEXT);
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    switchCheckboxTo(false, check);
    expected.add(frame.textBox(WAIT_TEXT_REGEX));
    expected.add(frame.textBox(WAIT_TEXT_TIMEOUT));
    expected.add(frame.textBox(WAIT_TEXT_AREA_LEFT));
    expected.add(frame.textBox(WAIT_TEXT_AREA_TOP));
    expected.add(frame.textBox(WAIT_TEXT_AREA_BOTTOM));
    expected.add(frame.textBox(WAIT_TEXT_AREA_RIGHT));
    validateEnabled(false, expected);
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  private void switchCheckboxTo(boolean state, JCheckBoxFixture check) {
    check.check(!state);
    check.check(state);
  }

  private void validateEnabled(boolean enable, List<AbstractJComponentFixture> components) {

    pause(new Condition("Components are " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        List<String> result = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        for (AbstractJComponentFixture c : components) {
          if (c.isEnabled() == enable) {
            result.add(c.target().getName());
          }
          expected.add(c.target().getName());
        }
        return result.containsAll(expected) && expected.containsAll(result);
      }
    }, timeout(CHANGE_TIMEOUT_MILLIS));
  }
}

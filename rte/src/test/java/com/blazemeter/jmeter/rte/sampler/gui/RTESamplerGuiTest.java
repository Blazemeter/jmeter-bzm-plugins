package com.blazemeter.jmeter.rte.sampler.gui;

import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.sampler.Mode;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTESamplerGuiTest {

  private RTESamplerGui samplerGui;
  private RTESampler testElement;

  @Mock
  private RTESamplerPanel panel;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Before
  public void setup() {
    samplerGui = new RTESamplerGui(panel);
    testElement = new RTESampler();
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void shouldSetTheTestElementFromThePanel() {
    final Action action = Action.ENTER;
    final String waitSyncTimeout = "1";
    final String waitCursorColumn = "2";
    final String waitCursorRow = "3";
    final String waitCursorTimeout = "4";
    final String waitSilentTimeout = "5";
    final String waitSilentTime = "6";
    final String waitTextAreaBottom = "7";
    final String waitTextAreaLeft = "8";
    final String waitTextAreaRight = "9";
    final String waitTextAreaTop = "10";
    final String waitTextRegex = "regExp";
    final String waitTextTimeout = "11";
    when(panel.getAction()).thenReturn(action);
    when(panel.getMode()).thenReturn(Mode.CONNECT);
    when(panel.getWaitSync()).thenReturn(true);
    when(panel.getWaitSyncTimeout()).thenReturn(waitSyncTimeout);
    when(panel.getWaitCursor()).thenReturn(true);
    when(panel.getWaitCursorColumn()).thenReturn(waitCursorColumn);
    when(panel.getWaitCursorRow()).thenReturn(waitCursorRow);
    when(panel.getWaitCursorTimeout()).thenReturn(waitCursorTimeout);
    when(panel.getWaitSilent()).thenReturn(true);
    when(panel.getWaitSilentTimeout()).thenReturn(waitSilentTimeout);
    when(panel.getWaitSilentTime()).thenReturn(waitSilentTime);
    when(panel.getWaitText()).thenReturn(true);
    when(panel.getWaitTextAreaBottom()).thenReturn(waitTextAreaBottom);
    when(panel.getWaitTextAreaLeft()).thenReturn(waitTextAreaLeft);
    when(panel.getWaitTextAreaRight()).thenReturn(waitTextAreaRight);
    when(panel.getWaitTextAreaTop()).thenReturn(waitTextAreaTop);
    when(panel.getWaitTextRegex()).thenReturn(waitTextRegex);
    when(panel.getWaitTextTimeout()).thenReturn(waitTextTimeout);
    when(panel.getPayload()).thenReturn(null);

    samplerGui.modifyTestElement(testElement);

    softly.assertThat(testElement.getAction()).as("Action").isEqualTo(action);
    softly.assertThat(testElement.getMode()).as("Mode").isEqualTo(Mode.CONNECT);
    softly.assertThat(testElement.getWaitSync()).as("WaitSync").isEqualTo(true);
    softly.assertThat(testElement.getWaitCursor()).as("WaitCursor").isEqualTo(true);
    softly.assertThat(testElement.getWaitSilent()).as("WaitSilent").isEqualTo(true);
    softly.assertThat(testElement.getWaitText()).as("WaitText").isEqualTo(true);
    softly.assertThat(testElement.getWaitSyncTimeout()).as("WaitSyncTimeout")
        .isEqualTo(waitSyncTimeout);
    softly.assertThat(testElement.getWaitCursorColumn()).as("WaitCursorColumn")
        .isEqualTo(waitCursorColumn);
    softly.assertThat(testElement.getWaitCursorRow()).as("WaitCursorRow")
        .isEqualTo(waitCursorRow);
    softly.assertThat(testElement.getWaitCursorTimeout()).as("WaitCursorTimeout")
        .isEqualTo(waitCursorTimeout);
    softly.assertThat(testElement.getWaitSilentTimeout()).as("WaitSilentTimeout")
        .isEqualTo(waitSilentTimeout);
    softly.assertThat(testElement.getWaitSilentTime()).as("WaitSilentTime")
        .isEqualTo(waitSilentTime);
    softly.assertThat(testElement.getWaitTextAreaBottom()).as("WaitTextAreaBottom")
        .isEqualTo(waitTextAreaBottom);
    softly.assertThat(testElement.getWaitTextAreaLeft()).as("WaitTextAreaLeft")
        .isEqualTo(waitTextAreaLeft);
    softly.assertThat(testElement.getWaitTextAreaRight()).as("WaitTextAreaRight")
        .isEqualTo(waitTextAreaRight);
    softly.assertThat(testElement.getWaitTextAreaTop()).as("WaitTextAreaTop")
        .isEqualTo(waitTextAreaTop);
    softly.assertThat(testElement.getWaitTextTimeout()).as("WaitTextTimeout")
        .isEqualTo(waitTextTimeout);
    softly.assertThat(testElement.getWaitTextRegex()).as("WaitTextRegex")
        .isEqualTo(waitTextRegex);
  }

}

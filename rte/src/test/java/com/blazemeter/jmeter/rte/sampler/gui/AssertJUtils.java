package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.swing.timing.Pause.pause;

import java.awt.ItemSelectable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.timing.Condition;

public final class AssertJUtils {

  private AssertJUtils() {
  }

  public static JPanelFixture findInvisiblePanelByName(FrameFixture frame, String name) {
    return frame.panel(new GenericTypeMatcher<JPanel>(JPanel.class) {
      @Override
      protected boolean isMatching(JPanel component) {
        return name.equals(component.getName());
      }
    });
  }
}

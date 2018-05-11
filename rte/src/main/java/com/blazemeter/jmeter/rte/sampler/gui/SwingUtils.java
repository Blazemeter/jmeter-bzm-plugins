package com.blazemeter.jmeter.rte.sampler.gui;

import javax.swing.JComponent;

public class SwingUtils {

  public static <T extends JComponent> T createComponent(String name, T component) {
    component.setName(name);
    return component;
  }
}

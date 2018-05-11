package com.blazemeter.jmeter.rte.core;

import java.awt.Dimension;

public class TerminalType {

  private final String id;
  private final Dimension screenSize;

  public TerminalType(String id, Dimension screenSize) {
    this.id = id;
    this.screenSize = screenSize;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return id + ": " + screenSize.height + "x" + screenSize.width;
  }

}

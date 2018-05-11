package com.blazemeter.jmeter.rte.sampler;

public enum Mode {
  CONNECT("Connect"), SEND_INPUT("Send keys"), DISCONNECT("Disconnect");

  private final String label;

  Mode(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

}

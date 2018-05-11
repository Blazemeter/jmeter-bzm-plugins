package com.blazemeter.jmeter.rte.core.ssl;

public enum SSLType {
  NONE("NONE"),
  SSLV2("SSLv2"),
  SSLV3("SSLv3"),
  TLS("TLS");

  private final String name;

  SSLType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
  
}

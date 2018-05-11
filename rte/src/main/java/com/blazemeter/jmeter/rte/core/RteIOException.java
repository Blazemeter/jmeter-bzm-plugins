package com.blazemeter.jmeter.rte.core;

public class RteIOException extends Exception {

  public RteIOException(Throwable cause) {
    super("Communication error with RTE client", cause);
  }

}

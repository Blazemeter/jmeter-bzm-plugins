package com.blazemeter.jmeter.rte.core.wait;

public class SilentWaitCondition extends WaitCondition {

  public SilentWaitCondition(long timeoutMillis, long stableTimeoutMillis) {
    super(timeoutMillis, stableTimeoutMillis);
  }

  @Override
  public String getDescription() {
    return "emulator to be silent for " + stableTimeoutMillis + " millis";
  }

}

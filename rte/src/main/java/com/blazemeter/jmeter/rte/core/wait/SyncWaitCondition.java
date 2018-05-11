package com.blazemeter.jmeter.rte.core.wait;

/**
 * {@link WaitCondition} to wait for the terminal to get unlocked.
 */
public class SyncWaitCondition extends WaitCondition {

  public SyncWaitCondition(long timeoutMillis, long stableTimeoutMillis) {
    super(timeoutMillis, stableTimeoutMillis);
  }

  @Override
  public String getDescription() {
    return "emulator to be unlocked";
  }

  @Override
  public String toString() {
    return "SyncWaitCondition{" +
        "timeoutMillis=" + timeoutMillis +
        ", stableTimeoutMillis=" + stableTimeoutMillis +
        '}';
  }

}

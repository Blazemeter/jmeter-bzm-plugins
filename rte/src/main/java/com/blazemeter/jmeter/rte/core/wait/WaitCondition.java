package com.blazemeter.jmeter.rte.core.wait;

import java.util.Objects;

/**
 * Condition to wait for emulator to match.
 */
public abstract class WaitCondition {

  protected long timeoutMillis;
  protected long stableTimeoutMillis;

  public WaitCondition(long timeoutMillis, long stableTimeoutMillis) {
    this.timeoutMillis = timeoutMillis;
    this.stableTimeoutMillis = stableTimeoutMillis;
  }

  public long getTimeoutMillis() {
    return this.timeoutMillis;
  }

  public long getStableTimeoutMillis() {
    return this.stableTimeoutMillis;
  }

  public abstract String getDescription();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WaitCondition that = (WaitCondition) o;
    return timeoutMillis == that.timeoutMillis &&
        stableTimeoutMillis == that.stableTimeoutMillis;
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeoutMillis, stableTimeoutMillis);
  }

}

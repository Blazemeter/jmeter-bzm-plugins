package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.Position;
import java.util.Objects;

/**
 * {@link WaitCondition} to wait for the cursor to be visible on the desired position.
 */
public class CursorWaitCondition extends WaitCondition {

  private final Position position;

  public CursorWaitCondition(Position position, long timeoutMillis, long stableTimeoutMillis) {
    super(timeoutMillis, stableTimeoutMillis);
    this.position = position;
  }

  public Position getPosition() {
    return position;
  }

  @Override
  public String getDescription() {
    return "cursor to be visible";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    CursorWaitCondition that = (CursorWaitCondition) o;
    return Objects.equals(position, that.position);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), position);
  }

  @Override
  public String toString() {
    return "CursorWaitCondition{" +
        "position=" + position +
        ", timeoutMillis=" + timeoutMillis +
        ", stableTimeoutMillis=" + stableTimeoutMillis +
        '}';
  }

}

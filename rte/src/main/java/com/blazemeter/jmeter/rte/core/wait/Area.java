package com.blazemeter.jmeter.rte.core.wait;

import com.blazemeter.jmeter.rte.core.Position;
import java.util.Objects;

public class Area {

  private final Position topLeft;
  private final Position bottomRight;

  private Area(Position topLeft, Position bottomRight) {
    this.topLeft = topLeft;
    this.bottomRight = bottomRight;
  }

  public static Area fromTopLeftBottomRight(int top, int left, int bottom, int right) {
    return new Area(new Position(top, left), new Position(bottom, right));
  }

  public int getTop() {
    return topLeft.getRow();
  }

  public int getBottom() {
    return bottomRight.getRow();
  }

  public int getLeft() {
    return topLeft.getColumn();
  }

  public int getRight() {
    return bottomRight.getColumn();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Area area = (Area) o;
    return Objects.equals(topLeft, area.topLeft) &&
        Objects.equals(bottomRight, area.bottomRight);
  }

  @Override
  public int hashCode() {
    return Objects.hash(topLeft, bottomRight);
  }

  @Override
  public String toString() {
    return "[" + topLeft + ", " + bottomRight + "]";
  }

}

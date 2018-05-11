package com.blazemeter.jmeter.rte.core;

public class CoordInput {

  private final Position position;
  private final String input;

  public CoordInput(Position pos, String in) {
    position = pos;
    input = in;
  }

  public Position getPosition() {
    return position;
  }

  public String getInput() {
    return input;
  }
}

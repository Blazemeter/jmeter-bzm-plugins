package com.blazemeter.jmeter.rte.core;

public class InvalidFieldPositionException extends IllegalArgumentException {

  public InvalidFieldPositionException(Position position) {
    super("No field at row " + position.getRow() + " and column " + position.getColumn());
  }

}

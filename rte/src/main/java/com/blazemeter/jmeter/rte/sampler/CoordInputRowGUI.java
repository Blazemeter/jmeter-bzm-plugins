package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Position;
import java.io.Serializable;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;

public class CoordInputRowGUI extends AbstractTestElement implements Serializable {

  private static final String INPUT = "CoordInputRowGUI.input";
  private static final String COLUMN = "CoordInputRowGUI.column";
  private static final String ROW = "CoordInputRowGUI.row";

  private static final long serialVersionUID = 4525234536003480135L;

  public CoordInputRowGUI() {
  }

  public CoordInputRowGUI(int row, int column, String input) {
    setRow(row);
    setColumn(column);
    setInput(input);
  }

  public String getInput() {
    return getPropertyAsString(INPUT);
  }

  public void setInput(String input) {
    setProperty(new StringProperty(INPUT, input));
  }

  public int getRow() {
    return getPropertyAsInt(ROW, 1);
  }

  public void setRow(int row) {
    setProperty(new IntegerProperty(ROW, row));
  }

  public int getColumn() {
    return getPropertyAsInt(COLUMN, 1);
  }

  public void setColumn(int column) {
    setProperty(new IntegerProperty(COLUMN, column));
  }

  public CoordInput toCoordInput() {
    return new CoordInput(new Position(getRow(), getColumn()), getInput());
  }

}

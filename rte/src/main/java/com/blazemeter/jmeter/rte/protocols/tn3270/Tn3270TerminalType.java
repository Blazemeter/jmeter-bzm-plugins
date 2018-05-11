package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.TerminalType;
import com.bytezone.dm3270.display.ScreenDimensions;
import java.awt.Dimension;

public class Tn3270TerminalType extends TerminalType {

  private static final String ID_PREFIX = "IBM-3278-";

  private final int model;
  private final boolean extended;
  private final ScreenDimensions screenDimensions;

  public Tn3270TerminalType(DeviceModel model, boolean extended) {
    super(ID_PREFIX + model + (extended ? "-E" : ""), model.screenSize);
    this.screenDimensions = new ScreenDimensions(model.screenSize.height, model.screenSize.width);
    this.model = model.id;
    this.extended = extended;
  }

  public enum DeviceModel {
    M2(2, 24, 80),
    M3(3, 32, 80),
    M4(4, 43, 80),
    M5(5, 27, 132);

    private final int id;
    private final Dimension screenSize;

    DeviceModel(int id, int rows, int columns) {
      this.id = id;
      this.screenSize = new Dimension(columns, rows);
    }

  }

  public int getModel() {
    return model;
  }

  public ScreenDimensions getScreenDimensions() {
    return screenDimensions;
  }

  public boolean isExtended() {
    return extended;
  }

}

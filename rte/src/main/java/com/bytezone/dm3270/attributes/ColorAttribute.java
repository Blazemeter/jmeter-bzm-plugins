package com.bytezone.dm3270.attributes;

import java.awt.Color;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style.
 */
public abstract class ColorAttribute extends Attribute {

  public static final byte COLOR_NEUTRAL1 = 0x00;
  public static final byte COLOR_BLUE = (byte) 0xF1;
  public static final byte COLOR_RED = (byte) 0xF2;
  public static final byte COLOR_PINK = (byte) 0xF3;
  public static final byte COLOR_GREEN = (byte) 0xF4;
  public static final byte COLOR_TURQUOISE = (byte) 0xF5;
  public static final byte COLOR_YELLOW = (byte) 0xF6;
  public static final byte COLOR_NEUTRAL2 = (byte) 0xF7;
  public static final byte COLOR_BLACK = (byte) 0xF8;
  public static final byte COLOR_DEEP_BLUE = (byte) 0xF9;
  public static final byte COLOR_ORANGE = (byte) 0xFA;
  public static final byte COLOR_PURPLE = (byte) 0xFB;
  public static final byte COLOR_PALE_GREEN = (byte) 0xFC;
  public static final byte COLOR_PALE_TURQUOISE = (byte) 0xFD;
  public static final byte COLOR_GREY = (byte) 0xFE;
  public static final byte COLOR_WHITE = (byte) 0xFF;

  public static final Color[] COLORS = //
      {new Color(0.9607843f, 0.9607843f, 0.9607843f),     // WHITESMOKE
          new Color(0.11764706f, 0.5647059f, 1.0f),     // DODGERBLUE
          Color.RED,
          Color.PINK,
          new Color(0.0f, 1.0f, 0.0f),           // LIME
          new Color(0.28235295f, 0.81960785f, 0.8f),      // TURQUOISE
          Color.YELLOW,
          new Color(0.9607843f, 0.9607843f, 0.9607843f),     // WHITESMOKE
          Color.BLACK,
          new Color(0.0f, 0.0f, 0.54509807f),       // DARKBLUE
          Color.ORANGE,
          new Color(0.5019608f, 0.0f, 0.5019608f),  // PURPLE
          new Color(0.59607846f, 0.9843137f, 0.59607846f),      // PALEGREEN
          new Color(0.6862745f, 0.93333334f, 0.93333334f),  // PALETURQUOISE
          Color.GRAY,
          new Color(0.9607843f, 0.9607843f, 0.9607843f),     // WHITESMOKE
      };

  private static final String[] COLOR_NAMES =
      {"Neutral1", "Blue", "Red", "Pink", "Green", "Turquoise", "Yellow", "Neutral2",
          "Black", "Deep blue", "Orange", "Purple", "Pale green", "Pale turquoise", "Grey",
          "White"};

  protected final Color color;

  public ColorAttribute(AttributeType type, byte byteType, byte value) {
    super(type, byteType, value);
    color = COLORS[value & 0x0F];
  }

  public static String getName(Color searchColor) {
    int count = 0;
    for (Color color : COLORS) {
      if (color == searchColor) {
        return COLOR_NAMES[count];
      }
      ++count;
    }
    return searchColor.toString();
  }

  public Color getColor() {
    return color;
  }

  public static String colorName(byte value) {
    return COLOR_NAMES[value & 0x0F];
  }

  @Override
  public String toString() {
    return String.format("%-12s : %02X %-12s", name(), attributeValue, colorName(attributeValue));
  }

}

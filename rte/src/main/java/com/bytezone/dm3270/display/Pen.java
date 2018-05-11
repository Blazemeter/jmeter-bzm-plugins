package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;

/**
 * Interface extracted from dm3270 source code but removing javafx dependencies, unnecessary code
 * and general refactor to comply with code style.
 */
public interface Pen extends Iterable<ScreenPosition> {

  static Pen getInstance(ScreenPosition[] screenPositions,
      ContextManager contextManager, ScreenDimensions screenDimensions) {
    return new PenType1(screenPositions, contextManager, screenDimensions);
  }

  void clearScreen();

  void startField(StartFieldAttribute startFieldAttribute);

  void addAttribute(Attribute attribute);

  int getPosition();

  void writeGraphics(byte b);

  void write(byte b);

  void moveRight();

  void eraseEOF();

  void tab();

  void moveTo(int position);

  int validate(int position);

  void setScreenDimensions(ScreenDimensions screenDimensions);

}

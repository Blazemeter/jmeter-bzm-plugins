package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.attributes.StartFieldAttribute;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style. Additionally, we replace existing standard output
 * with proper logging.
 */
public class PenType1 implements Pen {

  private static final Logger LOG = LoggerFactory.getLogger(PenType1.class);

  private final ScreenPosition[] screenPositions;   // owned by Screen

  private int currentPosition;
  private boolean formattedScreen;
  private ScreenDimensions screenDimensions;

  private final List<Attribute> pendingAttributes = new ArrayList<>();

  // created by Screen and HistoryScreen
  PenType1(ScreenPosition[] screenPositions, ContextManager contextManager,
      ScreenDimensions screenDimensions) {
    this.screenPositions = screenPositions;
    this.screenDimensions = screenDimensions;

    ScreenContext defaultContext = contextManager.getDefaultScreenContext();

    for (int i = 0; i < screenPositions.length; i++) {
      screenPositions[i] = new ScreenPosition(i, defaultContext);
    }
  }

  @Override
  public void clearScreen() {
    for (ScreenPosition screenPosition : screenPositions) {
      screenPosition.reset();
    }

    formattedScreen = false;
  }

  @Override
  public void startField(StartFieldAttribute startFieldAttribute) {
    formattedScreen = true;

    ScreenPosition screenPosition = screenPositions[currentPosition];

    screenPosition.reset();
    screenPosition.setStartField(startFieldAttribute);

    // sometimes a reset attribute is overwritten by a new SFA
    if (pendingAttributes.size() > 0) {
      pendingAttributes.clear();
    }
  }

  @Override
  public void addAttribute(Attribute attribute) {
    pendingAttributes.add(attribute);
  }

  @Override
  public int getPosition() {
    return currentPosition;
  }

  @Override
  public void writeGraphics(byte b) {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    screenPosition.reset();
    screenPosition.setGraphicsChar(b);
    moveRight();
  }

  @Override
  public void write(byte b) {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    screenPosition.reset();
    screenPosition.setChar(b);
    moveRight();
  }

  private void applyAttributes(ScreenPosition screenPosition) {
    for (Attribute attribute : pendingAttributes) {
      screenPosition.addAttribute(attribute);
    }
    pendingAttributes.clear();
  }

  @Override
  public void moveRight() {
    if (pendingAttributes.size() > 0) {
      applyAttributes(screenPositions[currentPosition]);
    }

    currentPosition = validate(currentPosition + 1);
  }

  @Override
  public void eraseEOF() {
    if (!formattedScreen) {
      LOG.debug("No fields to erase");
      return;
    }

    while (true) {
      ScreenPosition screenPosition = screenPositions[currentPosition];
      if (screenPosition.isStartField()) {
        break;
      }
      screenPosition.setChar((byte) 0);
      moveRight();
    }
  }

  @Override
  public void tab() {
    ScreenPosition screenPosition = screenPositions[currentPosition];
    if (screenPosition.isStartField()
        && !screenPosition.getStartFieldAttribute().isProtected()) {
      currentPosition = validate(currentPosition + 1);
      return;
    }
    int next = currentPosition;
    while (true) {
      next = findNextStartPosition(next);
      // wrapped around or not found
      if (next < currentPosition) {
        currentPosition = 0;
        break;
      }
      screenPosition = screenPositions[next];
      if (!screenPosition.getStartFieldAttribute().isProtected()) {
        currentPosition = validate(next + 1);
        break;
      }
    }
  }

  @Override
  public void moveTo(int position) {
    if (pendingAttributes.size() > 0) {
      if (LOG.isDebugEnabled()) {
        List<String> attrs = new ArrayList<>();
        for (Attribute attribute : pendingAttributes) {
          attrs.add(attribute.toString());
        }

        LOG.debug("Unapplied attributes at {}: {}", currentPosition, attrs);
      }
      applyAttributes(screenPositions[currentPosition]);
    }
    currentPosition = validate(position);
  }

  private int findNextStartPosition(int position) {
    int pos = position;
    while (true) {
      pos = validate(pos + 1);
      ScreenPosition screenPosition = screenPositions[pos];

      if (screenPosition.isStartField()) {
        return pos;
      }

      if (pos == position) {
        break;
      }
    }

    LOG.debug("No next start field found: {}", position);
    return -1;
  }

  @Override
  public int validate(int position) {
    while (position < 0) {
      position += screenPositions.length;
    }

    while (position >= screenPositions.length) {
      position -= screenPositions.length;
    }

    return position;
  }

  @Override
  public void setScreenDimensions(ScreenDimensions screenDimensions) {
    this.screenDimensions = screenDimensions;
  }

  // ---------------------------------------------------------------------------------//
  // Debugging
  // ---------------------------------------------------------------------------------//

  @Override
  public Iterator<ScreenPosition> iterator() {
    return new Iterator<ScreenPosition>() {
      private int pos = 0;

      @Override
      public boolean hasNext() {
        return screenPositions.length > pos;
      }

      @Override
      public ScreenPosition next() {
        return screenPositions[pos++];
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Cannot remove an element of an array.");
      }
    };
  }

  @Override
  public String toString() {
    return String.format("[Pos:%d, columns:%d, formatted:%s]", currentPosition,
        screenDimensions.columns, formattedScreen);
  }

}

package com.bytezone.dm3270.display;

import java.awt.Color;

/**
 * Mock of the original class from dm3270 which only uses one screen context.
 * <p/>
 * The implementation of this class in dm3270 has race condition problems due to CONTEXT_POOL not
 * supporting concurrent modifications and, additionally, the list is never cleared which would
 * produce a minor leak (since not that many context are expected for an application).
 * <p/>
 * Since the context attributes are only used in ScreenPosition.draw and we don't need
 */
public class ContextManager {

  private static final ScreenContext DEFAULT_CONTEXT = new ScreenContext();

  public ScreenContext getDefaultScreenContext() {
    return DEFAULT_CONTEXT;
  }

  public ScreenContext getScreenContext(Color foregroundColor, Color backgroundColor,
      byte highlight, boolean highIntensity) {
    return DEFAULT_CONTEXT;
  }

  public ScreenContext setForeground(ScreenContext oldContext, Color foregroundColor) {
    return DEFAULT_CONTEXT;
  }

  public ScreenContext setBackground(ScreenContext oldContext, Color backgroundColor) {
    return DEFAULT_CONTEXT;
  }

  public ScreenContext setHighlight(ScreenContext oldContext, byte highlight) {
    return DEFAULT_CONTEXT;
  }

  private ScreenContext addNewContext(Color foregroundColor, Color backgroundColor,
      byte highlight, boolean highIntensity) {
    return DEFAULT_CONTEXT;
  }

}

package com.bytezone.dm3270.display;

import javafx.scene.text.Font;

/**
 * Patched version of the original class to avoid issues due to dependency to sun internal classes.
 * <p/>
 * If we use the original class we get:
 * <p/>
 * <pre>{@code
 * java.lang.NoSuchMethodError: com.sun.javafx.tk.FontMetrics.computeStringWidth(java.lang.String)
 * }</pre>
 * <p/>
 * When running on JRE 1.8.0_121-b13. We can't use extension of the class and/or reflection here
 * since the code is in the class constructor, and by using shader plugin we replace the original
 * class with this one which does not depend on java internal classes which should not be used by
 * "non jvm" code.
 */
public class FontDetails {

  public final int width;
  public final int height;
  public final int ascent;
  public final int descent;
  public final Font font;

  public final String name;
  public final int size;

  public FontDetails(String name, int size, Font font) {
    this.font = font;
    this.name = name;
    this.size = size;
    /*
    these values were extracted from dm3270 execution by debugging. We set them to provide
    sensitive defaults for rest of the code, even though they should have no effect in the logic of
    the jmeter plugin.
     */
    width = 10;
    ascent = 14;
    descent = 5;
    height = ascent + descent;
  }

  @Override
  public String toString() {
    return String
        .format("[%-18s %d w=%2d, h=%2d, a=%2d, d=%2d]", font.getName(), (int) font.getSize(),
            width, height, ascent, descent);
  }

}

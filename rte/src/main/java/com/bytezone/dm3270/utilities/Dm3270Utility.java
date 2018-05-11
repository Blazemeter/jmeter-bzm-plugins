package com.bytezone.dm3270.utilities;

import java.io.UnsupportedEncodingException;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style.
 */
public class Dm3270Utility {

  //CHECKSTYLE:OFF
  public static final int[] ebc2asc = new int[256];
  //CHECKSTYLE:ON

  private static final String EBCDIC = "CP1047";

  private static final int LINESIZE = 16;

  static {
    byte[] values = new byte[256];
    for (int i = 0; i < 256; i++) {
      values[i] = (byte) i;
    }

    try {
      String s = new String(values, EBCDIC);
      char[] chars = s.toCharArray();
      for (int i = 0; i < 256; i++) {
        int val = chars[i];
        ebc2asc[i] = val;
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public static String getString(byte[] buffer) {
    return getString(buffer, 0, buffer.length);
  }

  public static String getString(byte[] buffer, int offset, int length) {
    try {
      if (offset + length > buffer.length) {
        length = buffer.length - offset - 1;
      }
      return new String(buffer, offset, length, EBCDIC);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return "FAIL";
    }
  }

  public static String getSanitisedString(byte[] buffer, int offset, int length) {
    if (offset + length > buffer.length) {
      length = buffer.length - offset - 1;
    }
    return getString(sanitise(buffer, offset, length));
  }

  private static byte[] sanitise(byte[] buffer, int offset, int length) {
    byte[] cleanBuffer = new byte[length];
    for (int i = 0; i < length; i++) {
      int b = buffer[offset++] & 0xFF;
      cleanBuffer[i] = b < 0x40 ? 0x40 : (byte) b;
    }
    return cleanBuffer;
  }

  public static int unsignedShort(byte[] buffer, int offset) {
    return (buffer[offset] & 0xFF) * 0x100 + (buffer[offset + 1] & 0xFF);
  }

  public static int packUnsignedShort(int value, byte[] buffer, int offset) {
    buffer[offset++] = (byte) ((value >> 8) & 0xFF);
    buffer[offset++] = (byte) (value & 0xFF);

    return offset;
  }

  public static int unsignedLong(byte[] buffer, int offset) {
    return (buffer[offset] & 0xFF) * 0x1000000 + (buffer[offset + 1] & 0xFF) * 0x10000
        + (buffer[offset + 2] & 0xFF) * 0x100 + (buffer[offset + 3] & 0xFF);
  }

  public static String toHex(byte[] b) {
    return toHex(b, 0, b.length);
  }

  public static String toHex(byte[] b, int offset, int length) {
    return toHex(b, offset, length, true);
  }

  public static String toHex(byte[] b, int offset, int length, boolean ebcdic) {
    StringBuilder text = new StringBuilder();

    try {
      for (int ptr = offset, max = offset + length; ptr < max; ptr += LINESIZE) {
        final StringBuilder hexLine = new StringBuilder();
        final StringBuilder textLine = new StringBuilder();
        for (int linePtr = 0; linePtr < LINESIZE; linePtr++) {
          if (ptr + linePtr >= max) {
            break;
          }

          int val = b[ptr + linePtr] & 0xFF;
          hexLine.append(String.format("%02X ", val));

          if (ebcdic) {
            if (val < 0x40 || val == 0xFF) {
              textLine.append('.');
            } else {
              textLine.append(new String(b, ptr + linePtr, 1, EBCDIC));
            }
          } else if (val < 0x20 || val >= 0xF0) {
            textLine.append('.');
          } else {
            textLine.append(new String(b, ptr + linePtr, 1));
          }
        }
        text.append(String.format("%04X  %-48s %s%n", ptr, hexLine.toString(),
            textLine.toString()));
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    if (text.length() > 0) {
      text.deleteCharAt(text.length() - 1);
    }

    return text.toString();
  }

  public static void hexDump(byte[] b) {
    System.out.println(toHex(b, 0, b.length));
  }

  public static void printStackTrace() {
    for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
      System.out.println(ste);
    }
  }

}

package com.bytezone.dm3270.commands;

import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldManager;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.orders.BufferAddressSource;
import com.bytezone.dm3270.orders.Order;
import com.bytezone.dm3270.orders.SetBufferAddressOrder;
import com.bytezone.dm3270.orders.TextOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Class extracted from dm3270 source code but removing unnecessary code and doing general refactor
 * to comply with code style. Additionally, add support for SYSREQ command.
 */

public class AIDCommand extends Command implements BufferAddressSource, Iterable<Order> {

  public static final byte NO_AID_SPECIFIED = 0x60;
  public static final byte AID_STRUCTURED_FIELD = (byte) 0x88;
  public static final byte AID_PA3 = 0x6B;
  public static final byte AID_PA1 = 0x6C;
  public static final byte AID_CLEAR = 0x6D;
  public static final byte AID_PA2 = 0x6E;
  public static final byte AID_ENTER = 0x7D;
  public static final byte AID_PF1 = (byte) 0xF1;
  public static final byte AID_PF2 = (byte) 0xF2;
  public static final byte AID_PF3 = (byte) 0xF3;
  public static final byte AID_PF4 = (byte) 0xF4;
  public static final byte AID_PF5 = (byte) 0xF5;
  public static final byte AID_PF6 = (byte) 0xF6;
  public static final byte AID_PF7 = (byte) 0xF7;
  public static final byte AID_PF8 = (byte) 0xF8;
  public static final byte AID_PF9 = (byte) 0xF9;
  public static final byte AID_PF10 = (byte) 0x7A;
  public static final byte AID_PF11 = (byte) 0x7B;
  public static final byte AID_PF12 = (byte) 0x7C;
  public static final byte AID_PF13 = (byte) 0xC1;
  public static final byte AID_PF14 = (byte) 0xC2;
  public static final byte AID_PF15 = (byte) 0xC3;
  public static final byte AID_PF16 = (byte) 0xC4;
  public static final byte AID_PF17 = (byte) 0xC5;
  public static final byte AID_PF18 = (byte) 0xC6;
  public static final byte AID_PF19 = (byte) 0xC7;
  public static final byte AID_PF20 = (byte) 0xC8;
  public static final byte AID_PF21 = (byte) 0xC9;
  public static final byte AID_PF22 = (byte) 0x4A;
  public static final byte AID_PF23 = (byte) 0x4B;
  public static final byte AID_PF24 = (byte) 0x4C;
  public static final byte AID_SYSREQ = (byte) 0xF0;

  private static final byte AID_READ_PARTITION = 0x61;
  private static final byte AID_CLEAR_PARTITION = 0x6A;

  private static byte[] keys =
      {0, NO_AID_SPECIFIED, AID_ENTER, AID_PF1, AID_PF2, AID_PF3, AID_PF4, AID_PF5,
          AID_PF6, AID_PF7, AID_PF8, AID_PF9, AID_PF10, AID_PF11, AID_PF12, AID_PF13,
          AID_PF14, AID_PF15, AID_PF16, AID_PF17, AID_PF18, AID_PF19, AID_PF20, AID_PF21,
          AID_PF22, AID_PF23, AID_PF24, AID_PA1, AID_PA2, AID_PA3, AID_CLEAR,
          AID_CLEAR_PARTITION, AID_READ_PARTITION, AID_SYSREQ};

  private static String[] keyNames =
      {"Not found", "No AID", "ENTR", "PF1", "PF2", "PF3", "PF4", "PF5", "PF6", "PF7",
          "PF8", "PF9", "PF10", "PF11", "PF12", "PF13", "PF14", "PF15", "PF16", "PF17",
          "PF18", "PF19", "PF20", "PF21", "PF22", "PF23", "PF24", "PA1", "PA2", "PA3",
          "CLR", "CLR Partition", "Read Partition", "System Request"};

  private int key;
  private byte keyCommand;
  private BufferAddress cursorAddress;

  private final List<ModifiedField> modifiedFields = new ArrayList<>();
  private final List<Order> orders = new ArrayList<>();
  private final List<Order> textOrders = new ArrayList<>();

  public AIDCommand(byte[] buffer, int offset, int length) {
    super(buffer, offset, length);       // copies buffer[offset:length] to data[]

    keyCommand = data[0];
    key = findKey(keyCommand);

    if (length <= 1) {
      cursorAddress = null;
      return;
    }

    cursorAddress = new BufferAddress(data[1], data[2]);

    int ptr = 3;
    Order previousOrder = null;
    ModifiedField currentAIDField = null;

    while (ptr < length) {
      Order order = Order.getOrder(data, ptr, length);
      if (!order.rejected()) {
        if (previousOrder != null && previousOrder.matchesPreviousOrder(order)) {
          previousOrder.incrementDuplicates();
        } else {
          orders.add(order);
          previousOrder = order;
        }

        if (order instanceof SetBufferAddressOrder) {
          currentAIDField = new ModifiedField((SetBufferAddressOrder) order);
          modifiedFields.add(currentAIDField);
        } else if (currentAIDField != null) {
          currentAIDField.addOrder(order);
        }

        if (order instanceof TextOrder) {
          textOrders.add(order);
        }
      }
      ptr += order.size();
    }
  }

  public boolean matches(AIDCommand otherCommand) {
    if (data.length != otherCommand.data.length) {
      return false;
    }

    // skip cursor position
    for (int i = 3; i < data.length; i++) {
      if (data[i] != otherCommand.data[i]) {
        return false;
      }
    }

    return true;
  }

  private int findKey(byte keyCommand) {
    // starts at offset 1
    for (int i = 1; i < keys.length; i++) {
      if (keys[i] == keyCommand) {
        return i;
      }
    }
    return 0;
  }

  // copy modified fields back to the screen - only used in Replay mode
  // Normally an AID is a reply command (which never has process() called)
  // Testing out whether the plugin reply should pass through here.
  @Override
  public void process(Screen screen) {
    if (!prettyMoveHandled(screen)) {
      FieldManager fieldManager = screen.getFieldManager();

      for (ModifiedField aidField : modifiedFields) {
        Optional<Field> optField = fieldManager.getFieldAt(aidField.getLocation());
        if (!optField.isPresent()) {
          continue;                 // in replay mode we cannot rely on the fields list
        }

        Field field = optField.get();
        if (aidField.hasData()) {
          field.setText(aidField.getBuffer());
        } else {
          field.erase();
        }
      }
    }

    // place cursor in new location
    if (cursorAddress != null) {
      screen.getScreenCursor().moveTo(cursorAddress.getLocation());
    }

    screen.lockKeyboard(keyNames[key]);
  }

  // test to see whether this is data entry that was null suppressed into moving
  // elsewhere on the screen (like the TSO logoff command) - purely aesthetic
  private boolean prettyMoveHandled(Screen screen) {
    if (modifiedFields.size() == 1) {
      Cursor cursor = screen.getScreenCursor();
      Field currentField = cursor.getCurrentField();
      if (currentField != null) {
        int cursorOldLocation = cursor.getLocation();
        if (cursorOldLocation != currentField.getFirstLocation()
            && currentField.contains(cursorOldLocation)) {
          int cursorDistance = cursorAddress.getLocation() - cursorOldLocation;
          byte[] buffer = modifiedFields.get(0).getBuffer();
          if (buffer.length == cursorDistance) {
            // cannot call field.setText() as the data starts mid-field
            for (byte b : buffer) {
              cursor.typeChar(b);        // send characters through the old cursor
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public BufferAddress getBufferAddress() {
    return cursorAddress;
  }

  @Override
  public String getName() {
    return "AID : " + keyNames[key];
  }

  public byte[] getText(int index) {
    return textOrders.get(index).getBuffer();
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    text.append(String.format("AID     : %-12s : %02X%n", keyNames[key], keyCommand));

    if (cursorAddress != null) {
      text.append(String.format("Cursor  : %s%n", cursorAddress));
    }

    if (modifiedFields.size() > 0) {
      text.append(String.format("%nModified fields  : %d", modifiedFields.size()));
      for (ModifiedField aidField : modifiedFields) {
        text.append("\nField   : ");
        text.append(aidField);
      }
      // response to a read buffer request
    } else if (orders.size() > 0) {
      text.append(String.format("%nOrders  : %d%n",
          orders.size() - textOrders.size()));
      text.append(String.format("Text    : %d%n", textOrders.size()));

      // if the list begins with a TextOrder then tab out the missing columns
      if (orders.size() > 0 && orders.get(0) instanceof TextOrder) {
        text.append(String.format("%40s", ""));
      }

      for (Order order : orders) {
        String fmt = (order instanceof TextOrder) ? "%s" : "%n%-40s";
        text.append(String.format(fmt, order));
      }
    }
    return text.toString();
  }

  // This class is used to collect information about each modified field specified
  // in the AIDCommand.
  private class ModifiedField {

    private SetBufferAddressOrder sbaOrder;
    private List<Order> orders = new ArrayList<>();

    private ModifiedField(SetBufferAddressOrder sbaOrder) {
      this.sbaOrder = sbaOrder;
    }

    private void addOrder(Order order) {
      orders.add(order);
    }

    public int getLocation() {
      return sbaOrder.getBufferAddress().getLocation();
    }

    private boolean hasData() {
      return getBuffer().length > 0;
    }

    public byte[] getBuffer() {
      for (Order order : orders) {
        if (order instanceof TextOrder) {
          return order.getBuffer();        // assumes only one
        }
      }
      return new byte[0];
    }

    @Override
    public String toString() {
      StringBuilder text = new StringBuilder();
      text.append(String.format("%-40s", sbaOrder));
      for (Order order : orders) {
        if (!(order instanceof TextOrder)) {
          text.append(String.format("\n        : %-40s", order));
        } else {
          text.append(order);
        }
      }
      return text.toString();
    }
  }

  @Override
  public Iterator<Order> iterator() {
    return orders.iterator();
  }

}

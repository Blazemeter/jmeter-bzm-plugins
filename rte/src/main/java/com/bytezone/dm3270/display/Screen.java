package com.bytezone.dm3270.display;

import static com.bytezone.dm3270.commands.AIDCommand.NO_AID_SPECIFIED;

import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.commands.SystemMessage;
import com.bytezone.dm3270.commands.WriteControlCharacter;
import com.bytezone.dm3270.orders.BufferAddress;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.structuredfields.SetReplyModeSF;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style. Additionally, we replace existing standard output
 * with proper logging.
 * <p/>
 * We also override some of the logic of this class in
 * <pre>{@link com.blazemeter.jmeter.rte.protocols.tn3270.SilentScreen}</pre>
 * to keep plugin specific logic to handle sound alarms separated from base code with general
 * modifications.
 */
public class Screen implements DisplayScreen {

  private static final Logger LOG = LoggerFactory.getLogger(Screen.class);

  private static final byte[] SAVE_SCREEN_REPLY_TYPES =
      {Attribute.XA_HIGHLIGHTING, Attribute.XA_FGCOLOR, Attribute.XA_CHARSET,
          Attribute.XA_BGCOLOR, Attribute.XA_TRANSPARENCY};

  private final ScreenPosition[] screenPositions;
  private final FieldManager fieldManager;
  private final ScreenPacker screenPacker;
  private final SystemMessage systemMessage;

  private final PluginsStage pluginsStage;
  private final TelnetState telnetState;

  private final ScreenDimensions defaultScreenDimensions;
  private ScreenDimensions alternateScreenDimensions;

  private final Pen pen;
  private final Cursor cursor;
  private ScreenOption currentScreen;

  private byte currentAID;
  private byte replyMode;
  private byte[] replyTypes = new byte[0];

  private int insertedCursorPosition = -1;
  private boolean keyboardLocked;
  private boolean insertMode;
  private boolean readModifiedAll = false;

  private final Set<KeyboardStatusListener> keyboardChangeListeners = new HashSet<>();

  public enum ScreenOption {
    DEFAULT, ALTERNATE
  }

  public Screen(ScreenDimensions defaultScreenDimensions,
      ScreenDimensions alternateScreenDimensions, TelnetState telnetState) {
    this.defaultScreenDimensions = defaultScreenDimensions;
    this.alternateScreenDimensions = alternateScreenDimensions;
    this.telnetState = telnetState;

    ScreenDimensions screenDimensions = alternateScreenDimensions == null
        ? defaultScreenDimensions : alternateScreenDimensions;

    cursor = new Cursor(this, screenDimensions);

    ContextManager contextManager = new ContextManager();
    fieldManager = new FieldManager(this, contextManager, screenDimensions);

    systemMessage = new SystemMessage();

    screenPositions = new ScreenPosition[screenDimensions.size];
    pen = Pen.getInstance(screenPositions, contextManager, screenDimensions);

    screenPacker = new ScreenPacker(pen, fieldManager);

    fieldManager.addScreenChangeListener(screenPacker);

    setCurrentScreen(ScreenOption.DEFAULT);

    pluginsStage = new PluginsStage();
  }

  public SystemMessage getSystemMessage() {
    return systemMessage;
  }

  public TelnetState getTelnetState() {
    return telnetState;
  }

  public void setCurrentScreen(ScreenOption value) {
    if (currentScreen == value) {
      return;
    }

    currentScreen = value;
    ScreenDimensions screenDimensions = getScreenDimensions();

    cursor.setScreenDimensions(screenDimensions);
    pen.setScreenDimensions(screenDimensions);
    fieldManager.setScreenDimensions(screenDimensions);

    BufferAddress.setScreenWidth(screenDimensions.columns);
  }

  @Override
  public ScreenDimensions getScreenDimensions() {
    return currentScreen == ScreenOption.DEFAULT ? defaultScreenDimensions
        : alternateScreenDimensions;
  }

  public void setConsolePane(ConsolePane consolePane) {
    addKeyboardStatusChangeListener(consolePane);
  }

  public FieldManager getFieldManager() {
    return fieldManager;
  }

  public String getPrefix() {
    return fieldManager.getScreenWatcher().getPrefix();
  }

  public PluginsStage getPluginsStage() {
    return pluginsStage;
  }

  public Cursor getScreenCursor() {
    return cursor;
  }

  public void resetInsertMode() {
    if (insertMode) {
      toggleInsertMode();
    }
  }

  public void toggleInsertMode() {
    insertMode = !insertMode;
    fireKeyboardStatusChange("");
  }

  public boolean isInsertMode() {
    return insertMode;
  }

  public void eraseAllUnprotected() {
    Optional<Field> firstUnprotectedField = fieldManager.eraseAllUnprotected();

    restoreKeyboard();         // resets the AID to NO_AID_SPECIFIED
    resetModified();
    draw();

    firstUnprotectedField
        .ifPresent(screenPositions1 -> cursor.moveTo(screenPositions1.getFirstLocation()));
  }

  public void buildFields(WriteControlCharacter wcc) {
    fieldManager.buildFields(screenPositions);        // what about resetModified?
  }

  public void checkRecording() {
    byte savedReplyMode = replyMode;
    byte[] savedReplyTypes = replyTypes;
    setReplyMode(SetReplyModeSF.RM_CHARACTER, SAVE_SCREEN_REPLY_TYPES);
    setReplyMode(savedReplyMode, savedReplyTypes);
  }

  public void draw() {
    if (insertedCursorPosition >= 0) {
      cursor.moveTo(insertedCursorPosition);
      insertedCursorPosition = -1;
      cursor.setVisible(true);
    }
  }

  public void drawPosition(int position, boolean hasCursor) {
  }

  public Optional<Field> getHomeField() {
    List<Field> fields = fieldManager.getUnprotectedFields();
    if (fields != null && fields.size() > 0) {
      return Optional.of(fields.get(0));
    }
    return Optional.empty();
  }

  public void setAID(byte aid) {
    currentAID = aid;
  }

  public void setReplyMode(byte replyMode, byte[] replyTypes) {
    this.replyMode = replyMode;
    this.replyTypes = replyTypes;
  }

  public void setFieldText(Field field, String text) {
    try {
      field.setText(text.getBytes("CP1047"));
      field.setModified(true);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  // ---------------------------------------------------------------------------------//
  // DisplayScreen interface methods
  // ---------------------------------------------------------------------------------//

  @Override
  public Pen getPen() {
    return pen;
  }

  @Override
  public ScreenPosition getScreenPosition(int position) {
    return screenPositions[position];
  }

  @Override
  public ScreenPosition[] getScreenPositions() {
    return null;
  }

  @Override
  public int validate(int position) {
    return pen.validate(position);
  }

  @Override
  public void clearScreen() {
    cursor.moveTo(0);
    pen.clearScreen();
    fieldManager.reset();
  }

  @Override
  public void insertCursor(int position) {
    insertedCursorPosition = position;                // move it here later
  }

  // ---------------------------------------------------------------------------------//
  // Convert screen contents to an AID command
  // ---------------------------------------------------------------------------------//

  public AIDCommand readModifiedFields() {
    return screenPacker.readModifiedFields(currentAID, getScreenCursor().getLocation(),
        readModifiedAll);
  }

  public AIDCommand readModifiedFields(byte type) {
    switch (type) {
      case Command.READ_MODIFIED_F6:
        return readModifiedFields();

      case Command.READ_MODIFIED_ALL_6E:
        readModifiedAll = true;
        AIDCommand command = readModifiedFields();
        readModifiedAll = false;
        return command;

      default:
        LOG.warn("Unknown type {}", type);
    }

    return null;
  }

  public AIDCommand readBuffer() {
    return screenPacker.readBuffer(currentAID, getScreenCursor().getLocation(),
        replyMode, replyTypes);
  }

  // ---------------------------------------------------------------------------------//
  // Events to be processed from WriteControlCharacter.process()
  // ---------------------------------------------------------------------------------//

  /*
  following two methods are provided to comply with the contract expected from the rest of the code
   */
  public void resetPartition() {
  }

  public void soundAlarm() {
  }

  public void restoreKeyboard() {
    setAID(NO_AID_SPECIFIED);
    cursor.setVisible(true);
    keyboardLocked = false;
    fireKeyboardStatusChange("");
  }

  public void lockKeyboard(String keyName) {
    keyboardLocked = true;
    fireKeyboardStatusChange(keyName);
    cursor.setVisible(false);
  }

  public void resetModified() {
    fieldManager.getUnprotectedFields().forEach(f -> f.setModified(false));
  }

  public boolean isKeyboardLocked() {
    return keyboardLocked;
  }

  // ---------------------------------------------------------------------------------//
  // Listener events
  // ---------------------------------------------------------------------------------//

  private void fireKeyboardStatusChange(String keyName) {
    KeyboardStatusChangedEvent evt =
        new KeyboardStatusChangedEvent(insertMode, keyboardLocked, keyName);
    keyboardChangeListeners.forEach(l -> l.keyboardStatusChanged(evt));
  }

  public void addKeyboardStatusChangeListener(KeyboardStatusListener listener) {
    keyboardChangeListeners.add(listener);
  }

  public void removeKeyboardStatusChangeListener(KeyboardStatusListener listener) {
    keyboardChangeListeners.remove(listener);
  }

}

package com.bytezone.dm3270.application;

import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.FieldChangeListener;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.extended.CommandHeader;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.streams.TerminalServer;
import com.bytezone.dm3270.utilities.Dm3270Utility;
import com.bytezone.dm3270.utilities.Site;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style.
 * <p/>
 * We also override some of the logic of this class in
 * <pre>{@link com.blazemeter.jmeter.rte.protocols.tn3270.ExtendedConsolePane}</pre>
 * to keep project dependencies (ssl dependencies) out of the scope of dm3270 source code for the
 * time being.
 */
public class ConsolePane implements FieldChangeListener, CursorMoveListener,
    KeyboardStatusListener {

  private final Screen screen;

  private final TelnetState telnetState;
  private int commandHeaderCount;
  private final Site server;

  private TerminalServer terminalServer;
  private Thread terminalServerThread;

  public ConsolePane(Screen screen, Site server) {
    this.screen = screen;
    this.telnetState = screen.getTelnetState();
    this.server = server;

    screen.setConsolePane(this);
    screen.getScreenCursor().addFieldChangeListener(this);
    screen.getScreenCursor().addCursorMoveListener(this);
  }

  public void sendAID(byte aid, String name) {
    if (screen.isInsertMode()) {
      screen.toggleInsertMode();
    }

    screen.lockKeyboard(name);
    screen.setAID(aid);

    AIDCommand command = screen.readModifiedFields();
    sendAID(command);
  }

  private void sendAID(AIDCommand command) {
    assert telnetState != null;

    if (telnetState.does3270Extended()) {
      byte[] buffer = new byte[5];
      Dm3270Utility.packUnsignedShort(commandHeaderCount++, buffer, 3);
      CommandHeader header = new CommandHeader(buffer);
      TN3270ExtendedCommand extendedCommand = new TN3270ExtendedCommand(header, command);
      telnetState.write(extendedCommand.getTelnetData());
    } else {
      telnetState.write(command.getTelnetData());
    }
  }

  public void connect() {
    if (server == null) {
      throw new IllegalArgumentException("Server must not be null");
    }

    // set preferences for this session
    telnetState.setDo3270Extended(server.getExtended());
    telnetState.setDoTerminalType(true);

    TelnetListener telnetListener = new TelnetListener(screen, telnetState);
    terminalServer =
        new TerminalServer(server.getURL(), server.getPort(), telnetListener);
    telnetState.setTerminalServer(terminalServer);

    terminalServerThread = new Thread(terminalServer);
    terminalServerThread.start();
  }

  public void disconnect() {
    if (terminalServer != null) {
      terminalServer.close();
    }

    telnetState.close();

    if (terminalServerThread != null) {
      terminalServerThread.interrupt();
      try {
        terminalServerThread.join();
      } catch (InterruptedException e) {
        /*
        Even though this could be improved, we leave it as is because it is not used by the plugin
        */
        e.printStackTrace();
      }
    }
  }

  @Override
  public void fieldChanged(Field oldField, Field newField) {
  }

  @Override
  public void cursorMoved(int oldLocation, int newLocation, Field currentField) {
    fieldChanged(currentField, currentField);            // update the acronym
  }

  @Override
  public void keyboardStatusChanged(KeyboardStatusChangedEvent evt) {
  }

}

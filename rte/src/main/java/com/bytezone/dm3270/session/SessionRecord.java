package com.bytezone.dm3270.session;

import com.bytezone.dm3270.buffers.ReplyBuffer;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.extended.TN3270ExtendedCommand;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style.
 */
public class SessionRecord {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm:ss.S");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("mm:ss");
  private final ReplyBuffer message;

  private final Source source;
  private final SessionRecordType sessionRecordType;
  private final LocalDateTime dateTime;

  private String time;

  public enum SessionRecordType {
    TELNET, TN3270, TN3270E
  }

  public SessionRecord(SessionRecordType sessionRecordType, ReplyBuffer message,
      Source source, LocalDateTime dateTime) {
    this.sessionRecordType = sessionRecordType;
    this.message = message;
    this.source = source;
    this.dateTime = dateTime;

    if (dateTime != null) {
      setTime(TIME_FORMATTER.format(dateTime));
    }
  }

  public boolean isTelnet() {
    return sessionRecordType == SessionRecordType.TELNET;
  }

  public boolean isCommand() {
    return message instanceof Command || message instanceof TN3270ExtendedCommand;
  }

  public Command getCommand() {
    if (message instanceof Command) {
      return (Command) message;
    }
    if (message instanceof TN3270ExtendedCommand) {
      return ((TN3270ExtendedCommand) message).getCommand();
    }
    return null;
  }

  public ReplyBuffer getMessage() {
    return message;
  }

  public byte[] getBuffer() {
    return message.getData();
  }

  public int size() {
    return message.size();
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getTime() {
    return time;
  }

  @Override
  public String toString() {
    return String.format("%s : %s", source, FORMATTER.format(dateTime));
  }

}

package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.helger.commons.annotation.VisibleForTesting;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTESampler extends AbstractSampler implements ThreadListener, LoopIterationListener {

  public static final String CONFIG_PORT = "RTEConnectionConfig.port";
  public static final String CONFIG_SERVER = "RTEConnectionConfig.server";
  public static final String CONFIG_PROTOCOL = "RTEConnectionConfig.protocol";
  public static final String CONFIG_SSL_TYPE = "RTEConnectionConfig.sslType";
  public static final String CONFIG_CONNECTION_TIMEOUT = "RTEConnectionConfig.connectTimeout";
  public static final String CONFIG_TERMINAL_TYPE = "RTEConnectionConfig.terminalType";
  public static final int DEFAULT_PORT = 23;
  public static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 60000;
  public static final Mode DEFAULT_MODE = Mode.SEND_INPUT;
  public static final Action DEFAULT_ACTION = Action.ENTER;
  public static final Protocol DEFAULT_PROTOCOL = Protocol.TN5250;
  public static final TerminalType DEFAULT_TERMINAL_TYPE = DEFAULT_PROTOCOL.createProtocolClient()
      .getDefaultTerminalType();
  public static final SSLType DEFAULT_SSLTYPE = SSLType.NONE;
  @VisibleForTesting
  protected static final long DEFAULT_STABLE_TIMEOUT_MILLIS = 1000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS = 60000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_SILENT_TIME_MILLIS = 1000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS = 60000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS = 30000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS = 30000;


  //If users wants to change Stable Timeout value it should be specified in
  // jmeter.properties by adding a line like ths one:
  // "RTEConnectionConfig.stableTimeoutMillis=value"
  private static final String CONFIG_STABLE_TIMEOUT = "RTEConnectionConfig.stableTimeoutMillis";
  private static final String MODE_PROPERTY = "RTESampler.mode";
  private static final String REUSE_CONNECTIONS_PROPERTY = "RTESampler.reuseConnections";
  private static final String ACTION_PROPERTY = "RTESampler.action";
  private static final String WAIT_SYNC_PROPERTY = "RTESampler.waitSync";
  private static final String WAIT_SYNC_TIMEOUT_PROPERTY = "RTESampler.waitSyncTimeout";
  private static final String WAIT_CURSOR_PROPERTY = "RTESampler.waitCursor";
  private static final String WAIT_CURSOR_ROW_PROPERTY = "RTESampler.waitCursorRow";
  private static final String WAIT_CURSOR_COLUMN_PROPERTY = "RTESampler.waitCursorColumn";
  private static final String WAIT_CURSOR_TIMEOUT_PROPERTY = "RTESampler.waitCursorTimeout";
  private static final String WAIT_SILENT_PROPERTY = "RTESampler.waitSilent";
  private static final String WAIT_SILENT_TIME_PROPERTY = "RTESampler.waitSilentTime";
  private static final String WAIT_SILENT_TIMEOUT_PROPERTY = "RTESampler.waitSilentTimeout";
  private static final String WAIT_TEXT_PROPERTY = "RTESampler.waitText";
  private static final String WAIT_TEXT_REGEX_PROPERTY = "RTESampler.waitTextRegex";
  private static final String WAIT_TEXT_AREA_TOP_PROPERTY = "RTESampler.waitTextAreaTop";
  private static final String WAIT_TEXT_AREA_LEFT_PROPERTY = "RTESampler.waitTextAreaLeft";
  private static final String WAIT_TEXT_AREA_BOTTOM_PROPERTY = "RTESampler.waitTextAreaBottom";
  private static final String WAIT_TEXT_AREA_RIGHT_PROPERTY = "RTESampler.waitTextAreaRight";
  private static final String WAIT_TEXT_TIMEOUT_PROPERTY = "RTESampler.waitTextTimeout";

  private static final Logger LOG = LoggerFactory.getLogger(RTESampler.class);
  private static ThreadLocal<Map<String, RteProtocolClient>> connections = ThreadLocal
      .withInitial(HashMap::new);

  private final transient Function<Protocol, RteProtocolClient> protocolFactory;

  public RTESampler() {
    this(Protocol::createProtocolClient);
  }

  public RTESampler(Function<Protocol, RteProtocolClient> protocolFactory) {
    setName("RTE");
    this.protocolFactory = protocolFactory;
  }

  @Override
  public String getName() {
    return getPropertyAsString(TestElement.NAME);
  }

  @Override
  public void setName(String name) {
    if (name != null) {
      setProperty(TestElement.NAME, name);
    }
  }

  private Protocol getProtocol() {
    return Protocol.valueOf(getPropertyAsString(CONFIG_PROTOCOL));
  }

  private String getServer() {
    return getPropertyAsString(CONFIG_SERVER);
  }

  private int getPort() {
    return getPropertyAsInt(CONFIG_PORT, DEFAULT_PORT);
  }

  private TerminalType getTerminalType() {
    return getProtocol().createProtocolClient()
        .getTerminalTypeById(getPropertyAsString(CONFIG_TERMINAL_TYPE));
  }

  private long getConnectionTimeout() {
    return getPropertyAsLong(CONFIG_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
  }

  private long getStableTimeout() {
    return JMeterUtils.getPropDefault(CONFIG_STABLE_TIMEOUT, DEFAULT_STABLE_TIMEOUT_MILLIS);
  }

  @VisibleForTesting
  protected void setStableTimeout(Long timeoutMillis) {
    if (timeoutMillis == null) {
      JMeterUtils.getJMeterProperties().remove(CONFIG_STABLE_TIMEOUT);
    } else {
      JMeterUtils.setProperty(CONFIG_STABLE_TIMEOUT, String.valueOf(timeoutMillis));
    }
  }

  private boolean isReuseConnections() {
    return JMeterUtils.getPropDefault(REUSE_CONNECTIONS_PROPERTY, false);
  }

  @VisibleForTesting
  protected void setReuseConnections(boolean doReuse) {
    JMeterUtils.setProperty(REUSE_CONNECTIONS_PROPERTY, Boolean.toString(doReuse));
  }

  private SSLType getSSLType() {
    return SSLType.valueOf(getPropertyAsString(CONFIG_SSL_TYPE));
  }

  @VisibleForTesting
  protected void setSslType(SSLType sslType) {
    setProperty(CONFIG_SSL_TYPE, sslType.name());
  }

  public Mode getMode() {

    if (getPropertyAsString(MODE_PROPERTY).isEmpty()) {
      return DEFAULT_MODE;
    }
    return Mode.valueOf(getPropertyAsString(MODE_PROPERTY));
  }

  public void setMode(Mode mode) {
    setProperty(MODE_PROPERTY, mode.name());
  }

  public void setPayload(Inputs payload) {
    setProperty(new TestElementProperty(Inputs.INPUTS_PROPERTY, payload));
  }

  public Action getAction() {
    if (getPropertyAsString(ACTION_PROPERTY).isEmpty()) {
      return DEFAULT_ACTION;
    }
    return Action.valueOf(getPropertyAsString(ACTION_PROPERTY));
  }

  public void setAction(Action action) {
    setProperty(ACTION_PROPERTY, action.name());
  }

  public boolean getWaitSync() {
    return getPropertyAsBoolean(WAIT_SYNC_PROPERTY, true);
  }

  public void setWaitSync(boolean waitSync) {
    setProperty(WAIT_SYNC_PROPERTY, waitSync);
  }

  public String getWaitSyncTimeout() {
    return getPropertyAsString(WAIT_SYNC_TIMEOUT_PROPERTY, "" + DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  public void setWaitSyncTimeout(String waitTimeoutSync) {
    setProperty(WAIT_SYNC_TIMEOUT_PROPERTY, waitTimeoutSync);
  }

  private long getWaitSyncTimeoutValue() {
    return getPropertyAsLong(WAIT_SYNC_TIMEOUT_PROPERTY, DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  public boolean getWaitCursor() {
    return getPropertyAsBoolean(WAIT_CURSOR_PROPERTY);
  }

  public void setWaitCursor(boolean waitCursor) {
    setProperty(WAIT_CURSOR_PROPERTY, waitCursor);
  }

  private long getWaitCursorTimeoutValue() {
    return getPropertyAsLong(WAIT_CURSOR_TIMEOUT_PROPERTY, DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS);
  }

  public String getWaitCursorRow() {
    return getPropertyAsString(WAIT_CURSOR_ROW_PROPERTY, String.valueOf(1));
  }

  public void setWaitCursorRow(String row) {
    setProperty(WAIT_CURSOR_ROW_PROPERTY, row);
  }

  private int getWaitCursorRowValue() {
    return getPropertyAsInt(WAIT_CURSOR_ROW_PROPERTY, 1);
  }

  public String getWaitCursorColumn() {
    return getPropertyAsString(WAIT_CURSOR_COLUMN_PROPERTY, String.valueOf(1));
  }

  public void setWaitCursorColumn(String row) {
    setProperty(WAIT_CURSOR_COLUMN_PROPERTY, row);
  }

  private int getWaitCursorColumnValue() {
    return getPropertyAsInt(WAIT_CURSOR_COLUMN_PROPERTY, 1);
  }

  public String getWaitCursorTimeout() {
    return getPropertyAsString(WAIT_CURSOR_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS));
  }

  public void setWaitCursorTimeout(String waitTimeoutCursor) {
    setProperty(WAIT_CURSOR_TIMEOUT_PROPERTY, waitTimeoutCursor);
  }

  public boolean getWaitSilent() {
    return getPropertyAsBoolean(WAIT_SILENT_PROPERTY);
  }

  public void setWaitSilent(boolean waitSilent) {
    setProperty(WAIT_SILENT_PROPERTY, waitSilent);
  }

  public String getWaitSilentTime() {
    return getPropertyAsString(WAIT_SILENT_TIME_PROPERTY,
        String.valueOf(DEFAULT_WAIT_SILENT_TIME_MILLIS));
  }

  public void setWaitSilentTime(String waitSilentTime) {
    setProperty(WAIT_SILENT_TIME_PROPERTY, waitSilentTime);
  }

  private long getWaitSilentTimeValue() {
    return getPropertyAsLong(WAIT_SILENT_TIME_PROPERTY, DEFAULT_WAIT_SILENT_TIME_MILLIS);
  }

  public String getWaitSilentTimeout() {
    return getPropertyAsString(WAIT_SILENT_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS));
  }

  public void setWaitSilentTimeout(String waitSilentTimeout) {
    setProperty(WAIT_SILENT_TIMEOUT_PROPERTY, waitSilentTimeout);
  }

  private long getWaitSilentTimeoutValue() {
    return getPropertyAsLong(WAIT_SILENT_TIMEOUT_PROPERTY, DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS);
  }

  public boolean getWaitText() {
    return getPropertyAsBoolean(WAIT_TEXT_PROPERTY);
  }

  public void setWaitText(boolean waitText) {
    setProperty(WAIT_TEXT_PROPERTY, waitText);
  }

  public String getWaitTextRegex() {
    return getPropertyAsString(WAIT_TEXT_REGEX_PROPERTY);
  }

  public void setWaitTextRegex(String regex) {
    setProperty(WAIT_TEXT_REGEX_PROPERTY, regex);
  }

  public String getWaitTextAreaTop() {
    return getPropertyAsString(WAIT_TEXT_AREA_TOP_PROPERTY, String.valueOf(1));
  }

  public void setWaitTextAreaTop(String row) {
    setProperty(WAIT_TEXT_AREA_TOP_PROPERTY, row);
  }

  private int getWaitTextAreaTopValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_TOP_PROPERTY, 1);
  }

  public String getWaitTextAreaLeft() {
    return getPropertyAsString(WAIT_TEXT_AREA_LEFT_PROPERTY, String.valueOf(1));
  }

  public void setWaitTextAreaLeft(String column) {
    setProperty(WAIT_TEXT_AREA_LEFT_PROPERTY, column);
  }

  private int getWaitTextAreaLeftValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_LEFT_PROPERTY, 1);
  }

  public String getWaitTextAreaBottom() {
    return getPropertyAsString(WAIT_TEXT_AREA_BOTTOM_PROPERTY);
  }

  public void setWaitTextAreaBottom(String row) {
    setProperty(WAIT_TEXT_AREA_BOTTOM_PROPERTY, row);
  }

  private int getWaitTextAreaBottomValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_BOTTOM_PROPERTY, Position.UNSPECIFIED_INDEX);
  }

  public String getWaitTextAreaRight() {
    return getPropertyAsString(WAIT_TEXT_AREA_RIGHT_PROPERTY);
  }

  public void setWaitTextAreaRight(String column) {
    setProperty(WAIT_TEXT_AREA_RIGHT_PROPERTY, column);
  }

  private int getWaitTextAreaRightValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_RIGHT_PROPERTY, Position.UNSPECIFIED_INDEX);
  }

  public String getWaitTextTimeout() {
    return getPropertyAsString(WAIT_TEXT_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS));
  }

  public void setWaitTextTimeout(String timeout) {
    setProperty(WAIT_TEXT_TIMEOUT_PROPERTY, timeout);
  }

  private long getWaitTextTimeoutValue() {
    return getPropertyAsLong(WAIT_TEXT_TIMEOUT_PROPERTY, DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS);
  }

  @Override
  public SampleResult sample(Entry entry) {
    SampleResult sampleResult = new SampleResult();
    sampleResult.setSampleLabel(getName());
    sampleResult.sampleStart();
    sampleResult.setRequestHeaders(buildRequestHeaders());

    try {
      RteProtocolClient client = getClient();
      if (getMode() == Mode.DISCONNECT) {
        if (client != null) {
          disconnect(client);
        }
        sampleResult.setSuccessful(true);
        sampleResult.sampleEnd();
        return sampleResult;
      }

      if (client == null) {
        client = buildClient();
      }
      sampleResult.connectEnd();
      RequestListener requestListener = client.buildRequestListener(sampleResult);
      try {
        addClientRequestHeaders(client, sampleResult);
        if (getMode() == Mode.SEND_INPUT) {
          sampleResult.setSamplerData(buildRequestBody());
          client.send(getCoordInputs(), getAction());
        }
        List<WaitCondition> waiters = getWaitersList();
        if (!waiters.isEmpty()) {
          client.await(waiters);
        }
        sampleResult.setSuccessful(true);
        sampleResult.setResponseHeaders(buildResponseHeaders(client));
        sampleResult.setDataType(SampleResult.TEXT);
        sampleResult.setResponseData(client.getScreen(), "utf-8");
      } finally {
        requestListener.stop();
        getCoordInputs().forEach(i -> {
          LOG.debug("Input(Row,Column,Value): {},{},{} - {}", i.getPosition().getRow(),
              i.getPosition().getColumn(), i.getInput(), getThreadName());
        });
        LOG.debug("Action sent: {} - {}", getAction().name(), getThreadName());
        LOG.debug("Request Headers: {} - {}", sampleResult.getRequestHeaders(), getThreadName());
        LOG.debug("Response Headers: {} - {}", sampleResult.getResponseHeaders(), getThreadName());
        LOG.debug("Response Screen: {} - {}", sampleResult.getResponseDataAsString(),
            getThreadName());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return errorResult("The sampling has been interrupted", e, sampleResult);
    } catch (Exception e) {
      return errorResult("Error while sampling the remote terminal", e, sampleResult);
    }
    return sampleResult;
  }

  private String buildRequestHeaders() {
    return "Server: " + getServer() + "\n" +
        "Port: " + getPort() + "\n" +
        "Protocol: " + getProtocol().toString() + "\n" +
        "Terminal-type: " + getTerminalType() + "\n" +
        "Security: " + getSSLType() + "\n" +
        "Mode: " + getMode() + "\n";
  }

  private RteProtocolClient getClient() {
    String clientId = buildConnectionId();
    Map<String, RteProtocolClient> clients = connections.get();
    return clients.get(clientId);
  }

  private String buildConnectionId() {
    return getServer() + ":" + getPort();
  }

  private RteProtocolClient buildClient()
      throws RteIOException, InterruptedException, TimeoutException {
    RteProtocolClient client = protocolFactory.apply(getProtocol());
    client.connect(getServer(), getPort(), getSSLType(), getTerminalType(), getConnectionTimeout(),
        getStableTimeout());
    connections.get().put(buildConnectionId(), client);
    return client;
  }

  private void addClientRequestHeaders(RteProtocolClient client, SampleResult result) {
    result.setRequestHeaders(
        result.getRequestHeaders() + "Input-inhibited: " + client.isInputInhibited() + "\n");
  }

  private String buildRequestBody() {
    StringBuilder ret = new StringBuilder();
    ret.append("Action: ")
        .append(getAction())
        .append("\n")
        .append("Inputs (Row,Column,Value):\n");

    for (CoordInput c : getCoordInputs()) {
      ret.append(c.getPosition().getRow())
          .append(",")
          .append(c.getPosition().getColumn())
          .append(",")
          .append(c.getInput())
          .append("\n");
    }
    return ret.toString();
  }

  private List<CoordInput> getCoordInputs() {
    List<CoordInput> inputs = new ArrayList<>();
    for (JMeterProperty p : getInputs()) {
      CoordInputRowGUI c = (CoordInputRowGUI) p.getObjectValue();
      inputs.add(c.toCoordInput());
    }
    return inputs;
  }

  public Inputs getInputs() {
    return (Inputs) getProperty(Inputs.INPUTS_PROPERTY).getObjectValue();
  }

  private List<WaitCondition> getWaitersList() {
    List<WaitCondition> waiters = new ArrayList<>();
    if (getWaitSync()) {
      waiters.add(new SyncWaitCondition(getWaitSyncTimeoutValue(), getStableTimeout()));
    }
    if (getWaitCursor()) {
      waiters.add(buildCursorWaitCondition());
    }
    if (getWaitSilent()) {
      waiters.add(new SilentWaitCondition(getWaitSilentTimeoutValue(), getWaitSilentTimeValue()));
    }
    if (getWaitText()) {
      waiters.add(buildTextWaitCondition());
    }
    waiters.sort(Comparator.comparing(WaitCondition::getTimeoutMillis));
    return waiters;
  }

  private CursorWaitCondition buildCursorWaitCondition() {
    return new CursorWaitCondition(
        new Position(getWaitCursorRowValue(), getWaitCursorColumnValue()),
        getWaitCursorTimeoutValue(), getStableTimeout());
  }

  private TextWaitCondition buildTextWaitCondition() {
    return new TextWaitCondition(
        JMeterUtils.getPattern(getWaitTextRegex()),
        JMeterUtils.getMatcher(),
        Area.fromTopLeftBottomRight(getWaitTextAreaTopValue(), getWaitTextAreaLeftValue(),
            getWaitTextAreaBottomValue(), getWaitTextAreaRightValue()),
        getWaitTextTimeoutValue(),
        getStableTimeout());
  }

  private String buildResponseHeaders(RteProtocolClient client) {
    Position cursorPosition = client.getCursorPosition();
    boolean soundAlarm = client.getSoundAlarm();
    return "Input-inhibited: " + client.isInputInhibited() + "\n" +
        "Cursor-position: " + (cursorPosition == null ? ""
        : cursorPosition.getRow() + "," + cursorPosition.getColumn()) +
        (soundAlarm ? "\nSound-Alarm: true" : "");
  }

  private void disconnect(RteProtocolClient client) throws RteIOException {
    connections.get().remove(buildConnectionId());
    client.disconnect();
  }

  private SampleResult errorResult(String message, Throwable e, SampleResult sampleResult) {
    sampleResult.setSuccessful(false);
    sampleResult.setResponseHeaders("");
    sampleResult.setResponseCode(e.getClass().getName());
    sampleResult.setResponseMessage(e.getMessage());
    sampleResult.setDataType(SampleResult.TEXT);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    sampleResult.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    LOG.error(message, e);
    return sampleResult;
  }

  @Override
  public void threadStarted() {
  }

  @Override
  public void threadFinished() {
    closeConnections();
  }

  private void closeConnections() {
    connections.get().values().forEach(c -> {
      try {
        c.disconnect();
      } catch (Exception e) {
        LOG.error("Problem while closing RTE connection", e);
      }
    });
    connections.get().clear();
  }

  @Override
  public void iterationStart(LoopIterationEvent loopIterationEvent) {
    if (!isReuseConnections()) {
      closeConnections();
    }
  }

}

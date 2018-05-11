package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisibleCursorListener extends Tn3270ConditionWaiter<CursorWaitCondition> implements
    CursorMoveListener {

  private static final Logger LOG = LoggerFactory.getLogger(VisibleCursorListener.class);

  private Cursor cursor;
  private Tn3270Client client;

  public VisibleCursorListener(CursorWaitCondition condition, Tn3270Client client,
      ScheduledExecutorService stableTimeoutExecutor, Cursor cursor,
      ExceptionHandler exceptionHandler) {
    super(condition, stableTimeoutExecutor, exceptionHandler);
    this.client = client;
    this.cursor = cursor;
    if (condition.getPosition().equals(client.getCursorPosition())) {
      startStablePeriod();
    }
  }

  @Override
  public void cursorMoved(int i, int i1, Field field) {
    if (condition.getPosition().equals(client.getCursorPosition())) {
      LOG.debug("Cursor is in expected position, now waiting for it to remain for stable period");
      startStablePeriod();
    } else {
      LOG.debug("Cursor is not in expected position, canceling any stable period");
      endStablePeriod();
    }
  }

  @Override
  public void stop() {
    super.stop();
    cursor.removeCursorMoveListener(this);
  }
}

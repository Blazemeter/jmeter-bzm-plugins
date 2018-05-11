package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Tn5250ConditionWaiter} which allows waiting until the cursor shows up on the desired
 * position.
 */
public class VisibleCursorListener extends Tn5250ConditionWaiter<CursorWaitCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(VisibleCursorListener.class);

  public VisibleCursorListener(CursorWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExtendedEmulator em,
      ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, em, exceptionHandler);
    if (condition.getPosition().equals(client.getCursorPosition())) {
      startStablePeriod();
    }
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    if (condition.getPosition().equals(client.getCursorPosition())) {
      LOG.debug("Cursor is in expected position, now waiting for it to remain for stable period");
      startStablePeriod();
    } else {
      LOG.debug("Cursor is not in expected position, canceling any stable period");
      endStablePeriod();
    }
  }

}

package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenTextListener extends Tn5250ConditionWaiter<TextWaitCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(ScreenTextListener.class);

  private boolean matched;

  public ScreenTextListener(TextWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExtendedEmulator em,
      ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, em, exceptionHandler);
    checkIfScreenMatchesCondition();
    if (matched) {
      startStablePeriod();
    }
  }

  @Override
  public void connecting(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  private void handleReceivedEvent(XI5250EmulatorEvent event) {
    if (matched) {
      LOG.debug("Restart silent stable period since received event {}", event);
      startStablePeriod();
    }
  }

  @Override
  public void connected(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent event) {
    checkIfScreenMatchesCondition();
    handleReceivedEvent(event);
  }

  private void checkIfScreenMatchesCondition() {
    if (condition.matchesScreen(client.getScreen(), client.getScreenSize())) {
      LOG.debug("Found matching text in screen, now waiting for silent period.");
      matched = true;
    }
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void dataSended(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

}

package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.bytezone.dm3270.application.KeyboardStatusChangedEvent;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;

public class UnlockListenerIT extends Tn3270ConditionWaiterIT {

  @Override
  @Before
  public void setup() throws Exception {
    when(client.isInputInhibited()).thenReturn(true);
    super.setup();
  }

  @Override
  protected Tn3270ConditionWaiter<?> buildConditionWaiter() {
    return new UnlockListener(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor,
        screen,
        exceptionHandler);
  }

  protected Runnable buildKeyboardStateChangeGenerator(KeyboardStatusChangedEvent keyboardEvent) {
    return () -> ((UnlockListener) listener)
        .keyboardStatusChanged(keyboardEvent);
  }

  protected Runnable buildKeyboardLockingAndUnlockingStateChangeGenerator() {
    return new Runnable() {

      private boolean locked = true;

      @Override
      public void run() {
        ((UnlockListener) listener).keyboardStatusChanged(new KeyboardStatusChangedEvent(false, locked, ""));
        locked = !locked;
      }
    };
  }

  @Test
  public void shouldUnblockAfterReceivingUnlockStateChange() throws Exception {
    KeyboardStatusChangedEvent keyboardEvent = new KeyboardStatusChangedEvent(false, false, "");
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildKeyboardStateChangeGenerator(keyboardEvent));
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test
  public void shouldUnblockWhenAlreadyNotInputInhibited() throws Exception {
    when(client.isInputInhibited()).thenReturn(false);
    Tn3270ConditionWaiter<?> listener = buildConditionWaiter();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNotReceiveUnlockStateChange() throws Exception {
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingUnlockAndLockStateChanges()
      throws Exception {
    startPeriodicEventGenerator(buildKeyboardLockingAndUnlockingStateChangeGenerator());
    listener.await();
  }
}
package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UnlockListenerIT extends Tn5250ConditionWaiterIT {

  @Override
  @Before
  public void setup() throws Exception {
    when(client.isInputInhibited()).thenReturn(true);
    super.setup();
  }

  @Override
  protected Tn5250ConditionWaiter<?> buildConditionWaiter() {
    return new UnlockListener(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor,
        em,
        exceptionHandler);
  }

  @Test
  public void shouldUnblockAfterReceivingUnlockStateChange() throws Exception {
    when(client.isInputInhibited()).thenReturn(false);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test
  public void shouldUnblockWhenAlreadyNotInputInhibited() throws Exception {
    when(client.isInputInhibited()).thenReturn(false);
    Tn5250ConditionWaiter<?> listener = buildConditionWaiter();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNotReceiveUnlockStateChange() throws Exception {
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenKeepReceivingUnlockAndLockStateChanges()
      throws Exception {
    setupEverLockingAndUnlockingEmulator();
    startPeriodicEventGenerator(buildStateChangeGenerator());
    listener.await();
  }

  private void setupEverLockingAndUnlockingEmulator() {
    when(client.isInputInhibited()).thenAnswer(new Answer<Boolean>() {

      private boolean locked = false;

      @Override
      public Boolean answer(InvocationOnMock invocation) {
        locked = !locked;
        return locked;
      }

    });
  }

}

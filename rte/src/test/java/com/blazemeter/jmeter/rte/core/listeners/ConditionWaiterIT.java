package com.blazemeter.jmeter.rte.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.google.common.base.Stopwatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public abstract class ConditionWaiterIT<T extends ConditionWaiter<?>> {

  protected static final long TIMEOUT_MILLIS = 3000;
  protected static final long STABLE_MILLIS = 1000;

  protected ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledExecutorService eventGeneratorExecutor;

  @Mock
  protected ExceptionHandler exceptionHandler;

  protected T listener;

  @Before
  public void setup() throws Exception {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor = Executors.newSingleThreadScheduledExecutor();
    when(exceptionHandler.hasPendingError()).thenReturn(false);
    listener = buildConditionWaiter();
  }

  protected abstract T buildConditionWaiter() throws Exception;

  @Test
  public void shouldUnblockAfterReceivingException() throws Exception {
    when(exceptionHandler.hasPendingError()).thenReturn(true);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildOnExceptionEventGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  protected Runnable buildOnExceptionEventGenerator() {
    return () -> listener
        .onException();
  }

  @After
  public void teardown() {
    eventGeneratorExecutor.shutdownNow();
    stableTimeoutExecutor.shutdownNow();
    listener.stop();
  }

  protected void startSingleEventGenerator(long delayMillis, Runnable eventGenerator) {
    eventGeneratorExecutor.schedule(eventGenerator, delayMillis, TimeUnit.MILLISECONDS);
  }

  protected void startPeriodicEventGenerator(Runnable eventGenerator) {
    eventGeneratorExecutor.scheduleAtFixedRate(eventGenerator, 500, 500, TimeUnit.MILLISECONDS);
  }


}

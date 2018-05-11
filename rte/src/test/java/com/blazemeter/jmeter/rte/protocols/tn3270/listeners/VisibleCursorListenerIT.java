package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.bytezone.dm3270.display.Cursor;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class VisibleCursorListenerIT extends Tn3270ConditionWaiterIT {

  private static final Position EXPECTED_CURSOR_POSITION = new Position(7, 53);

  @Mock
  private Cursor cursor;

  @Before
  @Override
  public void setup() throws Exception {
    when(client.getCursorPosition()).thenReturn(new Position(1, 1));
    super.setup();
  }

  @Override
  protected Tn3270ConditionWaiter<?> buildConditionWaiter() throws Exception {
    return new VisibleCursorListener(
        new CursorWaitCondition(EXPECTED_CURSOR_POSITION, TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor,
        cursor,
        exceptionHandler);
  }

  protected Runnable buildCursorStateChangeGenerator() {
    return () -> ((VisibleCursorListener) listener)
        .cursorMoved(1, 1, null);
  }

  @Test
  public void shouldUnblockAfterReceivingExpectedCursorPosition() throws Exception {
    when(client.getCursorPosition()).thenReturn(EXPECTED_CURSOR_POSITION);
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildCursorStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test
  public void shouldUnblockWhenAlreadyInExpectedCursorPosition() throws Exception {
    when(client.getCursorPosition()).thenReturn(EXPECTED_CURSOR_POSITION);
    Tn3270ConditionWaiter<?> listener = buildConditionWaiter();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedUnexpectedCursorPosition() throws Exception {
    buildCursorStateChangeGenerator().run();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNoVisibleCursorPosition() throws Exception {
    when(client.getCursorPosition()).thenReturn(null);
    buildCursorStateChangeGenerator().run();
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenCursorMovesWhileStablePeriod() throws Exception {
    setupCursorRepositioningEmulator();
    startPeriodicEventGenerator(buildCursorStateChangeGenerator());
    listener.await();
  }

  private void setupCursorRepositioningEmulator() {
    when(client.getCursorPosition()).thenAnswer(new Answer<Position>() {
      private boolean returnDefaultRow = true;

      @Override
      public Position answer(InvocationOnMock invocation) {
        returnDefaultRow = !returnDefaultRow;
        return returnDefaultRow
            ? EXPECTED_CURSOR_POSITION : new Position(EXPECTED_CURSOR_POSITION.getRow() + 1,
            EXPECTED_CURSOR_POSITION.getColumn() + 1);
      }
    });
  }
}

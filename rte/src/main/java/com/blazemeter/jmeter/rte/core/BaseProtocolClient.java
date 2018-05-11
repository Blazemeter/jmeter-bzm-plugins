package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public abstract class BaseProtocolClient implements RteProtocolClient {

  protected ExceptionHandler exceptionHandler;

  @Override
  public void await(List<WaitCondition> waitConditions)
      throws InterruptedException, TimeoutException, RteIOException {
    List<ConditionWaiter> listeners = waitConditions.stream()
        .map(this::buildWaiter)
        .collect(Collectors.toList());
    try {
      for (ConditionWaiter listener : listeners) {
        listener.await();
      }
    } finally {
      listeners.forEach(l -> {
        l.stop();
      });
    }
  }

  protected abstract ConditionWaiter buildWaiter(WaitCondition waitCondition);
}

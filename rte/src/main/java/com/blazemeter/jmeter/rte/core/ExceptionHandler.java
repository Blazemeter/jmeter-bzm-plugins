package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

  private List<ConditionWaiter> listeners = new ArrayList<>();
  private Throwable pendingError;

  public synchronized void setPendingError(Throwable ex) {
    if (pendingError == null) {
      pendingError = ex;
      listeners.forEach(ConditionWaiter::onException);
    } else {
      LOG.error("Exception ignored in step result due to previously thrown exception", ex);
    }
  }

  public synchronized boolean hasPendingError() {
    return pendingError != null;
  }

  public synchronized void throwAnyPendingError() throws RteIOException {
    if (pendingError != null) {
      Throwable ret = pendingError;
      pendingError = null;
      throw new RteIOException(ret);
    }
  }

  public void removeListener(ConditionWaiter listener) {
    listeners.remove(listener);
  }

  public void addListener(ConditionWaiter listener) {
    listeners.add(listener);
  }

}

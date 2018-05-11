package com.blazemeter.jmeter.rte.core.listener;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RequestListener<T extends RteProtocolClient> {

  private static final Logger LOG = LoggerFactory.getLogger(RequestListener.class);

  protected final T client;
  private final SampleResult result;
  private long lastResponseTime;
  private boolean receivedFirstResponse = false;

  public RequestListener(SampleResult result, T client) {
    this.result = result;
    this.client = client;
    lastResponseTime = result.currentTimeInMillis();
  }

  protected void newScreenReceived() {
    if (!receivedFirstResponse) {
      receivedFirstResponse = true;
      result.latencyEnd();
    }
    lastResponseTime = result.currentTimeInMillis();
    if (LOG.isTraceEnabled()) {
      LOG.trace(client.getScreen());
    }
  }

  public void stop() {
    result.setEndTime(lastResponseTime);
  }

}

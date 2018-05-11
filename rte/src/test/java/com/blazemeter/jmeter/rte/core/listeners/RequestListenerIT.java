package com.blazemeter.jmeter.rte.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class RequestListenerIT<T extends RequestListener<?>> {

  private SampleResult result;

  protected T listener;

  @Before
  public void setup() throws Exception {
    result = new SampleResult();
    listener = buildRequestListener(result);
  }

  protected abstract void generateScreenChangeEvent();

  public abstract T buildRequestListener(SampleResult result);

  @Test
  public void shouldReturnGreaterLatencyThanTheElapsedTime() throws Exception {
    Thread.sleep(500);
    generateScreenChangeEvent();
    listener.stop();
    assertThat(result.getLatency()).isGreaterThanOrEqualTo(500);
  }

  @Test
  public void shouldReturnGreaterEndTimeThanTheStartTime() throws Exception {
    long startTime = System.currentTimeMillis();
    /*This loop was included to simulate multiple screens sent by the server.
    The end time must be the time where the last screen came.*/
    for (int i = 0; i < 3; i++) {
      Thread.sleep(100);
      generateScreenChangeEvent();
    }
    listener.stop();
    assertThat(result.getEndTime()).isGreaterThanOrEqualTo(startTime + 300);
  }

}

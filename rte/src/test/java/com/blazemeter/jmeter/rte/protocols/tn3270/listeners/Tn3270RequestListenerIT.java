package com.blazemeter.jmeter.rte.protocols.tn3270.listeners;

import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.listeners.RequestListenerIT;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.bytezone.dm3270.display.FieldManager;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenWatcher;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Tn3270RequestListenerIT extends
    RequestListenerIT<Tn3270RequestListener> {

  @Mock
  private Tn3270Client client;

  @Mock
  private Screen screen;

  @Mock
  private ScreenWatcher screenWatcher;

  @Mock
  private FieldManager fieldManager;

  @Override
  protected void generateScreenChangeEvent() {
    listener.screenChanged(screenWatcher);
  }

  @Override
  public Tn3270RequestListener buildRequestListener(SampleResult result) {
    return new Tn3270RequestListener(result, client, screen);
  }

  @Override
  @Before
  public void setup() throws Exception {
    when(screen.getFieldManager()).thenReturn(fieldManager);
    super.setup();
  }
}

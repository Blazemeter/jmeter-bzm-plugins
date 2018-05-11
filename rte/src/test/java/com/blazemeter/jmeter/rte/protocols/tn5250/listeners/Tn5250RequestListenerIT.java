package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listeners.RequestListenerIT;
import com.blazemeter.jmeter.rte.protocols.tn5250.ExtendedEmulator;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Tn5250RequestListenerIT extends
    RequestListenerIT<Tn5250RequestListener> {

  @Mock
  private Tn5250Client client;

  @Mock
  private ExtendedEmulator emulator;

  @Override
  public Tn5250RequestListener buildRequestListener(SampleResult result) {
    return new Tn5250RequestListener(result, client);
  }

  @Override
  protected void generateScreenChangeEvent() {
    listener.newPanelReceived(
        new XI5250EmulatorEvent(XI5250EmulatorEvent.NEW_PANEL_RECEIVED, emulator));
  }
}

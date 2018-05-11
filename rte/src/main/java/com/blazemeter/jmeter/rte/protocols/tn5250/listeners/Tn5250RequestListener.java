package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.apache.jmeter.samplers.SampleResult;

public class Tn5250RequestListener extends RequestListener<Tn5250Client> implements
    XI5250EmulatorListener {

  public Tn5250RequestListener(SampleResult result, Tn5250Client client) {
    super(result, client);
  }

  @Override
  public void connecting(XI5250EmulatorEvent e) {
  }

  @Override
  public void connected(XI5250EmulatorEvent e) {
  }

  @Override
  public void disconnected(XI5250EmulatorEvent e) {
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent e) {
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent e) {
    newScreenReceived();
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent e) {
  }

  @Override
  public void dataSended(XI5250EmulatorEvent e) {
  }

  @Override
  public void stop() {
    super.stop();
    client.removeListener(this);
  }
}

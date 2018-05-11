package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270Client;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.function.Supplier;

public enum Protocol {
  TN5250(Tn5250Client::new),
  TN3270(Tn3270Client::new);

  private final Supplier<RteProtocolClient> factory;

  Protocol(Supplier<RteProtocolClient> s) {
    this.factory = s;
  }

  public RteProtocolClient createProtocolClient() {
    return factory.get();
  }
}


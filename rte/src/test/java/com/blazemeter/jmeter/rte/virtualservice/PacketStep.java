package com.blazemeter.jmeter.rte.virtualservice;

import java.io.IOException;

/**
 * A step in a flow to be executed for a given packet.
 */
public abstract class PacketStep {

  protected PacketData data;

  protected PacketStep() {}

  protected PacketStep(String data) {
    this.data = PacketData.fromHexDump(data);
  }

  public String getData() {
    return data.toString();
  }

  public void setData(String data) {
    this.data = PacketData.fromHexDump(data);
  }

  public abstract void process(ClientConnection clientConnection)
      throws IOException, InterruptedException;

}

package com.blazemeter.jmeter.rte.virtualservice;

import java.io.IOException;

/**
 * Exception thrown when a client has unexpectedly closed connection with the server.
 */
public class ConnectionClosedException extends IOException {

  public ConnectionClosedException() {
    super("Connection closed by remote end while waiting for packet");
  }

}

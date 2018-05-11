package com.blazemeter.jmeter.rte.virtualservice;

import com.blazemeter.jmeter.rte.core.ssl.SSLSocketFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows to create a virtual service (a mock) from an actual service traffic dump.
 *
 * This is useful for testing clients and interactions which depend on a not always available
 * environment, either due to cost, resiliency, or other potential concerns.
 *
 * This class was required for tn5250 and tn3270 since <a href="http://www.mbtest.org/">mountebank</a>,
 * a potential open source alternative for implementing virtual tcp services, has no support for
 * sending initial packets from server (all interactions must be request based for mountebank). We
 * also discarded <a href="https://github.com/CloudRacer/MockTCPServer">MockTCPServer</a> which
 * required manual implementation of the protocol.
 *
 * TODO: move this to an independent project.
 */
public class VirtualTcpService implements Runnable {

  public static final int DEFAULT_READ_BUFFER_SIZE = 2048;
  public static final int DEFAULT_MAX_CONNECTION_COUNT = 1;

  private static final int DYNAMIC_PORT = 0;
  private static final Logger LOG = LoggerFactory.getLogger(VirtualTcpService.class);

  private final ServerSocket server;
  private final int readBufferSize;
  private Flow flow;
  private ExecutorService serverExecutorService = Executors.newSingleThreadExecutor();
  private boolean stopped = false;
  private ArrayList<ClientConnection> clientConnections = new ArrayList<>();
  private ExecutorService clientExecutorService;

  public VirtualTcpService(int port, SSLType sslType, int readBufferSize, int maxConnections)
      throws IOException, GeneralSecurityException {
    clientExecutorService = Executors.newFixedThreadPool(maxConnections);
    if (sslType != null && sslType != SSLType.NONE) {
      SSLSocketFactory socketFactory = new SSLSocketFactory(sslType);
      socketFactory.init();
      server = socketFactory.createServerSocket(port);
    } else {
      server = new ServerSocket(port);
    }
    this.readBufferSize = readBufferSize;
  }

  public VirtualTcpService(SSLType sslType) throws IOException, GeneralSecurityException {
    this(DYNAMIC_PORT, sslType, DEFAULT_READ_BUFFER_SIZE, DEFAULT_MAX_CONNECTION_COUNT);
  }

  public VirtualTcpService(int port) throws IOException, GeneralSecurityException {
    this(port, null, DEFAULT_READ_BUFFER_SIZE, DEFAULT_MAX_CONNECTION_COUNT);
  }

  public VirtualTcpService() throws IOException, GeneralSecurityException {
    this(null);
  }

  public int getPort() {
    return server.getLocalPort();
  }

  public void setFlow(Flow flow) {
    this.flow = flow;
    Optional<PacketStep> bigPacketStep = flow.getSteps().stream()
        .filter(s -> s instanceof ClientPacket && s.data.getBytes().length > readBufferSize)
        .findAny();
    if (bigPacketStep.isPresent()) {
      throw new IllegalArgumentException(String.format(
          "Read buffer size of %d bytes is not enough for receiving expected packet from client "
              + "with %s", readBufferSize, bigPacketStep.get().data));
    }
  }

  public void start() {
    serverExecutorService.submit(this);
  }

  @Override
  public void run() {
    LOG.debug("Starting server on {} with flow: {}", server.getLocalPort(), flow);
    LOG.info("Waiting for connections on {}", server.getLocalPort());
    while (!stopped) {
      try {
        addClient(new ClientConnection(this, server.accept(), readBufferSize, flow));
      } catch (IOException e) {
        if (stopped) {
          LOG.trace("Received expected exception when server socket has been closed", e);
        } else {
          LOG.error("Problem waiting for client connection. Keep waiting.", e);
        }
      }
    }
  }

  private synchronized void addClient(ClientConnection clientConnection) throws IOException {
    if (stopped) {
      clientConnection.close();
      return;
    }
    clientConnections.add(clientConnection);
    clientExecutorService.submit(clientConnection);
  }

  public synchronized void removeClient(ClientConnection clientConnection) {
    clientConnections.remove(clientConnection);
  }

  public void stop(long timeoutMillis) throws IOException, InterruptedException {
    synchronized (this) {
      stopped = true;
      server.close();
      clientConnections.forEach(c -> {
        try {
          c.close();
        } catch (IOException e) {
          LOG.error("Problem closing connection {}", c.getId(), e);
        }
      });
    }
    clientExecutorService.shutdown();
    clientExecutorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS);
    serverExecutorService.shutdownNow();
    if (!serverExecutorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
      LOG.warn("Server thread didn't stop after {} millis", timeoutMillis);
    }
  }

}

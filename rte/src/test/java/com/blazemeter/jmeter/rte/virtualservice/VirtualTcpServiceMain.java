package com.blazemeter.jmeter.rte.virtualservice;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

/**
 * This is a utility tool we can use to create configuration files from Wireshark JSON dump files
 * for {@link VirtualTcpService} and to manually test configuration files.
 *
 * @see VirtualTcpService
 */
public class VirtualTcpServiceMain {

  @Option(name = "-p", aliases = "--port", metaVar = "port",
      usage = "Port to receive connections to the virtual service")
  private int port;

  @Option(name = "-b", aliases = "--read-buffer-size-bytes", metaVar = "bytes count", usage =
      "Size (in bytes) of buffer used to receive packets from client. Default value: "
          + VirtualTcpService.DEFAULT_READ_BUFFER_SIZE)
  private int readBufferSize = VirtualTcpService.DEFAULT_READ_BUFFER_SIZE;

  @Option(name = "-c", aliases = "--max-concurrent-connections", metaVar = "connection count",
      usage = "Maximum number of concurrent client connections to attend. Default value: "
          + VirtualTcpService.DEFAULT_MAX_CONNECTION_COUNT)
  private int maxConnectionCount = VirtualTcpService.DEFAULT_MAX_CONNECTION_COUNT;

  @Option(name = "-s", aliases = "--ssl-protocol",
      metaVar = "ssl (SSLv2, SSLv3 or TLS)",
      usage = "Specifies if the server should start with ssl protocol support. When this "
          + "option is provided javax.net.ssl.keyStore and javax.net.ssl.keyStorePassword JVM "
          + "system properties must be specified.")
  private SSLType sslType;

  @Option(name = "-w", aliases = "--wireshark-server-address", metaVar = "ip address",
      usage = "When specified, the flow config file is interpreted as a Wireshark generated JSON "
          + "dump file and this IP address identifies the service to be virtualized")
  private String wiresharkServerAddress;

  @Option(name = "-d", aliases = "--dump-file", metaVar = ".yml file",
      usage = "File path to dump loaded flow config. The virtual service will not be started when "
          + "this option is specified. This option makes sense when a Wireshark JSON file is used "
          + "for config to dump a simplified and smaller file and then manually tune it if needed")
  private File dumpFile;

  @Option(name = "-v", aliases = "--verbose", usage = "Logs debug messages")
  private boolean verbose;

  @Option(name = "-h", aliases = "--help", usage = "Show usage information", help = true)
  private boolean displayHelp;

  @Argument(metaVar = "config file", required = true,
      usage = "Configuration file from where to read packets information")
  private File configFile;

  private boolean isDisplayHelp() {
    return displayHelp;
  }

  public static void main(String[] args) throws IOException, GeneralSecurityException {
    VirtualTcpServiceMain main = new VirtualTcpServiceMain();
    CmdLineParser parser = new CmdLineParser(main);
    try {
      parser.parseArgument(args);
      if (main.isDisplayHelp()) {
        printHelp(parser, System.out);
      } else {
        main.run();
      }
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      printHelp(parser, System.err);
    }
  }

  private static void printHelp(CmdLineParser parser, PrintStream printStream) {
    String command = "java " + VirtualTcpService.class.getCanonicalName();
    printStream.println(command + " [options...] <config file>");
    parser.printUsage(printStream);
    printStream.println();
    printStream.println("  Examples: \n"
        + command + " -p 2324 login-invalid-creds.yml\n"
        + command + " -p 2324 -w 0.0.0.0 login-invalid-creds-wireshark.json\n"
        + command + " -d login-invalid-creds.yml -w 0.0.0.0 login-invalid-creds-wireshark.json\n");
  }

  private void run() throws IOException, GeneralSecurityException {
    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(verbose ? Level.DEBUG : Level.INFO);

    Flow flow = wiresharkServerAddress != null
        ? Flow.fromWiresharkJsonDump(configFile, wiresharkServerAddress)
        : Flow.fromYml(configFile);
    if (dumpFile != null) {
      flow.saveYml(dumpFile);
    } else {
      VirtualTcpService service = new VirtualTcpService(port, sslType, readBufferSize,
          maxConnectionCount);
      service.setFlow(flow);
      service.run();
    }
  }

}

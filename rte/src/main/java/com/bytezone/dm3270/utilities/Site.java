package com.bytezone.dm3270.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style. Additionally, we replace existing standard output
 * with proper logging.
 */
public class Site {

  private static final Logger LOG = LoggerFactory.getLogger(Site.class);

  public final String name;
  public final String url;
  public int port;
  public final boolean extended;
  public int model;

  public Site(String name, String url, int port, boolean extended, int model) {
    this.name = name;
    this.url = url;
    this.port = port;
    this.extended = extended;
    this.model = model;
  }

  public String getName() {
    return name;
  }

  public String getURL() {
    return url;
  }

  public int getPort() {
    if (port <= 0) {
      LOG.warn("Invalid port value: {}. Fallback to default value {}", port, 23);
      port = 23;
    }
    return port;
  }

  public boolean getExtended() {
    return extended;
  }

  public int getModel() {
    if (model < 2 || model > 5) {
      LOG.warn("Invalid model value: {}. Fallback to default value {}", model, 2);
      model = 2;
    }
    return model;
  }

  @Override
  public String toString() {
    return String.format("Site [name=%s, url=%s, port=%d]", getName(), getURL(), getPort());
  }

}

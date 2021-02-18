package org.calypsonet.certification.calypso;

import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigProperties {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private static final Properties properties = new Properties();

  // Properties keys values
  public static final String PLUGIN_NAME_STUB = "stub";
  public static final String PLUGIN_NAME_PCSC = "pcsc";

  private ConfigProperties() {}

  /** Properties keys. */
  public enum Key {
    PLUGIN_NAME("plugin.name"),
    ;

    private final String keyName;

    Key(String keyName) {
      this.keyName = keyName;
    }

    public String getKeyName() {
      return keyName;
    }
  }

  /**
   * Loads properties from external config file.
   *
   * @throws IOException
   */
  public static void loadProperties() throws IOException {
    logger.info("Load file 'config.properties'");
    InputStream inputStream = new FileInputStream("config.properties");
    properties.load(inputStream);
    inputStream.close();
  }

  /**
   * Gets the value of the provided key name.
   *
   * @param key
   * @return a nullable string.
   */
  public static String getValue(Key key) {
    return properties.getProperty(key.getKeyName());
  }
}

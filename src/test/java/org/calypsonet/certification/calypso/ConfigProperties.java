package org.calypsonet.certification.calypso;

import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigProperties {

  private static final Logger logger = LoggerFactory.getLogger(ConfigProperties.class);
  private static final Properties properties = new Properties();

  // Properties keys values
  public static final String PLUGIN_NAME_STUB = "stub";
  public static final String PLUGIN_NAME_PCSC = "pcsc";
  public static final String READER_TYPE_CONTACT = "contact";
  public static final String READER_TYPE_CONTACTLESS = "contactless";

  private ConfigProperties() {}

  /** Properties keys. */
  public enum Key {
    PLUGIN_NAME("plugin.name"),
    READER_1_NAME("reader.1.name"),
    READER_1_TYPE("reader.1.type"),
    READER_2_NAME("reader.2.name"),
    READER_2_TYPE("reader.2.type"),
    SAM_1_REVISION("sam.1.revision"),
    SAM_2_REVISION("sam.2.revision"),
    CARD_1_PROTOCOL("card.1.protocol"),
    CARD_1_AID("card.1.aid"),
    CARD_1_DFNAME("card.1.dfname"),
    CARD_2_PROTOCOL("card.2.protocol"),
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
    String value = properties.getProperty(key.getKeyName());
    if (value == null) {
      logger.error("Key '{}' not found!", key.getKeyName());
    }
    return value;
  }
}

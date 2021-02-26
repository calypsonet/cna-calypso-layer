package org.calypsonet.certification.calypso;

import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Config properties extracted from the 'config.properties' file. */
public final class ConfigProperties {

  private static final Logger logger = LoggerFactory.getLogger(ConfigProperties.class);
  private static final Properties properties = new Properties();

  static {
    logger.info("Load file 'config.properties'");
    try {
      InputStream inputStream;
      try {
        inputStream = new FileInputStream("config.properties");

      } catch (FileNotFoundException e) {
        inputStream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
      }
      try {
        properties.load(inputStream);
      } finally {
        inputStream.close();
      }
    } catch (Exception e) {
      logger.error("Error: {}", e.getMessage(), e);
    }
  }

  // Properties keys values
  public static final String PLUGIN_NAME_STUB = "stub";
  public static final String PLUGIN_NAME_PCSC = "pcsc";

  public static final String READER_TYPE_CONTACT = "contact";
  public static final String READER_TYPE_CONTACTLESS = "contactless";

  private ConfigProperties() {}

  /** Properties keys. */
  public enum Key {
    PLUGIN_NAME("plugin.name"),
    PO_READER_1_NAME("reader.po.1.name"),
    PO_READER_1_TYPE("reader.po.1.type"),
    SAM_READER_1_NAME("reader.sam.1.name"),
    SAM_READER_1_TYPE("reader.sam.1.type"),
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

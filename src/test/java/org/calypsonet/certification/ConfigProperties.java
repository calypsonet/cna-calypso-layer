package org.calypsonet.certification;

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
    CARD_READER_1_NAME("reader.card.1.name"),
    CARD_READER_1_TYPE("reader.card.1.type"),
    CARD_READER_2_NAME("reader.card.2.name"),
    CARD_READER_2_TYPE("reader.card.2.type"),
    SAM_READER_1_NAME("reader.sam.1.name"),
    CARD_1_PROTOCOL("card.1.protocol"),
    CARD_1_DFNAME("card.1.dfname"),
    CARD_1_SERIALNUMBER("card.1.serialnumber"),
    CARD_1_STARTUPINFO("card.1.startupinfo"),
    CARD_2_PROTOCOL("card.2.protocol"),
    CARD_2_DFNAME("card.2.dfname");

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
   * @param key key name
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

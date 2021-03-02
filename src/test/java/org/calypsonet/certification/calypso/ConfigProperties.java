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
    PO_READER_2_NAME("reader.po.2.name"),
    PO_READER_2_TYPE("reader.po.2.type"),
    SAM_READER_1_NAME("reader.sam.1.name"),
    SAM_1_REVISION("sam.1.revision"),
    PO_1_PROTOCOL("po.1.protocol"),
    PO_1_DFNAME("po.1.dfname"),
    PO_1_SERIALNUMBER("po.1.serialnumber"),
    PO_1_STARTUPINFO("po.1.startupinfo"),
    PO_2_PROTOCOL("po.2.protocol"),
    PO_2_DFNAME("po.2.dfname");

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

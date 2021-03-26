package org.calypsonet.certification.calypso;

import org.calypsonet.certification.procedures.Procedure;
import org.calypsonet.certification.procedures.ProcedureFactory;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class RL22Test {

  private static final Logger logger = LoggerFactory.getLogger(RL22Test.class);
  private static Procedure procedure;
  private static String pluginName;
  private static String poReaderName;
  private static String poReaderType;
  private static boolean isPoReaderContactless;
  private static String poProtocol;
  private static String poDfName;

  /**
   * Executed once before running all tests of this class.
   *
   * @throws Exception
   */
  @BeforeClass
  public static void beforeClass() throws Exception {

    // Get procedure adapter
    procedure = ProcedureFactory.getProcedure();

    // Configuration parameters
    pluginName = ConfigProperties.getValue(ConfigProperties.Key.PLUGIN_NAME);
    poReaderName = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_1_NAME);
    poReaderType = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_1_TYPE);
    poProtocol = ConfigProperties.getValue(ConfigProperties.Key.PO_1_PROTOCOL);
    poDfName = ConfigProperties.getValue(ConfigProperties.Key.PO_1_DFNAME);

    if (ConfigProperties.READER_TYPE_CONTACTLESS.equalsIgnoreCase(poReaderType)) {
      isPoReaderContactless = true;
    } else if (ConfigProperties.READER_TYPE_CONTACT.equalsIgnoreCase(poReaderType)) {
      isPoReaderContactless = false;
    } else {
      throw new IllegalStateException("Unknown reader type " + poReaderType);
    }
  }

  /**
   * Executed once before each individual test of this class.
   *
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {

    logger.info("PRE CONDITIONS");
    logger.info("Initialize smart card service / register the Plugin");

    procedure.initializeContext(pluginName);

    // Prepare PO reader
    procedure.setupPoReader(poReaderName, isPoReaderContactless, poProtocol);
  }

  /**
   * Executed once after each individual test of this class.
   *
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    procedure.resetContext();
  }

  /**
   * Executed once after running all tests of this class.
   *
   * @throws Exception
   */
  @AfterClass
  public static void afterClass() throws Exception {}

  @Test
  public void RL_22() throws Exception {
    // Display test infos
    logger.info(
        "Ensure that the Reader Layer do not use the ISO/IEC 14443-4 PICC presence check method.");

    procedure.activateSingleObservation(poDfName);

    logger.info("Present smartcard");

    procedure.waitForCardInsertion();

    logger.info("Wait 1 second");
    procedure.waitMilliSeconds(1000);

    procedure.sendAPDU("00 B2 01 3C 1D", true);
    assertThat(procedure.isApduSuccessful()).isTrue();

    logger.info("Wait 1 second");
    procedure.waitMilliSeconds(1000);

    logger.info("Remove smartcard");
    procedure.waitForCardRemoval();
  }
}

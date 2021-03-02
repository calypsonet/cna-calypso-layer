package org.calypsonet.certification.calypso;

import org.calypsonet.certification.procedures.Procedure;
import org.calypsonet.certification.procedures.ProcedureFactory;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class RL31Test {

  private static final Logger logger = LoggerFactory.getLogger(RL31Test.class);
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
    poReaderName = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_2_NAME);
    poReaderType = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_2_TYPE);
    poProtocol = ConfigProperties.getValue(ConfigProperties.Key.PO_2_PROTOCOL);
    poDfName = ConfigProperties.getValue(ConfigProperties.Key.PO_2_DFNAME);

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
  public static void afterClass() throws Exception {

  }

  @Test
  public void RL_31() {
    // Display test infos
    logger.info(
        "Ensure that the Reader Layer manage correctly the SW1SW2=6CXXh status word when it receive a correct SW1SW2=6CXXh status word from a smartcard.");

    // Console.display("Send SAM Read parameters APDU command Case 2 to the SAM reader");
    //      Console.display("CLA: 80");
    //      Console.display("INS: BE");
    //      Console.display("P1: 00");
    //      Console.display("P2: A0");
    //      Console.display("Le: 00");
    procedure.sendAPDU("80 BE 00 A0 00", true);

    assertThat(procedure.isApduSuccessful()).isTrue();
  }
}

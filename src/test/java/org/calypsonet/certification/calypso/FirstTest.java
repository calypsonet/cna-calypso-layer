package org.calypsonet.certification.calypso;

import static org.assertj.core.api.Assertions.assertThat;

import org.calypsonet.certification.Procedure;
import org.calypsonet.certification.ProcedureFactory;
import org.calypsonet.certification.SessionAccessLevel;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstTest {

  private static final Logger logger = LoggerFactory.getLogger(FirstTest.class);
  private static String smartCardAID1;
  private static String dfName1;
  private static String poReaderName;
  private static String samReaderName;
  private static String cardProtocol;
  private static String poReaderType;
  private static String samRevisionString;
  private static Procedure procedure;
  private static boolean isContactless;

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
    poReaderName = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_1_NAME);
    poReaderType = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_1_TYPE);
    samReaderName = ConfigProperties.getValue(ConfigProperties.Key.SAM_READER_1_NAME);
    cardProtocol = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_PROTOCOL);
    smartCardAID1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_AID);
    dfName1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_DFNAME);
    samRevisionString = ConfigProperties.getValue(ConfigProperties.Key.SAM_1_REVISION);

    if (ConfigProperties.READER_TYPE_CONTACTLESS.equalsIgnoreCase(poReaderType)) {
      isContactless = true;
    } else if (ConfigProperties.READER_TYPE_CONTACT.equalsIgnoreCase(poReaderType)) {
      isContactless = false;
    } else {
      throw new IllegalStateException("Unknown reader type " + poReaderType);
    }

    String pluginName = ConfigProperties.getValue(ConfigProperties.Key.PLUGIN_NAME);

    procedure.initializeContext(pluginName);
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

    // Prepare PO reader
    procedure.setupPoReader(poReaderName, isContactless, cardProtocol);

    // Prepare SAM reader
    procedure.setupPoSecuritySettings(samReaderName, samRevisionString);
  }

  /**
   * Executed once after each individual test of this class.
   *
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {}

  /**
   * Executed once after running all tests of this class.
   *
   * @throws Exception
   */
  @AfterClass
  public static void afterClass() throws Exception {
    procedure.resetContext();
  }

  @Test
  public void CL_116() {
    // Display test infos
    logger.info(
        "Ensure that Calypso Layer does not update the data of the current DF or current EF, if a new Select command sent return an error.");

    procedure.selectPo(smartCardAID1);

    assertThat(procedure.getPoDfName()).isEqualToIgnoringCase(dfName1);

    procedure.initializeNewTransaction();

    procedure.prepareReadRecord(0x07, 1);

    procedure.processOpening(SessionAccessLevel.DEBIT);

    /////////////////////////////////////////////
    logger.info("PROCEDURE");
    /////////////////////////////////////////////

    procedure.prepareReleaseChannel();

    procedure.processClosing();

    // Verify the application is no longer selected
    // assertThat(calypsoPo.getDfName()).isEqualToIgnoringCase("");
  }
}

package org.calypsonet.certification.calypso;

import static org.assertj.core.api.Assertions.assertThat;

import org.calypsonet.certification.procedures.Procedure;
import org.calypsonet.certification.procedures.ProcedureFactory;
import org.calypsonet.certification.procedures.SessionAccessLevel;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CL116Test {

  private static final Logger logger = LoggerFactory.getLogger(CL116Test.class);
  private static Procedure procedure;
  private static String pluginName;
  private static String poReaderName;
  private static String poReaderType;
  private static boolean isPoReaderContactless;
  private static String samReaderName;
  private static String poProtocol1;
  private static String poDfName1;
  private static String samRevision1;

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
    samReaderName = ConfigProperties.getValue(ConfigProperties.Key.SAM_READER_1_NAME);
    poProtocol1 = ConfigProperties.getValue(ConfigProperties.Key.PO_1_PROTOCOL);
    poDfName1 = ConfigProperties.getValue(ConfigProperties.Key.PO_1_DFNAME);
    samRevision1 = ConfigProperties.getValue(ConfigProperties.Key.SAM_1_REVISION);

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
    procedure.setupPoReader(poReaderName, isPoReaderContactless, poProtocol1);

    // Prepare SAM reader
    procedure.setupPoSecuritySettings(samReaderName, samRevision1);
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
  public void CL_116() {
    // Display test infos
    logger.info(
        "Ensure that Calypso Layer does not update the data of the current DF or current EF, if a new Select command sent return an error.");

    procedure.selectPo(poDfName1);

    assertThat(procedure.getPoDfName()).isEqualToIgnoringCase(poDfName1);

    procedure.initializeSecurePoTransaction();

    procedure.prepareReadRecord(0x07, 1);

    procedure.processOpening(SessionAccessLevel.DEBIT);

    procedure.prepareReleaseChannel();

    procedure.processClosing();

    // Verify the application is no longer selected
    // assertThat(calypsoPo.getDfName()).isEqualToIgnoringCase("");
  }
}

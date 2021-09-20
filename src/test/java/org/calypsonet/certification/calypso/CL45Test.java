package org.calypsonet.certification.calypso;

import org.calypsonet.certification.ConfigProperties;
import org.calypsonet.certification.procedures.*;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class CL45Test {

  private static final Logger logger = LoggerFactory.getLogger(CL45Test.class);
  private static ReaderProcedure readerProcedure;
  private static CalypsoProcedure calypsoProcedure;
  private static String cardReaderName;
  private static String cardReaderType;
  private static boolean isCardReaderContactless;
  private static String samReaderName;
  private static String cardProtocol1;
  private static String cardDfName1;

  /**
   * Executed once before running all tests of this class.
   *
   * @throws Exception
   */
  @BeforeClass
  public static void beforeClass() throws Exception {

    ParameterDto parameterDto =  new ParameterDto();
    // Get procedure adapter
    readerProcedure = new ReaderProcedureAdapter(parameterDto);
    calypsoProcedure = new CalypsoProcedureAdapter(parameterDto);


    // Configuration parameters
    cardReaderName = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_1_NAME);
    cardReaderType = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_1_TYPE);
    samReaderName = ConfigProperties.getValue(ConfigProperties.Key.SAM_READER_1_NAME);
    cardProtocol1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_PROTOCOL);
    cardDfName1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_DFNAME);

    if (ConfigProperties.READER_TYPE_CONTACTLESS.equalsIgnoreCase(cardReaderType)) {
      isCardReaderContactless = true;
    } else if (ConfigProperties.READER_TYPE_CONTACT.equalsIgnoreCase(cardReaderType)) {
      isCardReaderContactless = false;
    } else {
      throw new IllegalStateException("Unknown reader type " + cardReaderType);
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

    readerProcedure.RL_UR_InitializeContext();
    calypsoProcedure.CL_UT_InitializeContext();

    // Prepare card reader
    readerProcedure.RL_UR_SetupCardReader(cardReaderName, isCardReaderContactless, cardProtocol1);

    // Prepare SAM reader
    readerProcedure.RL_UR_SetupSamReader(samReaderName);

    readerProcedure.RL_UR_IsSamPresent();

    // Prepare Security Setting
    calypsoProcedure.CL_UT_SetupCardSecuritySetting();

    readerProcedure.RL_UR_IsCardPresent();

    calypsoProcedure.CL_UT_PrepareCardSelection(cardDfName1);
  }

  /**
   * Executed once after each individual test of this class.
   *
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    calypsoProcedure.CL_UT_ResetContext();
    readerProcedure.RL_UR_ResetContext();
  }

  /**
   * Executed once after running all tests of this class.
   *
   * @throws Exception
   */
  @AfterClass
  public static void afterClass() throws Exception {}

  @Test
  public void CL_45() {
    // Display test infos
    logger.info(
        "Validate that the Revision 3 is selected as operating mode by the Calypso Layer for session using a PO Prime Revision 3 without Revision 3.2 \n" +
                "\tmode and a SAM-C1");

    calypsoProcedure.CL_UT_SelectCard();

    assertThat(calypsoProcedure.CL_UT_GetCardDfName()).isEqualToIgnoringCase(cardDfName1);
    assertThat(calypsoProcedure.CL_UT_IsExtendedModeSupported()).isFalse();
    assertThat(calypsoProcedure.CL_UT_IsPkiModeSupported()).isFalse();

    calypsoProcedure.CL_UT_InitializeCardTransactionManager();

    calypsoProcedure.CL_UT_ProcessOpening(SessionAccessLevel.DEBIT);

    calypsoProcedure.CL_UT_PrepareReleaseCardChannel();

    calypsoProcedure.CL_UT_ProcessClosing();
  }
}

package org.calypsonet.certification.calypso;

import static org.assertj.core.api.Assertions.assertThat;

import org.calypsonet.certification.ConfigProperties;
import org.calypsonet.certification.procedures.*;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CL121Test {

  private static final Logger logger = LoggerFactory.getLogger(CL121Test.class);
  private static CalypsoReaderProcedure readerProcedure;
  private static CalypsoProcedure calypsoProcedure;
  private static String cardReaderName;
  private static String cardReaderType;
  private static boolean isCardReaderContactless;
  private static String cardProtocol1;
  private static String cardDfName1;
  private static String cardSerialNumber1;
  private static String cardStartupInfo1;

  /**
   * Executed once before running all tests of this class.
   *
   * @throws Exception
   */
  @BeforeClass
  public static void beforeClass() throws Exception {

    CommonDto commonDto =  new CommonDto();
    // Get procedure adapter
    readerProcedure = new CalypsoReaderProcedureAdapter(commonDto);
    calypsoProcedure = new CalypsoProcedureAdapter(commonDto);

    // Configuration parameters
    cardReaderName = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_1_NAME);
    cardReaderType = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_1_TYPE);
    cardProtocol1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_PROTOCOL);
    cardDfName1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_DFNAME);
    cardSerialNumber1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_SERIALNUMBER);
    cardStartupInfo1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_STARTUPINFO);

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

    // Prepare PO reader
    readerProcedure.RL_UR_SetupCardReader(cardReaderName, isCardReaderContactless, cardProtocol1);

    readerProcedure.RL_UR_IsCardPresent();
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
  public void CL_121() {
    // Display test infos
    logger.info("Ensure that the Calypso Layer allow additionnal TLV data.");

    // Data_80: {0x80, 0x01, 0x53}
    // Data_A0:{0xA0, 0x08, 0x8C, 0x02, 0x55, 0x55, 0x53, 0x02, 0xAA, 0xAA}
    // FCI: {0x6F, 0xXX, 0x84, poDfName1_Length, poDfName1, 0xA5, 0x16, 0xBF, 0x0C,  0x13, 0xC7,
    //  0x08, poSerialNumber1, 0x53, 0x07, poStartupInfo1}

    // Selection application response with additional data
    // Data emulated : FCI + Data_80
    calypsoProcedure.CL_UT_CreateCardSelection(cardDfName1);
    readerProcedure.RL_UR_SelectCard();

    calypsoProcedure.CL_UT_SetCard();
    assertThat(calypsoProcedure.CL_UT_GetCardDfName()).isEqualToIgnoringCase(cardDfName1);
    assertThat(calypsoProcedure.CL_UT_GetCardApplicationSerialNumber()).isEqualToIgnoringCase(cardSerialNumber1);
    assertThat(calypsoProcedure.CL_UT_GetCardStartupInfo()).isEqualToIgnoringCase(cardStartupInfo1);

    // Selection application response with additional data
    // Data emulated : Data_80 + FCI
    calypsoProcedure.CL_UT_CreateCardSelection(cardDfName1);
    readerProcedure.RL_UR_SelectCard();

    calypsoProcedure.CL_UT_SetCard();
    assertThat(calypsoProcedure.CL_UT_GetCardDfName()).isEqualToIgnoringCase(cardDfName1);
    assertThat(calypsoProcedure.CL_UT_GetCardApplicationSerialNumber()).isEqualToIgnoringCase(cardSerialNumber1);
    assertThat(calypsoProcedure.CL_UT_GetCardStartupInfo()).isEqualToIgnoringCase(cardStartupInfo1);

    // Selection application response with additional data
    // Data emulated : Data_A0 + FCI
    calypsoProcedure.CL_UT_CreateCardSelection(cardDfName1);
    readerProcedure.RL_UR_SelectCard();

    calypsoProcedure.CL_UT_SetCard();
    assertThat(calypsoProcedure.CL_UT_GetCardDfName()).isEqualToIgnoringCase(cardDfName1);
    assertThat(calypsoProcedure.CL_UT_GetCardApplicationSerialNumber()).isEqualToIgnoringCase(cardSerialNumber1);
    assertThat(calypsoProcedure.CL_UT_GetCardStartupInfo()).isEqualToIgnoringCase(cardStartupInfo1);

    // Selection application response with additional data
    // Data emulated : Data_80 + FCI + Data_A0
    calypsoProcedure.CL_UT_CreateCardSelection(cardDfName1);
    readerProcedure.RL_UR_SelectCard();

    calypsoProcedure.CL_UT_SetCard();
    assertThat(calypsoProcedure.CL_UT_GetCardDfName()).isEqualToIgnoringCase(cardDfName1);
    assertThat(calypsoProcedure.CL_UT_GetCardApplicationSerialNumber()).isEqualToIgnoringCase(cardSerialNumber1);
    assertThat(calypsoProcedure.CL_UT_GetCardStartupInfo()).isEqualToIgnoringCase(cardStartupInfo1);
  }
}

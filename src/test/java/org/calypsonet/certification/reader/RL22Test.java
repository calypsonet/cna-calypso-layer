package org.calypsonet.certification.reader;

import org.calypsonet.certification.ConfigProperties;
import org.calypsonet.certification.procedures.*;
import org.calypsonet.certification.procedures.CardProcedure;
import org.calypsonet.certification.procedures.CardProcedureAdapter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class RL22Test {

  private static final Logger logger = LoggerFactory.getLogger(RL22Test.class);
  private static ReaderProcedure readerProcedure;
  private static CardProcedure cardProcedure;
  private static String cardReaderName;
  private static String cardReaderType;
  private static boolean isCardReaderContactless;
  private static String cardProtocol;
  private static String cardDfName;

  /**
   * Executed once before running all tests of this class.
   *
   * @throws Exception
   */
  @BeforeClass
  public static void beforeClass() throws Exception {

    CommonDto commonDto =  new CommonDto();
    // Get procedure adapter
    readerProcedure = new ReaderProcedureAdapter(commonDto);
    cardProcedure = new CardProcedureAdapter(commonDto);

    // Configuration parameters
    cardReaderName = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_1_NAME);
    cardReaderType = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_1_TYPE);
    cardProtocol = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_PROTOCOL);
    cardDfName = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_DFNAME);

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
    cardProcedure.RL_UC_InitializeContext();

    // Prepare PO reader
    readerProcedure.RL_UR_SetupCardReader(cardReaderName, isCardReaderContactless, cardProtocol);

    cardProcedure.RL_UC_CreateCardSelection(cardDfName);
  }

  /**
   * Executed once after each individual test of this class.
   *
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    cardProcedure.RL_UC_ResetContext();
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
  public void RL_22() throws Exception {
    // Display test infos
    logger.info(
        "Ensure that the Reader Layer do not use the ISO/IEC 14443-4 PICC presence check method.");

    readerProcedure.RL_UR_ActivateSingleObservation();

    logger.info("Present smartcard");

    readerProcedure.RL_UR_WaitForCardInsertion();

    logger.info("Wait 1 second");
    readerProcedure.RL_UR_WaitMilliSeconds(1000);

    cardProcedure.RL_UC_InitializeGenericCardTransactionManager();
    cardProcedure.RL_UC_PrepareApdu("00B2013C1D");
    cardProcedure.RL_UC_ProcessApdusToHexStrings();
    assertThat(cardProcedure.RL_UC_IsApduSuccessful()).isTrue();

    logger.info("Wait 1 second");
    readerProcedure.RL_UR_WaitMilliSeconds(1000);

    logger.info("Remove smartcard");
    readerProcedure.RL_UR_WaitForCardRemoval();
  }
}

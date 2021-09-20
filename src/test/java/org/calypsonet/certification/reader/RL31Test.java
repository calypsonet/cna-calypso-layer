package org.calypsonet.certification.reader;

import static org.assertj.core.api.Assertions.assertThat;

import org.calypsonet.certification.ConfigProperties;
import org.calypsonet.certification.procedures.*;
import org.calypsonet.certification.procedures.CardProcedure;
import org.calypsonet.certification.procedures.CardProcedureAdapter;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RL31Test {

  private static final Logger logger = LoggerFactory.getLogger(RL31Test.class);
  private static ReaderProcedure readerProcedure;
  private static CardProcedure cardProcedure;
  private static String cardReaderName;
  private static String cardReaderType;
  private static boolean isCardReaderContactless;
  private static String cardProtocol;
  private static String cardDfName;
  private List<String> apduList;

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
    cardProcedure = new CardProcedureAdapter(parameterDto);

    // Configuration parameters
    cardReaderName = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_2_NAME);
    cardReaderType = ConfigProperties.getValue(ConfigProperties.Key.CARD_READER_2_TYPE);
    cardProtocol = ConfigProperties.getValue(ConfigProperties.Key.CARD_2_PROTOCOL);
    cardDfName = ConfigProperties.getValue(ConfigProperties.Key.CARD_2_DFNAME);

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

    readerProcedure.RL_UR_IsCardPresent();

    cardProcedure.RL_UC_PrepareCardSelection();

    cardProcedure.RL_UC_SelectCard();

    cardProcedure.RL_UC_InitializeGenericCardTransactionManager();
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
  public void RL_31() {
    // Display test infos
    logger.info(
        "Ensure that the Reader Layer manage correctly the SW1SW2=6CXXh status word when it receive a correct SW1SW2=6CXXh status word from a smartcard.");

    logger.info("Send SAM Read parameters APDU command Case 2 to the SAM reader");
    logger.info("CLA: 80");
    logger.info("INS: BE");
    logger.info("P1: 00");
    logger.info("P2: A0");
    logger.info("Le: 00");
    cardProcedure.RL_UC_PrepareApdu("80BE00A030");

    cardProcedure.RL_UC_ProcessApdusToHexStrings();

    assertThat(cardProcedure.RL_UC_IsApduSuccessful()).isTrue();
  }
}

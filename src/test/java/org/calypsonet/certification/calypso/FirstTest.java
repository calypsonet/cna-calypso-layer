package org.calypsonet.certification.calypso;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.selection.*;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstTest {

  private static final Logger logger = LoggerFactory.getLogger(FirstTest.class);
  private static SmartCardService smartCardService;
  private static Plugin plugin;
  private static String smartCardAID1;
  private static String dfName1;
  private static String poReaderName;
  private static String samReaderName;
  private static String cardProtocol;
  private static String poReaderType;
  private static String samReaderType;
  private static String samRevisionString;
  private Reader poReader;
  private PoSecuritySettings poSecuritySettings;

  /**
   * Executed once before running all tests of this class.
   *
   * @throws Exception
   */
  @BeforeClass
  public static void beforeClass() throws Exception {

    // Configuration parameters
    poReaderName = ConfigProperties.getValue(ConfigProperties.Key.READER_1_NAME);
    poReaderType = ConfigProperties.getValue(ConfigProperties.Key.READER_1_TYPE);
    samReaderName = ConfigProperties.getValue(ConfigProperties.Key.READER_2_NAME);
    samReaderType = ConfigProperties.getValue(ConfigProperties.Key.READER_2_TYPE);
    cardProtocol = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_PROTOCOL);
    smartCardAID1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_AID);
    dfName1 = ConfigProperties.getValue(ConfigProperties.Key.CARD_1_DFNAME);
    samRevisionString = ConfigProperties.getValue(ConfigProperties.Key.SAM_1_REVISION);

    String pluginName = ConfigProperties.getValue(ConfigProperties.Key.PLUGIN_NAME);
    PluginFactory pluginFactory;

    if (ConfigProperties.PLUGIN_NAME_STUB.equals(pluginName)) {
      pluginFactory = new StubPluginFactory(pluginName, null, null);
    } else if (ConfigProperties.PLUGIN_NAME_PCSC.equals(pluginName)) {
      pluginFactory = new PcscPluginFactory(null, null);
    } else {
      pluginFactory = new PcscPluginFactory(null, null);
      // throw new IllegalStateException("Bad plugin name : " + pluginName);
    }

    // Gets the SmartCardService
    smartCardService = SmartCardService.getInstance();

    // Register the Plugin with SmartCardService, get the corresponding Plugin in return
    plugin = SmartCardService.getInstance().registerPlugin(pluginFactory);
  }

  /**
   * Select the SAM and create a SamResource
   *
   * @param samRevision the required SAM revision
   * @return A SAM resource
   */
  private CardResource<CalypsoSam> getSamResource(Reader samReader, SamRevision samRevision) {
    CardSelectionsService samSelection = new CardSelectionsService();

    SamSelector samSelector =
        SamSelector.builder().samRevision(samRevision).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelection(samSelector));

    CardSelectionsResult cardSelectionsResult = samSelection.processExplicitSelections(samReader);

    if (!cardSelectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("SAM matching failed!");
    }

    // Associate the calypsoSam and the samReader to create a samResource
    return new CardResource<CalypsoSam>(
        samReader, (CalypsoSam) cardSelectionsResult.getActiveSmartCard());
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
    poReader = plugin.getReader(poReaderName);
    if (ConfigProperties.READER_TYPE_CONTACTLESS.equalsIgnoreCase(poReaderType)) {
      // Get and configure a contactless reader
      ((PcscReader) poReader).setContactless(true);
      ((PcscReader) poReader).setIsoProtocol(PcscReader.IsoProtocol.T1);
    } else if (ConfigProperties.READER_TYPE_CONTACT.equalsIgnoreCase(poReaderType)) {
      // Get and configure a contactless reader
      ((PcscReader) poReader).setContactless(false);
      ((PcscReader) poReader).setIsoProtocol(PcscReader.IsoProtocol.T0);
    } else {
      throw new IllegalStateException("Unknown reader type " + poReaderType);
    }

    // Activate PO protocols
    if (ContactlessCardCommonProtocols.NFC_A_ISO_14443_3A.name().equals(cardProtocol)) {
      poReader.activateProtocol(PcscSupportedContactlessProtocols.ISO_14443_4.name(), cardProtocol);
    } else if (ContactlessCardCommonProtocols.NFC_B_ISO_14443_3B.name().equals(cardProtocol)) {
      poReader.activateProtocol(PcscSupportedContactlessProtocols.ISO_14443_4.name(), cardProtocol);
    } else if (ContactCardCommonProtocols.ISO_7816_3_TO.name().equals(cardProtocol)) {
      poReader.activateProtocol(PcscSupportedContactProtocols.ISO_7816_3_T0.name(), cardProtocol);
    } else if (ContactCardCommonProtocols.ISO_7816_3_T1.name().equals(cardProtocol)) {
      poReader.activateProtocol(PcscSupportedContactProtocols.ISO_7816_3_T1.name(), cardProtocol);
    } else {
      throw new IllegalArgumentException(
          "Protocol not supported by this PC/SC reader: " + cardProtocol);
    }

    // Prepare SAM reader
    Reader samReader = plugin.getReader(samReaderName);
    if (ConfigProperties.READER_TYPE_CONTACT.equalsIgnoreCase(samReaderType)) {
      // Get and configure a contactless reader
      ((PcscReader) samReader).setContactless(false);
      ((PcscReader) samReader).setIsoProtocol(PcscReader.IsoProtocol.T0);
    } else {
      throw new IllegalStateException("Unexpected SAM reader type " + samReaderType);
    }

    SamRevision samRevision;

    if (SamRevision.AUTO.name().equals(samRevisionString)) {
      samRevision = SamRevision.AUTO;
    } else if (SamRevision.C1.name().equals(samRevisionString)) {
      samRevision = SamRevision.C1;
    } else if (SamRevision.S1D.name().equals(samRevisionString)) {
      samRevision = SamRevision.S1D;
    } else if (SamRevision.S1E.name().equals(samRevisionString)) {
      samRevision = SamRevision.S1E;
    } else {
      throw new IllegalStateException("Unexpected SAM revision " + samRevisionString);
    }

    // Prepare the security settings used during the Calypso transaction
    poSecuritySettings =
        new PoSecuritySettings.PoSecuritySettingsBuilder(getSamResource(samReader, samRevision))
            .build();
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
    // unregister plugin
    smartCardService.unregisterPlugin(plugin.getName());
  }

  @Test
  public void CL_116() {
    // Display test infos
    logger.info(
        "Ensure that Calypso Layer does not update the data of the current DF or current EF, if a new Select command sent return an error.");

    // Prepare the card selection
    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    // First selection case targeting cards with AID1
    PoSelection cardSelection =
        new PoSelection(
            PoSelector.builder()
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(smartCardAID1).build())
                .build());

    // Add the selection case to the current selection
    cardSelectionsService.prepareSelection(cardSelection);

    // Actual card communication: operate through a single request the card selection
    CardSelectionsResult cardSelectionsResult =
        cardSelectionsService.processExplicitSelections(poReader);

    CalypsoPo calypsoPo = (CalypsoPo) cardSelectionsResult.getActiveSmartCard();

    assertThat(calypsoPo.getDfName()).isEqualToIgnoringCase(dfName1);

    // Create the PO resource
    CardResource<CalypsoPo> poResource = new CardResource<CalypsoPo>(poReader, calypsoPo);

    PoTransaction poTransaction =
        new PoTransaction(new CardResource<CalypsoPo>(poReader, calypsoPo), poSecuritySettings);

    poTransaction.prepareReadRecordFile((byte) 0x07, 1);

    poTransaction.processOpening(PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT);

    /////////////////////////////////////////////
    logger.info("PROCEDURE");
    /////////////////////////////////////////////

    poTransaction.prepareReleasePoChannel();

    poTransaction.processClosing();

    // Verify the application is no longer selected
    // assertThat(calypsoPo.getDfName()).isEqualToIgnoringCase("");
  }
}

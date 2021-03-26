package org.calypsonet.certification.procedures;

import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.calypsonet.certification.stub.StubCalypsoClassic;
import org.calypsonet.certification.stub.StubSamCalypsoClassic;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.transaction.*;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.*;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.util.ContactCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactProtocols;
import org.eclipse.keyple.plugin.pcsc.PcscSupportedContactlessProtocols;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.plugin.stub.StubSmartCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcedureAdapter implements Procedure, ObservableReader.ReaderObserver, PluginObservationExceptionHandler, ReaderObservationExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(ProcedureAdapter.class);

  private SmartCardService smartCardService;
  private Plugin plugin;
  private Reader poReader;
  private Reader samReader;
  private PoSecuritySettings poSecuritySettings;
  private CalypsoPo calypsoPo;
  private CardResource<CalypsoPo> poResource;
  private PoTransaction poTransaction;
  private CardResponse cardResponse;
  private AbstractSmartCard selectedCard;
  private ReaderEvent.EventType eventType;
  private Reader eventReader;

  // This object is used to freeze the main thread while card operations are handle through the
  // observers callbacks. A call to the notify() method would end the program (not demonstrated
  // here).
  private static final Object waitForEnd = new Object();

  @Override
  public void onPluginObservationError(String pluginName, Throwable e) {

  }

  @Override
  public void onReaderObservationError(String pluginName, String readerName, Throwable e) {

  }

  /** Create a new class extending AbstractCardSelection */
  public final class GenericCardSelection extends AbstractCardSelection {
    public GenericCardSelection(CardSelector cardSelector) {
      super(cardSelector);
    }

    @Override
    protected AbstractSmartCard parse(CardSelectionResponse cardSelectionResponse) {
      class GenericSmartCard extends AbstractSmartCard {
        public GenericSmartCard(CardSelectionResponse cardSelectionResponse) {
          super(cardSelectionResponse);
        }

        public String toJson() {
          return "{}";
        }
      }
      return new GenericSmartCard(cardSelectionResponse);
    }
  }

  @Override
  public void initializeContext(String... pluginsNames) {

    // Gets the SmartCardService
    smartCardService = SmartCardService.getInstance();

    // Register the first plugin
    String pluginName = pluginsNames[0];
    PluginFactory pluginFactory;
    if ("stub".equals(pluginName)) {
      pluginFactory = new StubPluginFactory(pluginName, this, this);
    } else if ("pcsc".equals(pluginName)) {
      pluginFactory = new PcscPluginFactory(this, this);
    } else {
      pluginFactory = new PcscPluginFactory(this, this);
      // throw new IllegalStateException("Bad plugin name : " + pluginName);
    }
    plugin = smartCardService.registerPlugin(pluginFactory);
  }

  @Override
  public void resetContext() {

    // Unregister plugins
    smartCardService.unregisterPlugin(plugin.getName());
  }

  @Override
  public void setupPoReader(String readerName, boolean isContactless, String cardProtocol) {

    // Prepare PO reader
    poReader = plugin.getReader(readerName);
    if (isContactless) {
      // Get and configure a contactless reader
      if (poReader instanceof PcscReader) {
        ((PcscReader) poReader).setContactless(true);
        ((PcscReader) poReader).setSharingMode(PcscReader.SharingMode.SHARED);
        ((PcscReader) poReader).setIsoProtocol(PcscReader.IsoProtocol.T1);
      }
    } else {
      // Get and configure a contact reader
      if (poReader instanceof PcscReader) {
        ((PcscReader) poReader).setContactless(false);
        ((PcscReader) poReader).setSharingMode(PcscReader.SharingMode.SHARED);
        ((PcscReader) poReader).setIsoProtocol(PcscReader.IsoProtocol.T0);
      }
    }

    if (poReader instanceof StubReader) {
      // Create 'virtual' Calypso card
      StubSmartCard stubPo = new StubCalypsoClassic();
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
  }

  @Override
  public void setupPoSecuritySettings(String readerName, String samRevision) {

    // Prepare SAM reader
    samReader = plugin.getReader(readerName);

    // Get and configure a contactless reader
    if (samReader instanceof PcscReader) {
      ((PcscReader) samReader).setContactless(false);
      ((PcscReader) samReader).setIsoProtocol(PcscReader.IsoProtocol.T0);
    }

    if (poReader instanceof StubReader) {
      // Create 'virtual' SAM card
      StubSmartCard stubSam = new StubSamCalypsoClassic();
    }

    SamRevision revision;
    if (SamRevision.AUTO.name().equals(samRevision)) {
      revision = SamRevision.AUTO;
    } else if (SamRevision.C1.name().equals(samRevision)) {
      revision = SamRevision.C1;
    } else if (SamRevision.S1D.name().equals(samRevision)) {
      revision = SamRevision.S1D;
    } else if (SamRevision.S1E.name().equals(samRevision)) {
      revision = SamRevision.S1E;
    } else {
      throw new IllegalStateException("Unexpected SAM revision " + samRevision);
    }

    // Prepare the security settings used during the Calypso transaction
    CardSelectionsService samSelection = new CardSelectionsService();

    SamSelector samSelector =
        SamSelector.builder().samRevision(revision).serialNumber(".*").build();

    // Prepare selector
    samSelection.prepareSelection(new SamSelection(samSelector));

    CardSelectionsResult cardSelectionsResult = samSelection.processExplicitSelections(samReader);

    if (!cardSelectionsResult.hasActiveSelection()) {
      throw new IllegalStateException("SAM matching failed!");
    }

    // Associate the calypsoSam and the samReader to create a samResource
    CardResource<CalypsoSam> samResource =
        new CardResource<CalypsoSam>(
            samReader, (CalypsoSam) cardSelectionsResult.getActiveSmartCard());

    poSecuritySettings = new PoSecuritySettings.PoSecuritySettingsBuilder(samResource).build();
  }

  @Override
  public void selectPo(String aid) {

    // Prepare the card selection
    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    // First selection case targeting cards with AID1
    PoSelection cardSelection =
        new PoSelection(
            PoSelector.builder()
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(aid).build())
                .build());

    // Add the selection case to the current selection
    cardSelectionsService.prepareSelection(cardSelection);

    // Actual card communication: operate through a single request the card selection
    CardSelectionsResult cardSelectionsResult =
        cardSelectionsService.processExplicitSelections(poReader);

    calypsoPo = (CalypsoPo) cardSelectionsResult.getActiveSmartCard();

    // Create the PO resource
    poResource = new CardResource<CalypsoPo>(poReader, calypsoPo);
  }

  @Override
  public void initializePoTransaction() {
    poTransaction = new PoTransaction(new CardResource<CalypsoPo>(poReader, calypsoPo));
  }

  @Override
  public void initializeSecurePoTransaction() {
    poTransaction =
        new PoTransaction(new CardResource<CalypsoPo>(poReader, calypsoPo), poSecuritySettings);
  }

  @Override
  public void prepareReadRecord(int sfi, int recordNumber) {
    poTransaction.prepareReadRecordFile((byte) sfi, recordNumber);
  }

  @Override
  public void processOpening(SessionAccessLevel sessionAccessLevel) {
    PoTransaction.SessionSetting.AccessLevel level;
    switch (sessionAccessLevel) {
      case PERSO:
        level = PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_PERSO;
        break;
      case LOAD:
        level = PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_LOAD;
        break;
      case DEBIT:
        level = PoTransaction.SessionSetting.AccessLevel.SESSION_LVL_DEBIT;
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + sessionAccessLevel);
    }
    poTransaction.processOpening(level);
  }

  @Override
  public void prepareReleaseChannel() {
    poTransaction.prepareReleasePoChannel();
  }

  @Override
  public void processClosing() {
    poTransaction.processClosing();
  }

  @Override
  public String getPoDfName() {
    return calypsoPo.getDfName();
  }

  @Override
  public String getPoApplicationSerialNumber() {
    return calypsoPo.getApplicationSerialNumber();
  }

  @Override
  public String getPoStartupInfo() {
    return calypsoPo.getStartupInfo();
  }

  @Override
  public void activateSingleObservation(String aid) {
    // Prepare the card selection
    CardSelectionsService cardSelectionsService = new CardSelectionsService();

    // first selection case targeting cards with aid
    GenericCardSelection cardSelection =
        new GenericCardSelection(
            CardSelector.builder()
                .aidSelector(CardSelector.AidSelector.builder().aidToSelect(aid).build())
                .build());

    // Add the selection case to the current selection
    cardSelectionsService.prepareSelection(cardSelection);

    // Provide the Reader with the selection operation to be processed when a card is inserted.
    ((ObservableReader) poReader)
        .setDefaultSelectionRequest(
            cardSelectionsService.getDefaultSelectionsRequest(),
            ObservableReader.NotificationMode.ALWAYS,
            ObservableReader.PollingMode.SINGLESHOT);

    // Set the current class as Observer of the first reader
    ((ObservableReader) poReader).addObserver(this);
  }

  @Override
  public void waitMilliSeconds(int delay) {
    await()
        .pollDelay(delay, TimeUnit.MILLISECONDS)
        .until(
            new Callable<Boolean>() {
              @Override
              public Boolean call() {
                return true;
              }
            });
  }

  @Override
  public void waitForCardInsertion() {
    await().atMost(10, TimeUnit.SECONDS).until(eventOccurs(ReaderEvent.EventType.CARD_INSERTED, ReaderEvent.EventType.CARD_MATCHED));
  }

  @Override
  public void waitForCardRemoval() {
    //((ObservableReader) (eventReader)).finalizeCardProcessing();
    await().atMost(10, TimeUnit.SECONDS).until(eventOccurs(ReaderEvent.EventType.CARD_REMOVED));
  }

  @Override
  public void sendAPDU(String apdu, boolean case4) {
    List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();
    apduRequestList.add(new ApduRequest(ByteArrayUtil.fromHex(apdu), case4));
    CardRequest cardRequest = new CardRequest(apduRequestList);
    System.out.println(cardRequest.getApduRequests().toString());
    cardResponse =
        ((ProxyReader) poReader).transmitCardRequest(cardRequest, ChannelControl.KEEP_OPEN);
  }

  @Override
  public boolean isApduSuccessful() {
    return cardResponse.getApduResponses().get(0).isSuccessful();
  }

  /**
   * Method invoked in the case of a reader event
   *
   * @param event the reader event
   */
  @Override
  public void update(ReaderEvent event) {
    eventType = event.getEventType();
    if(event.getEventType() != ReaderEvent.EventType.UNREGISTERED) {
      eventReader = event.getReader();
    }
  }

  Callable<Boolean> eventOccurs(final ReaderEvent.EventType... events) {
    return new Callable<Boolean>() {
      public Boolean call() {
        for(ReaderEvent.EventType event:events) {
          if (eventType == event) {
            return true;
          }
        }
        return false;
      }
    };
  }
}

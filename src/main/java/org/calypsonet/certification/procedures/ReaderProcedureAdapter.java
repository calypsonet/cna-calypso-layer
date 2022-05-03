package org.calypsonet.certification.procedures;

import org.calypsonet.certification.spi.ReaderApiSpecific;
import org.calypsonet.certification.spi.ReaderApiSpecificAdapter;
import org.calypsonet.terminal.reader.*;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.CardSelectionResult;
import org.calypsonet.terminal.reader.spi.CardReaderObservationExceptionHandlerSpi;
import org.calypsonet.terminal.reader.spi.CardReaderObserverSpi;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class ReaderProcedureAdapter implements ReaderProcedure, CardReaderObserverSpi, CardReaderObservationExceptionHandlerSpi {

  private static final Logger logger = LoggerFactory.getLogger(ReaderProcedureAdapter.class);

  public final CommonDto commonDto;
  private CardSelectionManager cardSelectionManager;
  private CardSelectionResult cardSelectionResult;
  private CardReaderEvent.Type eventType;
  private CardReader eventReader;

  public ReaderApiSpecific readerApiSpecific = new ReaderApiSpecificAdapter();

  // This object is used to freeze the main thread while card operations are handle through the
  // observers callbacks. A call to the notify() method would end the program (not demonstrated
  // here).
  private static final Object waitForEnd = new Object();

  public ReaderProcedureAdapter(CommonDto commonDto) {
    this.commonDto = commonDto;
  }

  @Override
  public void onReaderObservationError(String pluginName, String readerName, Throwable e) {

  }

  @Override
  public void RL_UR_InitializeContext() {
    readerApiSpecific.initializeReaderContext();
  }

  @Override
  public void RL_UR_ResetContext() {
    readerApiSpecific.resetReaderContext();
  }

  @Override
  public void RL_UR_SetupCardReader(String readerName, boolean isContactless, String cardProtocol) {

    // Prepare PO reader
    commonDto.cardReader = readerApiSpecific.setupCardReader(readerName, isContactless, cardProtocol);

    // Activate card protocols
    /*if (cardProtocol.equals("NFC_A_ISO_14443_3A")) {
      ((ConfigurableCardReader)cardReader).activateProtocol(ContactlessCardCommonProtocol.ISO_14443_4.name(), cardProtocol);
    } else if (cardProtocol.equals("NFC_B_ISO_14443_3B")) {
      ((ConfigurableCardReader)cardReader).activateProtocol(ContactlessCardCommonProtocol.ISO_14443_4.name(), cardProtocol);
    } else if (cardProtocol.equals("ISO_7816_3_TO")) {
      ((ConfigurableCardReader)cardReader).activateProtocol(ContactCardCommonProtocol.ISO_7816_3_T0.name(), cardProtocol);
    } else if (cardProtocol.equals("ISO_7816_3_T1")) {
      ((ConfigurableCardReader)cardReader).activateProtocol(ContactCardCommonProtocol.ISO_7816_3_T1.name(), cardProtocol);
    } else {
      throw new IllegalArgumentException(
          "Protocol not supported by this PC/SC reader: " + cardProtocol);
    }*/

    // Get the core card selection manager.
    cardSelectionManager = readerApiSpecific.createCardSelectionManager();
  }


  @Override
  public void RL_UR_SelectCard() {

    // Prepare the selection by adding the created card selection to the card selection
    // scenario.
    cardSelectionManager.prepareSelection(commonDto.cardSelection);

    // Actual card communication: run the selection scenario.
    cardSelectionResult = cardSelectionManager.processCardSelectionScenario(commonDto.cardReader);

    // Check the selection result.
    if (cardSelectionResult.getActiveSmartCard() != null) {
      // Get the SmartCard resulting of the selection.
      commonDto.smartCard = cardSelectionResult.getActiveSmartCard();
    }
  }

  @Override
  public void PrepareReleaseChannel() {
    cardSelectionManager.prepareReleaseChannel();
  }

  @Override
  public String RL_UR_GetPowerOnData() {
    return commonDto.smartCard.getPowerOnData();
  }

  @Override
  public String RL_UR_SelectApplicationResponse() {
    return ByteArrayUtil.toHex(commonDto.smartCard.getSelectApplicationResponse());
  }

  @Override
  public void RL_UR_WaitMilliSeconds(int delay) {
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
  public void RL_UR_WaitForCardInsertion() {
    await().atMost(10, TimeUnit.SECONDS).until(eventOccurs(CardReaderEvent.Type.CARD_INSERTED, CardReaderEvent.Type.CARD_MATCHED));
  }

  @Override
  public void RL_UR_WaitForCardRemoval() {
    //((ObservableCardReader) (eventReader)).finalizeCardProcessing();
    ((ObservableCardReader) (commonDto.cardReader)).finalizeCardProcessing();
    await().atMost(10, TimeUnit.SECONDS).until(eventOccurs(CardReaderEvent.Type.CARD_REMOVED));
  }


  /**
   * Method invoked in the case of a reader event
   *
   * @param event the reader event
   */
  @Override
  public void onReaderEvent(CardReaderEvent event) {
    eventType = event.getType();
    if(eventType != CardReaderEvent.Type.UNAVAILABLE) {
      //eventReader = event.getReaderName();
      commonDto.smartCard =
              cardSelectionManager
                      .parseScheduledCardSelectionsResponse(event.getScheduledCardSelectionsResponse())
                      .getActiveSmartCard();
    }
  }

  Callable<Boolean> eventOccurs(final CardReaderEvent.Type... events) {
    return new Callable<Boolean>() {
      public Boolean call() {
        for(CardReaderEvent.Type event:events) {
          if (eventType == event) {
            return true;
          }
        }
        return false;
      }
    };
  }

  ///////////////////////////////////////////////////////////////////////
  // org.calypsonet.terminal.reader
  // Interface CardReader: GetName, IsCardPresent, IsContactless
  ///////////////////////////////////////////////////////////////////////
  @Override
  public String RL_UR_GetReaderName() {
    return commonDto.cardReader.getName();
  }

  @Override
  public boolean RL_UR_IsCardPresent() {
    // Check the card presence
    if (!commonDto.cardReader.isCardPresent()) {
      throw new IllegalStateException("No card is present in the reader " + commonDto.cardReader.getName());
    }
    return commonDto.cardReader.isCardPresent();
  }

  @Override
  public boolean RL_UR_IsContactless() {
    // Check if the reader is contactless
    return commonDto.cardReader.isContactless();
  }

  ///////////////////////////////////////////////////////////////////////

  ///////////////////////////////////////////////////////////////////////
  // org.calypsonet.terminal.reader
  // Interface ConfigurableCardReader: activateProtocol, deactivateProtocol
  ///////////////////////////////////////////////////////////////////////
  @Override
  public void RL_UR_ActivateProtocol(String ReaderProtocol, String CardProtocol) {
    // Deactivate Protocol
    ((ConfigurableCardReader)commonDto.cardReader).activateProtocol(ReaderProtocol, CardProtocol);
  }

  @Override
  public void RL_UR_DeActivateProtocol(String CurrentProtocol) {
    // Deactivate Protocol
    ((ConfigurableCardReader)commonDto.cardReader).deactivateProtocol(CurrentProtocol);
  }
  ///////////////////////////////////////////////////////////////////////


  // For Observable Readers
  @Override
  public void RL_UR_ActivateSingleObservation() {

    // Prepare the selection by adding the created card selection to the card selection
    // scenario.
    cardSelectionManager.prepareSelection(commonDto.cardSelection);

    // Schedule the selection scenario.
    cardSelectionManager.scheduleCardSelectionScenario(
            (ObservableCardReader) commonDto.cardReader,
            ObservableCardReader.DetectionMode.SINGLESHOT,
            ObservableCardReader.NotificationMode.ALWAYS);

    // Create and add an observer
    ((ObservableCardReader) commonDto.cardReader).setReaderObservationExceptionHandler(this);
    ((ObservableCardReader) commonDto.cardReader).addObserver(this);
    ((ObservableCardReader) commonDto.cardReader).startCardDetection(ObservableCardReader.DetectionMode.REPEATING);
  }

  @Override
  public void RL_UR_DeActivateSingleObservation() {
    ((ObservableCardReader) commonDto.cardReader).stopCardDetection();
  }

  @Override
  public void RL_UR_CardSelection() {

    // Prepare the selection by adding the created card selection to the card selection
    // scenario.
    cardSelectionManager.prepareSelection(commonDto.cardSelection);
    cardSelectionManager.processCardSelectionScenario(commonDto.cardReader);
  }


}

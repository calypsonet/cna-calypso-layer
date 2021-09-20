package org.calypsonet.certification.procedures;

import org.calypsonet.certification.spi.ReaderApiSpecific;
import org.calypsonet.certification.spi.ReaderApiSpecificAdapter;
import org.calypsonet.terminal.reader.CardReader;
import org.calypsonet.terminal.reader.CardReaderEvent;
import org.calypsonet.terminal.reader.ObservableCardReader;
import org.calypsonet.terminal.reader.spi.CardReaderObservationExceptionHandlerSpi;
import org.calypsonet.terminal.reader.spi.CardReaderObserverSpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class ReaderProcedureAdapter implements ReaderProcedure , CardReaderObserverSpi, CardReaderObservationExceptionHandlerSpi {

  private static final Logger logger = LoggerFactory.getLogger(ReaderProcedureAdapter.class);

  private final ParameterDto parameterDto;
  private CardReaderEvent.Type eventType;
  private CardReader eventReader;

  private ReaderApiSpecific readerLayerSpecific = new ReaderApiSpecificAdapter();

  // This object is used to freeze the main thread while card operations are handle through the
  // observers callbacks. A call to the notify() method would end the program (not demonstrated
  // here).
  private static final Object waitForEnd = new Object();

  public ReaderProcedureAdapter(ParameterDto parameterDto) {
    this.parameterDto = parameterDto;
  }

  @Override
  public void onReaderObservationError(String pluginName, String readerName, Throwable e) {

  }

  @Override
  public void RL_UR_InitializeContext() {
    readerLayerSpecific.initializeReaderContext();
  }

  @Override
  public void RL_UR_ResetContext() {
    readerLayerSpecific.resetReaderContext();
  }

  @Override
  public void RL_UR_SetupCardReader(String readerName, boolean isContactless, String cardProtocol) {

    // Prepare PO reader
    parameterDto.cardReader = readerLayerSpecific.setupCardReader(readerName, isContactless, cardProtocol);

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
    parameterDto.cardSelectionManager = readerLayerSpecific.createCardSelectionManager();
  }

  @Override
  public void RL_UR_SetupSamReader(String readerName) {

    parameterDto.samReader = readerLayerSpecific.setupSamReader(readerName);

    // Create a SAM selection manager.
    parameterDto.samSelectionManager = readerLayerSpecific.createCardSelectionManager();
  }

  @Override
  public void RL_UR_IsCardPresent() {
    // Check the card presence
    if (!parameterDto.cardReader.isCardPresent()) {
      throw new IllegalStateException("No card is present in the reader " + parameterDto.cardReader.getName());
    }
  }

  public void RL_UR_IsSamPresent() {
    // Check the SAM presence
    if (!parameterDto.samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + parameterDto.samReader.getName());
    }
  }

  @Override
  public void RL_UR_ActivateSingleObservation() {
   /* // Prepare the card selection
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
    ((ObservableReader) poReader).addObserver(this);*/

    // Schedule the selection scenario.
    parameterDto.cardSelectionManager.scheduleCardSelectionScenario(
            (ObservableCardReader) parameterDto.cardReader,
            ObservableCardReader.DetectionMode.SINGLESHOT,
            ObservableCardReader.NotificationMode.ALWAYS);

    // Create and add an observer
    ((ObservableCardReader) parameterDto.cardReader).setReaderObservationExceptionHandler(this);
    ((ObservableCardReader) parameterDto.cardReader).addObserver(this);
    ((ObservableCardReader) parameterDto.cardReader).startCardDetection(ObservableCardReader.DetectionMode.REPEATING);
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
    ((ObservableCardReader) (parameterDto.cardReader)).finalizeCardProcessing();
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
      parameterDto.smartCard =
              parameterDto.cardSelectionManager
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

}

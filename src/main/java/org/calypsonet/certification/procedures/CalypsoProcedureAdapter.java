package org.calypsonet.certification.procedures;

import static org.awaitility.Awaitility.await;

import org.calypsonet.certification.spi.CalypsoApiSpecific;
import org.calypsonet.certification.spi.CalypsoApiSpecificAdapter;
import org.calypsonet.terminal.calypso.*;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.card.CalypsoCardSelection;
import org.calypsonet.terminal.calypso.sam.CalypsoSam;
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting;
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalypsoProcedureAdapter implements CalypsoProcedure {//}, CardReaderObserverSpi, CardReaderObservationExceptionHandlerSpi {

  private static final Logger logger = LoggerFactory.getLogger(CalypsoProcedureAdapter.class);

  private final ParameterDto parameterDto;
  private CalypsoCard calypsoCard;
  private CalypsoSam calypsoSam;
  private CardSecuritySetting cardSecuritySetting;
  private CardTransactionManager cardTransactionManager;

  private CalypsoApiSpecific calypsoLayerSpecific = new CalypsoApiSpecificAdapter();

  // This object is used to freeze the main thread while card operations are handle through the
  // observers callbacks. A call to the notify() method would end the program (not demonstrated
  // here).
  private static final Object waitForEnd = new Object();

  public CalypsoProcedureAdapter(ParameterDto parameterDto) {
    this.parameterDto = parameterDto;
  }

/* @Override
  public void onPluginObservationError(String pluginName, Throwable e) {

  }

  @Override
  public void onReaderObservationError(String pluginName, String readerName, Throwable e) {

  }*/

  /** Create a new class extending AbstractCardSelection */
  /*public final class GenericCardSelection extends AbstractCardSelection {
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
  }*/

  @Override
  public void CL_UT_InitializeContext() {
    calypsoLayerSpecific.initializeCalypsoContext();
  }

  @Override
  public void CL_UT_ResetContext() {
    calypsoLayerSpecific.resetCalypsoContext();
  }

  @Override
  public void CL_UT_SetupCardSecuritySetting() {
    // Create a SAM selection using the Calypso card extension.
    parameterDto.samSelectionManager.prepareSelection(
            calypsoLayerSpecific.createSamSelection());

    // Actual card communication: process the SAM selection.
    parameterDto.samSelectionResult =
            parameterDto.samSelectionManager.processCardSelectionScenario(parameterDto.samReader);

    // Check the selection result.
    if (parameterDto.samSelectionResult.getActiveSmartCard() == null) {
      throw new IllegalStateException(
              "The SAM selection failed.");
    }

    // Get the SmartCard resulting of the selection.
    calypsoSam = (CalypsoSam) parameterDto.samSelectionResult.getActiveSmartCard();
  }

  @Override
  public void CL_UT_PrepareCardSelection(String aid) {
    // First selection case targeting cards with AID1
    // Create a card selection.
    CalypsoCardSelection calypsoCardSelection = calypsoLayerSpecific.createCardSelection();

    // Prepare the selection by adding the created Calypso card selection to the card selection
    // scenario.
    parameterDto.cardSelectionManager.prepareSelection(
            calypsoCardSelection
                    .filterByDfName(aid)
                    .acceptInvalidatedCard());
  }

  @Override
  public void CL_UT_SelectCard() {
    // Actual card communication: run the selection scenario.
    parameterDto.cardSelectionResult =
            parameterDto.cardSelectionManager.processCardSelectionScenario(parameterDto.cardReader);

    // Check the selection result.
    if (parameterDto.cardSelectionResult.getActiveSmartCard() != null) {
      // Get the SmartCard resulting of the selection.
      calypsoCard = (CalypsoCard) parameterDto.cardSelectionResult.getActiveSmartCard();
    }
  }

  @Override
  public void CL_UT_InitializeCardTransactionManager() {
    cardSecuritySetting = calypsoLayerSpecific.createCardSecuritySetting();

    cardSecuritySetting.setSamResource(parameterDto.samReader, calypsoSam);

    cardTransactionManager = calypsoLayerSpecific.createCardTransaction(parameterDto.cardReader, calypsoCard, cardSecuritySetting);
  }

  @Override
  public void CL_UT_PrepareReadRecord(int sfi, int recordNumber) {
    cardTransactionManager.prepareReadRecordFile((byte) sfi, recordNumber);
  }

  @Override
  public void CL_UT_ProcessOpening(SessionAccessLevel sessionAccessLevel) {
    WriteAccessLevel level;
    switch (sessionAccessLevel) {
      case PERSONALIZATION:
        level = WriteAccessLevel.PERSONALIZATION;
        break;
      case LOAD:
        level = WriteAccessLevel.LOAD;
        break;
      case DEBIT:
        level = WriteAccessLevel.DEBIT;
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + sessionAccessLevel);
    }
    cardTransactionManager.processOpening(level);
  }

  @Override
  public void CL_UT_PrepareReleaseCardChannel() {
    cardTransactionManager.prepareReleaseCardChannel();
  }

  @Override
  public void CL_UT_ProcessClosing() {
    cardTransactionManager.processClosing();
  }

  @Override
  public String CL_UT_GetCardDfName() {
    return ByteArrayUtil.toHex(calypsoCard.getDfName());
  }

  @Override
  public String CL_UT_GetCardApplicationSerialNumber() {
    return ByteArrayUtil.toHex(calypsoCard.getApplicationSerialNumber());
  }

  @Override
  public String CL_UT_GetCardStartupInfo() {
    return ByteArrayUtil.toHex(calypsoCard.getStartupInfoRawData());
  }

  @Override
  public boolean CL_UT_IsPkiModeSupported() {
    return calypsoCard.isPkiModeSupported();
  }

  @Override
  public boolean CL_UT_IsExtendedModeSupported() {
    return calypsoCard.isExtendedModeSupported();
  }

}

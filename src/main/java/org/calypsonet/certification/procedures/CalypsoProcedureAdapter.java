package org.calypsonet.certification.procedures;

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

  private final CommonDto commonDto;
  private CalypsoCard calypsoCard;
  private CalypsoSam calypsoSam;
  private CardSecuritySetting cardSecuritySetting;
  private CardTransactionManager cardTransactionManager;

  private CalypsoApiSpecific calypsoApiSpecific = new CalypsoApiSpecificAdapter();

  // This object is used to freeze the main thread while card operations are handle through the
  // observers callbacks. A call to the notify() method would end the program (not demonstrated
  // here).
  private static final Object waitForEnd = new Object();

  public CalypsoProcedureAdapter(CommonDto commonDto) {
    this.commonDto = commonDto;
  }

  @Override
  public void CL_UT_InitializeContext() {
    calypsoApiSpecific.initializeCalypsoContext();
  }

  @Override
  public void CL_UT_ResetContext() {
    calypsoApiSpecific.resetCalypsoContext();
  }

  @Override
  public void CL_UT_CreateSamSelection() {
    // Create a SAM selection using the Calypso card extension.
    commonDto.samSelection = calypsoApiSpecific.createSamSelection();
  }

  public void CL_UT_SetSam(){ calypsoSam = (CalypsoSam) commonDto.samSmartCard; }

  @Override
  public void CL_UT_CreateCardSelection(String aid) {
    // First selection case targeting cards with AID1
    // Create a card selection.
    CalypsoCardSelection calypsoCardSelection = calypsoApiSpecific.createCardSelection();

    commonDto.cardSelection = calypsoCardSelection
            .filterByDfName(aid)
            .acceptInvalidatedCard();
  }

  public void CL_UT_SetCard(){ calypsoCard = (CalypsoCard) commonDto.smartCard; }

  @Override
  public void CL_UT_InitializeCardTransactionManager() {
    cardSecuritySetting = calypsoApiSpecific.createCardSecuritySetting();

    cardSecuritySetting.setSamResource(commonDto.samReader, calypsoSam);

    cardTransactionManager = calypsoApiSpecific.createCardTransaction(commonDto.cardReader, calypsoCard, cardSecuritySetting);
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

package org.calypsonet.certification.procedures;

import com.sun.jdi.ByteValue;
import net.bytebuddy.implementation.ToStringMethod;
import org.calypsonet.certification.spi.CalypsoApiSpecific;
import org.calypsonet.certification.spi.CalypsoApiSpecificAdapter;
import org.calypsonet.terminal.calypso.*;
import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.card.CalypsoCardSelection;
import org.calypsonet.terminal.calypso.sam.CalypsoSam;
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting;
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager;
import org.calypsonet.terminal.calypso.transaction.SvAction;
import org.calypsonet.terminal.calypso.transaction.SvOperation;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalypsoProcedureAdapter implements CalypsoProcedure {
  //}, CardReaderObserverSpi, CardReaderObservationExceptionHandlerSpi {

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

  public void CL_UT_SetSam() {
    calypsoSam = (CalypsoSam) commonDto.samSmartCard;
  }

  @Override
  public void CL_UT_CreateCardSelection(String aid) {
    // First selection case targeting cards with AID1
    // Create a card selection.
    CalypsoCardSelection calypsoCardSelection = calypsoApiSpecific.createCardSelection();

    commonDto.cardSelection = calypsoCardSelection
            .filterByDfName(aid)
            .acceptInvalidatedCard();
  }

  public void CL_UT_SetCard() {
    calypsoCard = (CalypsoCard) commonDto.smartCard;
  }

  @Override
  public void CL_UT_InitializeCardTransactionManager() {
    cardSecuritySetting = calypsoApiSpecific.createCardSecuritySetting();

    cardSecuritySetting.setSamResource(commonDto.samReader, calypsoSam);

    cardTransactionManager = calypsoApiSpecific.createCardTransaction(commonDto.cardReader, calypsoCard, cardSecuritySetting);
  }

  @Override
  public void CL_UT_PrepareReadRecord(byte sfi, int recordNumber) {
    cardTransactionManager.prepareReadRecordFile(sfi, recordNumber);
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

  @Override
  public void CL_UT_Prepare_SVReloadWithAmountOnly(int Amount) {
    cardTransactionManager.prepareSvReload(Amount);
  }

  @Override
  public void CL_UT_SVReloadWithAmountOnly(int Amount) {
    cardTransactionManager.prepareSvReload(Amount);
    cardTransactionManager.processCardCommands();
  }

  @Override
  public void CL_UT_Prepare_SVReload(int Amount, byte[] Date, byte[] Time, byte[] FreeData) {
    cardTransactionManager.prepareSvReload(Amount, Date, Time, FreeData);
  }

  @Override
  public void CL_UT_SVReload(int Amount, byte[] Date, byte[] Time, byte[] FreeData) {
    cardTransactionManager.prepareSvReload(Amount, Date, Time, FreeData);
    cardTransactionManager.processCardCommands();
  }

  @Override
  public void CL_UT_Prepare_SVDebitWithAmountOnly(int Amount) {
    cardTransactionManager.prepareSvDebit(Amount);
  }

  @Override
  public void CL_UT_SVDebitWithAmountOnly(int Amount) {
    cardTransactionManager.prepareSvDebit(Amount);
    cardTransactionManager.processCardCommands();
  }

  @Override
  public void CL_UT_Prepare_SVDebit(int Amount, byte[] Date, byte[] Time) {
    cardTransactionManager.prepareSvDebit(Amount, Date, Time);
  }

  @Override
  public void CL_UT_SVDebit(int Amount, byte[] Date, byte[] Time) {
    cardTransactionManager.prepareSvDebit(Amount, Date, Time);
    cardTransactionManager.processCardCommands();
  }

  @Override
  public void CL_UT_Prepare_SVGet(SvOperation OperationMode, SvAction ActionMode) {
    cardTransactionManager.prepareSvGet(OperationMode, ActionMode);
  }

  @Override
  public void CL_UT_SVGet(SvOperation OperationMode, SvAction ActionMode) {
    cardTransactionManager.prepareSvGet(OperationMode, ActionMode);
    cardTransactionManager.processCardCommands();
  }

  @Override
  public void CL_UT_Prepare_UpdateRecord(byte sfi, int recordNumber, byte[] DataField) {
    cardTransactionManager.prepareUpdateRecord(sfi, recordNumber, DataField);
  }

  @Override
  public void CL_UT_Prepare_AppendRecord(byte sfi, byte[] DataField) {
    cardTransactionManager.prepareAppendRecord(sfi, DataField);
  }

  @Override
  public void CL_UT_ProcessCardCommands() {
    cardTransactionManager.processCardCommands();
  }

  @Override
  public void CL_UT_ProcessCancel() {
    cardTransactionManager.processCancel();
  }

  @Override
  public boolean CL_UT_IsPreviousSessionRatified() {
    return calypsoCard.isDfRatified();
  }

  @Override
  public boolean CL_UT_IsRatificationOnDeselectFeatureSupported() {
    return calypsoCard.isRatificationOnDeselectSupported();
  }

  @Override
  public void CL_UT_EnableRatificationMechanism() {
    cardSecuritySetting.enableRatificationMechanism();
  }


  @Override
  public void CL_UT_SelectFileByLID(byte[] lid) {
    cardTransactionManager.prepareSelectFile(lid);
    cardTransactionManager.processCardCommands();
   }

  @Override
  public String CL_UT_GetApplicationSerialNumber() {
    return calypsoCard.getApplicationSerialNumber().toString();
  }

  @Override
  public void CL_UT_EnableMultipleSession() {
    cardSecuritySetting.enableMultipleSession();
  }

  @Override
  public void CL_UT_EnableTransactionAudit() {
    cardSecuritySetting.enableTransactionAudit();
  }

  @Override
  public String CL_UT_GetTransactionAuditData() {
    return cardTransactionManager.getTransactionAuditData();
  }

  @Override
  public int CL_UT_GetSVBalance() {
    return calypsoCard.getSvBalance();
  }

  @Override
  public void CL_UT_AuthorizeSVNegativeBalance() {
    cardSecuritySetting.authorizeSvNegativeBalance();
  }

  @Override
  public String CL_UT_GetSAMProductType() {
    return calypsoSam.getProductType().toString();
  }

  @Override
  public String CL_UT_GetSAMSerialNumber() {
    return calypsoSam.getSerialNumber().toString();
  }

  @Override
  public String CL_UT_GetSAMPlatform() {
    return Byte.toString(calypsoSam.getPlatform());
  }

  @Override
  public String CL_UT_GetSAMApplicationType() {
    return Byte.toString(calypsoSam.getApplicationType());
  }

  @Override
  public String CL_UT_GetSAMApplicationSubType() {
    return Byte.toString(calypsoSam.getApplicationSubType());
  }

  @Override
  public String CL_UT_GetSAMSoftwareIssuer() {
    return Byte.toString(calypsoSam.getSoftwareIssuer());
  }

  @Override
  public String CL_UT_GetSAMSoftwareVersion() {
    return Byte.toString(calypsoSam.getSoftwareVersion());
  }

  @Override
  public String CL_UT_GetSAMSoftwareRevision() {
    return Byte.toString(calypsoSam.getSoftwareRevision());
  }
}

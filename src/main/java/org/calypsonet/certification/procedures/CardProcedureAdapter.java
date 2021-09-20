package org.calypsonet.certification.procedures;

import org.calypsonet.certification.spi.CardApiSpecific;
import org.calypsonet.certification.spi.CardApiSpecificAdapter;
import org.calypsonet.certification.util.card.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class CardProcedureAdapter implements CardProcedure {

  private static final Logger logger = LoggerFactory.getLogger(CardProcedureAdapter.class);

  private final CommonDto commonDto;
  private GenericExtensionService genericExtensionService;
  private GenericCardTransactionManager genericCardTransactionManager;
  private List<byte[]> apduResponsesBytes;
  private List<String> apduResponsesHex;
  private CardApiSpecific cardApiSpecific = new CardApiSpecificAdapter();


  public CardProcedureAdapter(CommonDto commonDto) { this.commonDto = commonDto; }

  @Override
  public void RL_UC_InitializeContext() {

    cardApiSpecific.initializeCardContext();

    // Get the Generic card extension service
    genericExtensionService = GenericExtensionService.getInstance();
  }

  @Override
  public void RL_UC_ResetContext() { cardApiSpecific.resetCardContext(); }

  @Override
  public void RL_UC_CreateCardSelection() {
    // Create a card selection using the generic card extension without specifying any filter
    // (protocol/power-on data/DFName).
    GenericCardSelection genericCardSelection = genericExtensionService.createCardSelection();
    commonDto.cardSelection = genericCardSelection;
  }

  @Override
  public void RL_UC_CreateCardSelection(String aid) {
    // Create a card selection using the generic card extension without specifying any filter
    // (protocol/power-on data/DFName).
    GenericCardSelection genericCardSelection = genericExtensionService.createCardSelection();
    commonDto.cardSelection = genericCardSelection.filterByDfName(aid);
  }

  @Override
  public void RL_UC_InitializeGenericCardTransactionManager() {
    genericCardTransactionManager = genericExtensionService.createCardTransaction(commonDto.cardReader, commonDto.smartCard);
  }

  @Override
  public void RL_UC_PrepareApdu(String apduCommand) { genericCardTransactionManager.prepareApdu(apduCommand); }

  @Override
  public void RL_UC_PrepareApdu(byte[] apduCommand) { genericCardTransactionManager.prepareApdu(apduCommand); }

  @Override
  public void RL_UC_PrepareApdu(
          byte cla, byte ins, byte p1, byte p2, byte[] dataIn, Byte le) {
    genericCardTransactionManager.prepareApdu(cla, ins, p1, p2, dataIn, le);
  }

  @Override
  public void RL_UC_PrepareReleaseChannel() {
    genericCardTransactionManager.prepareReleaseChannel();
  }

  @Override
  public void RL_UC_ProcessApdusToByteArrays(){
    apduResponsesBytes = genericCardTransactionManager.processApdusToByteArrays();
  }

  @Override
  public void RL_UC_ProcessApdusToHexStrings(){
    apduResponsesHex = genericCardTransactionManager.processApdusToHexStrings();
  }

  @Override
  public boolean RL_UC_IsApduSuccessful() {
    return true;//cardResponse.getApduResponses().get(0).isSuccessful();
  }


}

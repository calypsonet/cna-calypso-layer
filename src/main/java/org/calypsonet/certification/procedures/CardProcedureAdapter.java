package org.calypsonet.certification.procedures;

import org.calypsonet.certification.spi.CardApiSpecific;
import org.calypsonet.certification.spi.CardApiSpecificAdapter;
import org.calypsonet.certification.util.card.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class CardProcedureAdapter implements CardProcedure {

  private static final Logger logger = LoggerFactory.getLogger(CardProcedureAdapter.class);

  private final ParameterDto parameterDto;
  private GenericExtensionService genericExtensionService;
  private GenericCardSelection genericCardSelection;
  private GenericCardTransactionManager genericCardTransactionManager;
  private List<byte[]> apduResponsesBytes;
  private List<String> apduResponsesHex;
  private CardApiSpecific cardApiSpecific = new CardApiSpecificAdapter();


  public CardProcedureAdapter(ParameterDto parameterDto) { this.parameterDto = parameterDto; }

  @Override
  public void RL_UC_InitializeContext() {

    cardApiSpecific.initializeCardContext();

    // Get the Generic card extension service
    genericExtensionService = GenericExtensionService.getInstance();
  }

  @Override
  public void RL_UC_ResetContext() { cardApiSpecific.resetCardContext(); }

  @Override
  public void RL_UC_PrepareCardSelection() {
    // Create a card selection using the generic card extension without specifying any filter
    // (protocol/power-on data/DFName).
    genericCardSelection = genericExtensionService.createCardSelection();

    // Prepare the selection by adding the created generic selection to the card selection scenario.
    parameterDto.cardSelectionManager.prepareSelection(genericCardSelection);
  }

  @Override
  public void RL_UC_PrepareCardSelection(String aid) {
    // Create a card selection using the generic card extension without specifying any filter
    // (protocol/power-on data/DFName).
    genericCardSelection = genericExtensionService.createCardSelection();

    // Prepare the selection by adding the created generic selection to the card selection scenario.
    parameterDto.cardSelectionManager.prepareSelection(genericCardSelection.filterByDfName(aid));
  }

  @Override
  public void RL_UC_SelectCard() {

    // Actual card communication: run the selection scenario.
    parameterDto.cardSelectionResult =
            parameterDto.cardSelectionManager.processCardSelectionScenario(parameterDto.cardReader);

    // Check the selection result.
    if (parameterDto.cardSelectionResult.getActiveSmartCard() != null) {
      // Get the SmartCard resulting of the selection.
      parameterDto.smartCard = parameterDto.cardSelectionResult.getActiveSmartCard();
    }
  }

  @Override
  public void RL_UC_InitializeGenericCardTransactionManager() {
    genericCardTransactionManager = genericExtensionService.createCardTransaction(parameterDto.cardReader, parameterDto.smartCard);
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

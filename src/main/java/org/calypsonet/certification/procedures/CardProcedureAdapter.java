package org.calypsonet.certification.procedures;

import org.calypsonet.certification.spi.CardApiSpecific;
import org.calypsonet.certification.spi.CardApiSpecificAdapter;
import org.calypsonet.certification.util.card.generic.*;
import org.calypsonet.certification.util.card.generic.GenericCardSelection;
import org.calypsonet.terminal.calypso.SelectFileControl;
import org.calypsonet.terminal.calypso.card.CalypsoCardSelection;
import org.calypsonet.terminal.card.ApduResponseApi;
import org.calypsonet.terminal.card.CardResponseApi;
import org.calypsonet.terminal.card.spi.CardSelectorSpi;
import org.calypsonet.terminal.reader.CardReader;
import org.eclipse.keyple.core.util.ApduUtil;
import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
  public List<String> RL_UC_ProcessApdusToHexStrings(){
    apduResponsesHex = genericCardTransactionManager.processApdusToHexStrings();
    return apduResponsesHex;
  }

  @Override
  public boolean RL_UC_CheckLastSW(List<String> apduresponse, String expectedstatusword) {
    String regularExpression = "[0-9a-fA-F]{0}.*" + expectedstatusword + "$";
    Pattern pattern = Pattern.compile(regularExpression);
    System.out.println("Regular Expression:" + regularExpression);
    System.out.println(apduresponse);
    Matcher expressionMatcher =
            pattern.matcher(apduresponse.get(0));

    return expressionMatcher.matches();
  }

  @Override
  public void RL_UC_CreateCardSelectionWithOccurrence(String aid, String occurrence) {
    GenericCardSelection genericCardSelection = genericExtensionService.createCardSelection();
    commonDto.cardSelection = genericCardSelection.filterByDfName(aid);
    commonDto.cardSelection = genericCardSelection.setFileOccurrence(GenericCardSelection.FileOccurrence.valueOf(occurrence));
   }

}

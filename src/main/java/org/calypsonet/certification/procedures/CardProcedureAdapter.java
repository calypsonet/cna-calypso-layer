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
	List<String> apduResponsesHex = new ArrayList<String>();
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
public boolean RL_UC_CheckSWInResponsesList(List<String> apduresponse, String expectedstatusword, int i) {
    String regularExpression = "[0-9a-fA-F]{0}.*" + expectedstatusword + "$";
    Pattern pattern = Pattern.compile(regularExpression);
    System.out.println("Regular Expression:" + regularExpression);
    System.out.println(apduresponse);
    Matcher expressionMatcher =
            pattern.matcher(apduresponse.get(i));

    return expressionMatcher.matches();
  }


  @Override
  public void RL_UC_CreateCardSelectionWithOccurrence(String aid, String occurrence) {
    GenericCardSelection genericCardSelection = genericExtensionService.createCardSelection();
    commonDto.cardSelection = genericCardSelection.filterByDfName(aid);
    commonDto.cardSelection = genericCardSelection.setFileOccurrence(GenericCardSelection.FileOccurrence.valueOf(occurrence));
   }

  @Override
  public List<String> RL_UC_PrepareDefaultAPDUsList() {
	List<String> defaultAPDUsList = new ArrayList<String>();
	defaultAPDUsList.add("00B2011400");
	defaultAPDUsList.add("00B2011C00");
	defaultAPDUsList.add("00B0840000");
	defaultAPDUsList.add("00E200184000112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00");
	return defaultAPDUsList;
  }
  
    @Override
  public List<String> RL_UC_PrepareDefaultAPDUsListWithCLAto02() {
	List<String> defaultAPDUsList = new ArrayList<String>();
	defaultAPDUsList.add("02B2011400");
	defaultAPDUsList.add("02B2011C00");
	defaultAPDUsList.add("02B0840000");
	defaultAPDUsList.add("02E200184000112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00112233445566778899AABBCCDDEEFF00");
	return defaultAPDUsList;
  }
  
  @Override
  public boolean RL_UC_CheckReaderCapabilityToAcceptAPDUs(List<String> APDUsList, int WaitingTime) {
	
	String APDU = "";
	List<String> apduResponse = new ArrayList<String>();
	Sting ExpectedSW = "9000";
	boolean LoopControl = true;
	
	if (WaitingTime != 0)
	{
		for (int i=0; LoopControl && i < APDUsList.size();i++)
		{
			APDU = APDUsList.get(i);
			RL_UC_PrepareApdu(APDU);
			apduresponse = RL_UC_ProcessApdusToHexStrings();
			if (RL_UC_CheckLastSW(apduresponse, ExpectedSW))
			{
				return true;
				RL_UR_WaitMilliSeconds(WaitingTime);
			}
			else 
			{
				LoopControl = false;
				return false;
			}
		}
	}
	else
	{
		for (int i=0; i < APDUsList.size();i++)
		{
			APDU = APDUsList.get(i);
			RL_UC_PrepareApdu(APDU);
		}
		apduresponse = RL_UC_ProcessApdusToHexStrings();
		for (int i=0; LoopControl && i < apduresponse.size();i++)
		{
			if(RL_UC_CheckSWInResponsesList(apduresponse, ExpectedSW, i))
				return true;
			else 
			{
				LoopControl = false;
				return false;
			}
		}
	}
}

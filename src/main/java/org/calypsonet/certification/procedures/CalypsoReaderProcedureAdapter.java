package org.calypsonet.certification.procedures;

import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.CardSelectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalypsoReaderProcedureAdapter extends ReaderProcedureAdapter implements CalypsoReaderProcedure {

  private static final Logger logger = LoggerFactory.getLogger(CalypsoReaderProcedureAdapter.class);

  private CardSelectionManager samSelectionManager;
  private CardSelectionResult samSelectionResult;

  public CalypsoReaderProcedureAdapter(CommonDto commonDto) {
    super(commonDto);
  }


  @Override
  public void RL_UR_SetupSamReader(String readerName) {

    commonDto.samReader = readerApiSpecific.setupSamReader(readerName);

    // Create a SAM selection manager.
    samSelectionManager = readerApiSpecific.createCardSelectionManager();
  }

  public void RL_UR_IsSamPresent() {
    // Check the SAM presence
    if (!commonDto.samReader.isCardPresent()) {
      throw new IllegalStateException("No SAM is present in the reader " + commonDto.samReader.getName());
    }
  }

  @Override
  public void RL_UR_SelectSam() {

    // Prepare the selection by adding the created SAM selection to the card selection
    // scenario.
    samSelectionManager.prepareSelection(commonDto.samSelection);

    // Actual card communication: run the selection scenario.
    samSelectionResult = samSelectionManager.processCardSelectionScenario(commonDto.samReader);

    // Check the selection result.
    if (samSelectionResult.getActiveSmartCard() == null) {
      throw new IllegalStateException(
              "The SAM selection failed.");
    }

    // Get the SmartCard resulting of the selection.
    commonDto.samSmartCard = samSelectionResult.getActiveSmartCard();
  }
}

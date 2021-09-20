package org.calypsonet.certification.procedures;

import org.calypsonet.certification.util.card.generic.GenericCardSelection;
import org.calypsonet.terminal.reader.CardReader;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.calypsonet.terminal.reader.selection.CardSelectionResult;
import org.calypsonet.terminal.reader.selection.spi.SmartCard;

public class ParameterDto {

    public SmartCard smartCard;
    public CardReader cardReader;
    public CardSelectionManager cardSelectionManager;
    public CardSelectionResult cardSelectionResult;
    public CardReader samReader;
    public CardSelectionManager samSelectionManager;
    public CardSelectionResult samSelectionResult;
}

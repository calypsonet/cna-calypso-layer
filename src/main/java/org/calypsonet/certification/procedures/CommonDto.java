package org.calypsonet.certification.procedures;

import org.calypsonet.terminal.reader.CardReader;
import org.calypsonet.terminal.reader.selection.spi.CardSelection;
import org.calypsonet.terminal.reader.selection.spi.SmartCard;

public class CommonDto {

    public CardReader cardReader;
    public CardSelection cardSelection;
    public SmartCard smartCard;

    public CardReader samReader;
    public CardSelection samSelection;
    public SmartCard samSmartCard;
}

package org.calypsonet.certification.spi;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.card.CalypsoCardSelection;
import org.calypsonet.terminal.calypso.sam.CalypsoSamSelection;
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting;
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager;
import org.calypsonet.terminal.reader.CardReader;

public interface CalypsoApiSpecific {

    void initializeCalypsoContext();

    void resetCalypsoContext();

    CalypsoSamSelection createSamSelection();

    CalypsoCardSelection createCardSelection();

    CardSecuritySetting createCardSecuritySetting();

    CardTransactionManager createCardTransaction(CardReader cardReader, CalypsoCard calypsoCard, CardSecuritySetting cardSecuritySetting);
}

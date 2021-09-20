package org.calypsonet.certification.spi;

import org.calypsonet.terminal.calypso.card.CalypsoCard;
import org.calypsonet.terminal.calypso.card.CalypsoCardSelection;
import org.calypsonet.terminal.calypso.sam.CalypsoSamSelection;
import org.calypsonet.terminal.calypso.transaction.CardSecuritySetting;
import org.calypsonet.terminal.calypso.transaction.CardTransactionManager;
import org.calypsonet.terminal.reader.CardReader;
import org.eclipse.keyple.card.calypso.CalypsoExtensionService;

public class CalypsoApiSpecificAdapter implements CalypsoApiSpecific {

    private CalypsoExtensionService calypsoExtensionService;

    public CalypsoApiSpecificAdapter() {
    }

    public void initializeCalypsoContext() {

        // Get the Calypso card extension service
        calypsoExtensionService = CalypsoExtensionService.getInstance();

    }

    public void resetCalypsoContext() {

    }

    public CalypsoSamSelection createSamSelection() {
        // Create a SAM selection manager.
        return calypsoExtensionService.createSamSelection();
    }

    public CalypsoCardSelection createCardSelection() {
        // Create a SAM selection manager.
        return calypsoExtensionService.createCardSelection();
    }


    public CardSecuritySetting createCardSecuritySetting() {
        return calypsoExtensionService.createCardSecuritySetting();
    }

    public CardTransactionManager createCardTransaction(CardReader cardReader, CalypsoCard calypsoCard, CardSecuritySetting cardSecuritySetting) {
        return calypsoExtensionService.createCardTransaction(cardReader, calypsoCard, cardSecuritySetting);
    }
}

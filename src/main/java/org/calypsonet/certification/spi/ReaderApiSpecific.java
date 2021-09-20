package org.calypsonet.certification.spi;

import org.calypsonet.terminal.reader.CardReader;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;

public interface ReaderApiSpecific {

    void initializeReaderContext();

    void resetReaderContext();

    CardReader setupCardReader(String readerName, boolean isContactless, String cardProtocol);

    CardReader setupSamReader(String readerName);

    CardSelectionManager createCardSelectionManager();
}

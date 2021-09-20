package org.calypsonet.certification.spi;

import org.calypsonet.terminal.reader.CardReader;
import org.calypsonet.terminal.reader.selection.CardSelectionManager;
import org.eclipse.keyple.core.common.KeyplePluginExtensionFactory;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.SmartCardService;
import org.eclipse.keyple.core.service.SmartCardServiceProvider;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.core.util.protocol.ContactCardCommonProtocol;
import org.eclipse.keyple.core.util.protocol.ContactlessCardCommonProtocol;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactoryBuilder;
import org.eclipse.keyple.plugin.pcsc.PcscReader;
import org.eclipse.keyple.plugin.stub.StubPluginFactoryBuilder;
import org.eclipse.keyple.plugin.stub.StubSmartCard;

public class ReaderApiSpecificAdapter implements ReaderApiSpecific {

    private SmartCardService smartCardService;
    // Plugin name [stub, pcsc]
    private static final String PLUGIN_NAME = "pcsc";
    private Plugin plugin;

    public ReaderApiSpecificAdapter() {
    }

    public void initializeReaderContext() {
        // Get the instance of the SmartCardService (singleton pattern)
        smartCardService = SmartCardServiceProvider.getService();

        // Register the first plugin
        KeyplePluginExtensionFactory pluginExtensionFactory;
        if ("stub".equals(PLUGIN_NAME)) {
            pluginExtensionFactory = StubPluginFactoryBuilder.builder()
                    .withStubReader("stubCardReader", true, getStubCard())
                    .withStubReader("stubSamReader", false, getStubSam())
                    .build();
        } else if ("pcsc".equals(PLUGIN_NAME)) {
            pluginExtensionFactory = PcscPluginFactoryBuilder.builder().build();
        } else {
            pluginExtensionFactory = PcscPluginFactoryBuilder.builder().build();
            // throw new IllegalStateException("Bad plugin name : " + pluginName);
        }
        plugin = smartCardService.registerPlugin(pluginExtensionFactory);
    }

    public void resetReaderContext() {
        // Unregister plugins
        smartCardService.unregisterPlugin(plugin.getName());
    }

    public CardReader setupCardReader(String readerName, boolean isContactless, String cardProtocol) {

        // Prepare PO reader
        Reader cardReader = plugin.getReader(readerName);

        if (cardReader == null) {
            throw new IllegalStateException("Card reader " + readerName + " not found.");
        }

        if (isContactless) {
            // Get and configure a contactless reader
            if (plugin.getName().equals("PcscPlugin")) {
                cardReader.getExtension(PcscReader.class)
                        .setContactless(true)
                        .setSharingMode(PcscReader.SharingMode.SHARED)
                        .setIsoProtocol(PcscReader.IsoProtocol.T1);
            }
        } else {
            // Get and configure a contact reader
            if (plugin.getName().equals("PcscPlugin")) {
                cardReader.getExtension(PcscReader.class)
                        .setContactless(false)
                        .setSharingMode(PcscReader.SharingMode.SHARED)
                        .setIsoProtocol(PcscReader.IsoProtocol.T0);
            }
        }

        return cardReader;
    }

    public CardReader setupSamReader(String readerName) {

        // Prepare SAM reader
        Reader samReader = plugin.getReader(readerName);

        if (plugin.getName().equals("PcscPlugin")) {
            samReader.getExtension(PcscReader.class)
                    .setContactless(false)
                    .setIsoProtocol(PcscReader.IsoProtocol.T0);
        }
        return samReader;
    }

    public CardSelectionManager createCardSelectionManager() {
        // Create a card selection manager.
        return smartCardService.createCardSelectionManager();
    }


    public StubSmartCard getStubCard() {
        return StubSmartCard.builder()
                .withPowerOnData(ByteArrayUtil.fromHex("3B8880010000000000718100F9"))
                .withProtocol(ContactlessCardCommonProtocol.ISO_14443_4.name())
                /* Select Application */
                .withSimulatedCommand("00A4040005AABBCCDDEE00", "6A82")
                /* Select Application */
                .withSimulatedCommand(
                        "00A4040009315449432E4943413100",
                        "6F238409315449432E49434131A516BF0C13C7080000000062010706530708C027052002139000")
                /* Read Records - EnvironmentAndHolder (SFI=07)) */
                .withSimulatedCommand(
                        "00B2013C00", "24B92848080000131A50001200000000000000000000000000000000009000")
                .withSimulatedCommand("00B2013C1D", "24B92848080000131A50001200000000000000000000000000000000009000")
                /* Read Records - EventLog (SFI=08, recnbr=1)) */
                .withSimulatedCommand("00B2014400", "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000")
                /* Open Secure Session V3.1 */
                .withSimulatedCommand(
                        "008A030104C1C2C3C400","03049098003079009000\n")
                /* Open Secure Session V3.1 */
                .withSimulatedCommand(
                        "008A0B4104C1C2C3C400",
                        "030490980030791D01112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000")
                /* Open Secure Session V3.1 */
                .withSimulatedCommand(
                        "008A0B3904C1C2C3C400",
                        "0308306C00307E1D24B928480800000606F0001200000000000000000000000000000000009000")
                /* Open Secure Session Compatibility mode */
                .withSimulatedCommand("008A8100040102030405", "0308306C009000")
                /* Read Records */
                .withSimulatedCommand(
                        "00B2014400", "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000")
                /* Read Records */
                .withSimulatedCommand(
                        "00B201F400", "00000000000000000000000000000000000000000000000000000000009000")
                /* Read Records */
                .withSimulatedCommand(
                        "00B2014C00", "00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000")
                /* Read Records */
                .withSimulatedCommand(
                        "00B2014D00",
                        "011D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF021D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF031D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF041D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000")
                /* Append Record */
                .withSimulatedCommand(
                        "00E200401D00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC", "9000")
                /* Append Record */
                .withSimulatedCommand(
                        "00E200401D01112233445566778899AABBCCDDEEFF00112233445566778899AABBCC", "9000")
                /* Close Secure Session */
                /* no ratification asked */
                .withSimulatedCommand("008E0000040506070800", "010203049000")
                /* ratification asked */
                .withSimulatedCommand("008E8000040506070800", "010203049000")
                /* Ratification */
                .withSimulatedCommand("00B2000000", "6B00")
                .build();
    }

    public StubSmartCard getStubSam() {
        return StubSmartCard.builder()
                .withPowerOnData(ByteArrayUtil.fromHex("3B3F9600805A0080C120000012345678829000"))
                .withProtocol(ContactCardCommonProtocol.ISO_7816_3.name())
                /* Select Application */
                /* Select Diversifier */
                .withSimulatedCommand("80140000080000000011223344", "9000")
                .withSimulatedCommand("80140000080000000062010706", "9000")
                /* Get Challenge */
                .withSimulatedCommand("8084000004", "C1C2C3C49000")
                /* Digest Init */
                .withSimulatedCommand(
                        "808A00FF27307E0308306C00307E1D24B928480800000606F000120000000000000000000000000000000000",
                        "9000")
                /* Digest Init */
                .withSimulatedCommand(
                        "808A00FF273079030490980030791D01112233445566778899AABBCCDDEEFF00112233445566778899AABBCC",
                        "9000")
                /* Digest Update */
                .withSimulatedCommand("808C00000500B2014400", "9000")
                /* Digest Update */
                .withSimulatedCommand(
                        "808C00001F00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC9000", "9000")
                /* Digest Update */
                .withSimulatedCommand("808C00000500B201F400", "9000")
                /* Digest Update */
                .withSimulatedCommand(
                        "808C00001F00000000000000000000000000000000000000000000000000000000009000", "9000")
                /* Digest Update */
                .withSimulatedCommand("808C00000500B2014C00", "9000")
                /* Digest Update */
                .withSimulatedCommand(
                        "808C00001F00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000", "9000")
                /* Digest Update */
                .withSimulatedCommand(
                        "808C00002200E200401D00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC", "9000")
                /* Digest Update */
                .withSimulatedCommand("808C0000029000", "9000")
                /* Digest Update */
                .withSimulatedCommand("808C00000500B2014D00", "9000")
                /* Digest Update */
                .withSimulatedCommand(
                        "808C00007E011D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF021D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF031D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF041D00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF9000",
                        "9000")
                /* Digest Update */
                .withSimulatedCommand(
                        "808C00002200E200401D01112233445566778899AABBCCDDEEFF00112233445566778899AABBCC", "9000")
                /* Digest Close */
                .withSimulatedCommand("808E000004", "050607089000")
                /* Digest Authenticate */
                .withSimulatedCommand("808200000401020304", "9000")
                /* Digest Authenticate */
                .withSimulatedCommand("80BE00A000",
                        "98052797D44027B5B800000000000000000000FFF560000000000000000000000000000002A0AEC10ABEFAFF408000009000")
                .build();
    }
}

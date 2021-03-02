package org.calypsonet.certification.calypso;

import org.calypsonet.certification.procedures.Procedure;
import org.calypsonet.certification.procedures.ProcedureFactory;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class CL121Test {

  private static final Logger logger = LoggerFactory.getLogger(CL121Test.class);
  private static Procedure procedure;
  private static String pluginName;
  private static String poReaderName;
  private static String poReaderType;
  private static boolean isPoReaderContactless;
  private static String poProtocol1;
  private static String poDfName1;
  private static String poSerialNumber1;
  private static String poStartupInfo1;

  /**
   * Executed once before running all tests of this class.
   *
   * @throws Exception
   */
  @BeforeClass
  public static void beforeClass() throws Exception {

    // Get procedure adapter
    procedure = ProcedureFactory.getProcedure();

    // Configuration parameters
    pluginName = ConfigProperties.getValue(ConfigProperties.Key.PLUGIN_NAME);
    poReaderName = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_1_NAME);
    poReaderType = ConfigProperties.getValue(ConfigProperties.Key.PO_READER_1_TYPE);
    poProtocol1 = ConfigProperties.getValue(ConfigProperties.Key.PO_1_PROTOCOL);
    poDfName1 = ConfigProperties.getValue(ConfigProperties.Key.PO_1_DFNAME);
    poSerialNumber1 = ConfigProperties.getValue(ConfigProperties.Key.PO_1_SERIALNUMBER);
    poStartupInfo1 = ConfigProperties.getValue(ConfigProperties.Key.PO_1_STARTUPINFO);

    if (ConfigProperties.READER_TYPE_CONTACTLESS.equalsIgnoreCase(poReaderType)) {
      isPoReaderContactless = true;
    } else if (ConfigProperties.READER_TYPE_CONTACT.equalsIgnoreCase(poReaderType)) {
      isPoReaderContactless = false;
    } else {
      throw new IllegalStateException("Unknown reader type " + poReaderType);
    }
  }

  /**
   * Executed once before each individual test of this class.
   *
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {

    logger.info("PRE CONDITIONS");
    logger.info("Initialize smart card service / register the Plugin");

    procedure.initializeContext(pluginName);

    // Prepare PO reader
    procedure.setupPoReader(poReaderName, isPoReaderContactless, poProtocol1);
  }

  /**
   * Executed once after each individual test of this class.
   *
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {
    procedure.resetContext();
  }

  /**
   * Executed once after running all tests of this class.
   *
   * @throws Exception
   */
  @AfterClass
  public static void afterClass() throws Exception {

  }

  @Test
  public void CL_121() {
    // Display test infos
    logger.info(
        "Ensure that the Calypso Layer allow additionnal TLV data.");

    // Data_80: {0x80, 0x01, 0x53}
    // Data_A0:{0xA0, 0x08, 0x8C, 0x02, 0x55, 0x55, 0x53, 0x02, 0xAA, 0xAA}
    // FCI: {0x6F, 0xXX, 0x84, poDfName1_Length, poDfName1, 0xA5, 0x16, 0xBF, 0x0C,  0x13, 0xC7, 0x08, poSerialNumber1, 0x53, 0x07, poStartupInfo1}

    // Selection application response with additional data
    // Data emulated : FCI + Data_80
    procedure.selectPo(poDfName1);

    assertThat(procedure.getPoDfName()).isEqualToIgnoringCase(poDfName1);
    assertThat(procedure.getPoApplicationSerialNumber()).isEqualToIgnoringCase(poSerialNumber1);
    assertThat(procedure.getPoStartupInfo()).isEqualToIgnoringCase(poStartupInfo1);

    // Selection application response with additional data
    // Data emulated : Data_80 + FCI
    procedure.selectPo(poDfName1);

    assertThat(procedure.getPoDfName()).isEqualToIgnoringCase(poDfName1);
    assertThat(procedure.getPoApplicationSerialNumber()).isEqualToIgnoringCase(poSerialNumber1);
    assertThat(procedure.getPoStartupInfo()).isEqualToIgnoringCase(poStartupInfo1);

    // Selection application response with additional data
    // Data emulated : Data_A0 + FCI
    procedure.selectPo(poDfName1);

    assertThat(procedure.getPoDfName()).isEqualToIgnoringCase(poDfName1);
    assertThat(procedure.getPoApplicationSerialNumber()).isEqualToIgnoringCase(poSerialNumber1);
    assertThat(procedure.getPoStartupInfo()).isEqualToIgnoringCase(poStartupInfo1);

    // Selection application response with additional data
    // Data emulated : Data_80 + FCI + Data_A0
    procedure.selectPo(poDfName1);

    assertThat(procedure.getPoDfName()).isEqualToIgnoringCase(poDfName1);
    assertThat(procedure.getPoApplicationSerialNumber()).isEqualToIgnoringCase(poSerialNumber1);
    assertThat(procedure.getPoStartupInfo()).isEqualToIgnoringCase(poStartupInfo1);
  }
}

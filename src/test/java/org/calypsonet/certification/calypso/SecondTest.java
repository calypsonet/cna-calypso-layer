package org.calypsonet.certification.calypso;

import org.assertj.core.api.Assertions;
import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.eclipse.keyple.plugin.stub.StubPluginFactory;
import org.junit.*;

public class SecondTest {

  private static PluginFactory pluginFactory;

  /**
   * Executed once before running all tests of this class.
   *
   * @throws Exception
   */
  @BeforeClass
  public static void beforeClass() throws Exception {
    String pluginName = ConfigProperties.getValue(ConfigProperties.Key.PLUGIN_NAME);
    if (ConfigProperties.PLUGIN_NAME_STUB.equals(pluginName)) {
      pluginFactory = new StubPluginFactory(pluginName, null, null);
    } else if (ConfigProperties.PLUGIN_NAME_PCSC.equals(pluginName)) {
      pluginFactory = new PcscPluginFactory(null, null);
    } else {
      pluginFactory = new PcscPluginFactory(null, null);
      // throw new IllegalStateException("Bad plugin name : " + pluginName);
    }
  }

  /**
   * Executed once before each individual test of this class.
   *
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {}

  /**
   * Executed once after each individual test of this class.
   *
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception {}

  /**
   * Executed once after running all tests of this class.
   *
   * @throws Exception
   */
  @AfterClass
  public static void afterClass() throws Exception {}

  @Test
  public void test3() {
    Assertions.assertThat(true).isTrue();
  }

  @Test
  public void test4() {
    Assertions.assertThat(true).isTrue();
  }
}

package org.calypsonet.certification.calypso;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ // tests list
  RL22Test.class,
  RL31Test.class,
  CL116Test.class,
  CL121Test.class,
  SecondTest.class
})
public class FirstTestSuite {}

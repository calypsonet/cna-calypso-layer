package org.calypsonet.certification.calypso;

import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Standalone application main class. */
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  /** Main program entry */
  public static void main(String[] args) {

    JUnitCore jUnitCore = new JUnitCore();
    jUnitCore.addListener(new ExecutionListener());

    // Run tests suites
    jUnitCore.run(FirstTestSuite.class);
  }
}

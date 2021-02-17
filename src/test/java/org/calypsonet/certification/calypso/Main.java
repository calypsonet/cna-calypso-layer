package org.calypsonet.certification.calypso;

import org.junit.runner.JUnitCore;

public class Main {

  public static void main(String[] args) {
    System.out.println("Hello world !");
    JUnitCore.main(FirstTestSuite.class.getName());
  }
}

package org.calypsonet.certification.calypso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Custom JUnit Runner Listener. */
public class ExecutionListener extends RunListener {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionListener.class);

  /** Called before any tests have been run. This may be called on an arbitrary thread. */
  @Override
  public void testRunStarted(Description description) {
    logger.debug("Number of tests to execute : " + description.testCount());
  }

  /** Called when all tests have finished. This may be called on an arbitrary thread. */
  @Override
  public void testRunFinished(Result result) {

    DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    logger.info("");
    logger.info("## TEST REPORT SUMMARY ##");
    logger.info("");
    logger.info("End execution datetime   : {}", df.format(new Date()));
    logger.info(
        "Result                   : {}", (result.wasSuccessful() ? "SUCCESSFUL" : "FAILED"));
    logger.info("Number of tests executed : {}", result.getRunCount());
    logger.info("Number of tests failed   : {}", result.getFailureCount());
    logger.info("Number of tests ignored  : {}", result.getIgnoreCount());
    logger.info("Execution time (millis)  : {}", result.getRunTime());
    if (result.getFailures() != null && !result.getFailures().isEmpty()) {
      logger.info("");
      logger.info("Failed tests : ");
      for (Failure failure : result.getFailures()) {
        logger.info("  Test name : {}", failure.getDescription().getMethodName());
        logger.info("      Cause : {}", failure.getMessage());
      }
    }
  }

  /** Called when an atomic test is about to be started. */
  @Override
  public void testStarted(Description description) {
    logger.debug("## Starting execution of test : {}", description.getMethodName());
  }

  /** Called when an atomic test has finished, whether the test succeeds or fails. */
  @Override
  public void testFinished(Description description) {
    logger.debug("## Finished execution of test : {}", description.getMethodName());
  }

  /**
   * Called when an atomic test fails, or when a listener throws an exception.
   *
   * <p>In the case of a failure of an atomic test, this method will be called with the same
   * Description passed to testStarted(Description), from the same thread that called
   * testStarted(Description).
   *
   * <p>In the case of a listener throwing an exception, this will be called with a Description of
   * Description.TEST_MECHANISM, and may be called on an arbitrary thread.
   */
  @Override
  public void testFailure(Failure failure) {
    logger.error("### Execution of test failed : {}", failure.getMessage());
    logger.error("### Execution trace : {}", failure.getTrace());
  }

  /** Called when an atomic test flags that it assumes a condition that is false. */
  @Override
  public void testAssumptionFailure(Failure failure) {
    logger.error("### Execution of test failed : {}", failure.getMessage());
    logger.error("### Execution trace : {}", failure.getTrace());
  }

  /**
   * Called when a test will not be run, generally because a test method is annotated with Ignore.
   */
  @Override
  public void testIgnored(Description description) {
    logger.warn("## Execution of test ignored : {}", description.getMethodName());
  }
}

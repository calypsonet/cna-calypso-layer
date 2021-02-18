package org.calypsonet.certification.calypso;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import junit.framework.Test;
import junit.framework.TestResult;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  /**
   * Main program entry.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    ConfigProperties.loadProperties();

    final File reportDir = new File(".");

    logger.info(
        "Execute first test suite using plugin {}",
        ConfigProperties.getValue(ConfigProperties.Key.PLUGIN_NAME));

    // JUnitCore.main(FirstTestSuite.class.getName());
    JUnitCore jUnitCore = new JUnitCore();
    jUnitCore.addListener(new TextListener(System.out));
    jUnitCore.addListener(
        new JUnitResultFormatterAsRunListener(new XMLJUnitResultFormatter()) {
          @Override
          public void testStarted(Description description) throws Exception {
            formatter.setOutput(
                new FileOutputStream(
                    new File(reportDir, "TEST-" + description.getDisplayName() + ".xml")));
            super.testStarted(description);
          }
          /*
          @Override
          public void testSuiteStarted(Description description) throws Exception {
            formatter.setOutput(new FileOutputStream(new File(reportDir,"TEST-"+description.getDisplayName()+".xml")));
            super.testSuiteStarted(description);
          }

          @Override
          public void testSuiteFinished(Description description) throws Exception {
            formatter.setOutput(new FileOutputStream(new File(reportDir,"TEST-"+description.getDisplayName()+".xml")));
            super.testSuiteFinished(description);
          }*/
        });

    // jUnitCore.addListener(new TextListener(System.out));
    Result result = jUnitCore.run(FirstTestSuite.class);

    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    System.out.println(gson.toJson(result));
    /*
      String filePath = ".";
      String reportFileName = "myReport.htm";
      StringBuffer myContent =
          getResultContent(
              result,
              10); // Size represents number of AllEJBJunitTests class, suppose if you have 5 EJB
      // classes then size is 5.
      writeReportFile(filePath + "/" + reportFileName, myContent);
    */
  }

  private static StringBuffer getResultContent(Result result, int numberOfTestFiles) {
    int numberOfTest = result.getRunCount();
    int numberOfTestFail = result.getFailureCount();
    int numberOfTestIgnore = result.getIgnoreCount();
    int numberOfTestSuccess = numberOfTest - numberOfTestFail - numberOfTestIgnore;
    int successPercent = (numberOfTest != 0) ? numberOfTestSuccess * 100 / numberOfTest : 0;
    double time = result.getRunTime();
    StringBuffer myContent =
        new StringBuffer(
            "<h1>Junitee Test Report</h1><h2>Result</h2><table border=\"1\"><tr><th>Test Files</th><th>Tests</th><th>Success</th>");
    if ((numberOfTestFail > 0) || (numberOfTestIgnore > 0)) {
      myContent.append("<th>Failure</th><th>Ignore</th>");
    }

    myContent.append("<th>Test Time (seconds)</th></tr><tr");
    if ((numberOfTestFail > 0) || (numberOfTestIgnore > 0)) {
      myContent.append(" style=\"color:red\" ");
    }
    myContent.append("><td>");
    myContent.append(numberOfTestFiles);
    myContent.append("</td><td>");
    myContent.append(numberOfTest);
    myContent.append("</td><td>");
    myContent.append(successPercent);
    myContent.append("%</td><td>");
    if ((numberOfTestFail > 0) || (numberOfTestIgnore > 0)) {
      myContent.append(numberOfTestFail);
      myContent.append("</td><td>");
      myContent.append(numberOfTestIgnore);
      myContent.append("</td><td>");
    }

    myContent.append(Double.valueOf(time / 1000.0D));
    myContent.append("</td></tr></table>");
    return myContent;
  }

  private static void writeReportFile(String fileName, StringBuffer reportContent) {
    FileWriter myFileWriter = null;
    try {
      myFileWriter = new FileWriter(fileName);
      myFileWriter.write(reportContent.toString());
    } catch (IOException e) {

    } finally {
      if (myFileWriter != null) {
        try {
          myFileWriter.close();
        } catch (IOException e) {

        }
      }
    }
  }

  /**
   * Adopts {@link JUnitResultFormatter} into {@link RunListener}, and also captures stdout/stderr
   * by intercepting the likes of {@link System#out}.
   *
   * <p>Because Ant JUnit formatter uses one stderr/stdout per one test suite, we capture each test
   * case into a separate report file.
   */
  public static class JUnitResultFormatterAsRunListener extends RunListener {
    protected final JUnitResultFormatter formatter;
    private ByteArrayOutputStream stdout, stderr;
    private PrintStream oldStdout, oldStderr;
    private int problem;
    private long startTime;

    private JUnitResultFormatterAsRunListener(JUnitResultFormatter formatter) {
      this.formatter = formatter;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {}

    @Override
    public void testRunFinished(Result result) throws Exception {}

    @Override
    public void testStarted(Description description) throws Exception {
      formatter.startTestSuite(new JUnitTest(description.getDisplayName()));
      formatter.startTest(new DescriptionAsTest(description));
      problem = 0;
      startTime = System.currentTimeMillis();

      this.oldStdout = System.out;
      this.oldStderr = System.err;
      System.setOut(new PrintStream(stdout = new ByteArrayOutputStream()));
      System.setErr(new PrintStream(stderr = new ByteArrayOutputStream()));
    }

    @Override
    public void testFinished(Description description) throws Exception {
      System.out.flush();
      System.err.flush();
      System.setOut(oldStdout);
      System.setErr(oldStderr);

      formatter.setSystemOutput(stdout.toString());
      formatter.setSystemError(stderr.toString());
      formatter.endTest(new DescriptionAsTest(description));

      JUnitTest suite = new JUnitTest(description.getDisplayName());
      suite.setCounts(1, problem, 0);
      suite.setRunTime(System.currentTimeMillis() - startTime);
      formatter.endTestSuite(suite);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
      testAssumptionFailure(failure);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
      problem++;
      formatter.addError(new DescriptionAsTest(failure.getDescription()), failure.getException());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
      super.testIgnored(description);
    }
  }

  /** Wraps {@link Description} into {@link Test} enough to fake {@link JUnitResultFormatter}. */
  public static class DescriptionAsTest implements Test {
    private final Description description;

    public DescriptionAsTest(Description description) {
      this.description = description;
    }

    public int countTestCases() {
      return 1;
    }

    public void run(TestResult result) {
      throw new UnsupportedOperationException();
    }

    /** {@link JUnitResultFormatter} determines the test name by reflection. */
    public String getName() {
      return description.getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      DescriptionAsTest that = (DescriptionAsTest) o;

      if (!description.equals(that.description)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return description.hashCode();
    }
  }
}

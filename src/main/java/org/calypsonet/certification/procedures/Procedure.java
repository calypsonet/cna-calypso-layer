package org.calypsonet.certification.procedures;

public interface Procedure {

  void initializeContext(String... pluginsNames);

  void resetContext();

  void setupPoReader(String readerName, boolean isContactless, String cardProtocol);

  void setupPoSecuritySettings(String readerName, String samRevision);

  void selectPo(String aid);

  void initializePoTransaction();

  void initializeSecurePoTransaction();

  void prepareReadRecord(int sfi, int recordNumber);

  void processOpening(SessionAccessLevel sessionAccessLevel);

  void prepareReleaseChannel();

  void processClosing();

  String getPoDfName();

  String getPoApplicationSerialNumber();

  String getPoStartupInfo();

  void activateSingleObservation(String aid);

  void waitMilliSeconds(int delay);

  void waitForCardInsertion();

  void waitForCardRemoval();

  void sendAPDU(String apdu, boolean case4);

  boolean isApduSuccessful();
}

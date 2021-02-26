package org.calypsonet.certification;

public interface Procedure {

  void initializeContext(String... pluginsNames);

  void resetContext();

  void setupPoReader(String readerName, boolean isContactless, String cardProtocol);

  void setupPoSecuritySettings(String readerName, String samRevision);

  void selectPo(String aid);

  void initializeNewTransaction();

  void prepareReadRecord(int sfi, int recordNumber);

  void processOpening(SessionAccessLevel sessionAccessLevel);

  void prepareReleaseChannel();

  void processClosing();

  String getPoDfName();
}

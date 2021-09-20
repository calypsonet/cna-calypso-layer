package org.calypsonet.certification.procedures;

public interface CalypsoProcedure {

  void CL_UT_InitializeContext();

  void CL_UT_ResetContext();

  void CL_UT_SetupCardSecuritySetting();

  void CL_UT_PrepareCardSelection(String aid);

  void CL_UT_SelectCard();

  void CL_UT_InitializeCardTransactionManager();

  void CL_UT_PrepareReadRecord(int sfi, int recordNumber);

  void CL_UT_ProcessOpening(SessionAccessLevel sessionAccessLevel);

  void CL_UT_PrepareReleaseCardChannel();

  void CL_UT_ProcessClosing();

  String CL_UT_GetCardDfName();

  String CL_UT_GetCardApplicationSerialNumber();

  String CL_UT_GetCardStartupInfo();

  boolean CL_UT_IsPkiModeSupported();

  boolean CL_UT_IsExtendedModeSupported();

}

package org.calypsonet.certification.procedures;

import org.calypsonet.terminal.calypso.transaction.SvAction;
import org.calypsonet.terminal.calypso.transaction.SvOperation;

public interface CalypsoProcedure {

  void CL_UT_InitializeContext();

  void CL_UT_ResetContext();

  void CL_UT_CreateSamSelection();

  void CL_UT_SetSam();

  void CL_UT_CreateCardSelection(String aid);

  void CL_UT_SetCard();

  void CL_UT_InitializeCardTransactionManager();



  void CL_UT_ProcessOpening(SessionAccessLevel sessionAccessLevel);

  void CL_UT_PrepareReleaseCardChannel();

  void CL_UT_ProcessClosing();

  String CL_UT_GetApplicationSerialNumber();

  String CL_UT_GetCardDfName();

  String CL_UT_GetCardApplicationSerialNumber();

  String CL_UT_GetCardStartupInfo();

  boolean CL_UT_IsPkiModeSupported();

  boolean CL_UT_IsExtendedModeSupported();

  void CL_UT_Prepare_SVGet(SvOperation OperationMode, SvAction ActionMode);
  void CL_UT_SVGet(SvOperation OperationMode, SvAction ActionMode);

  void CL_UT_Prepare_SVReloadWithAmountOnly(int Amount);
  void CL_UT_SVReloadWithAmountOnly(int Amount);

  void CL_UT_Prepare_SVReload(int Amount, byte[] Date, byte[] Time, byte[] FreeData);
  void CL_UT_SVReload(int Amount, byte[] Date, byte[] Time, byte[] FreeData);

  void CL_UT_Prepare_SVDebitWithAmountOnly(int Amount);
  void CL_UT_SVDebitWithAmountOnly(int Amount);

  void CL_UT_Prepare_SVDebit(int Amount, byte[] Date, byte[] Time);
  void CL_UT_SVDebit(int Amount, byte[] Date, byte[] Time);

  void CL_UT_PrepareReadRecord(byte sfi, int recordNumber);

  boolean CL_UT_IsPreviousSessionRatified();
  boolean CL_UT_IsRatificationOnDeselectFeatureSupported();

  void CL_UT_EnableRatificationMechanism();

  void CL_UT_Prepare_UpdateRecord(byte sfi, int recordNumber, byte[] recordData);
  void CL_UT_Prepare_AppendRecord(byte sfi, byte[] recordData);
  void CL_UT_ProcessCardCommands();
  void CL_UT_EnableMultipleSession();

  // Abort Session
  void CL_UT_ProcessCancel();

  // For transaction audit
  void CL_UT_EnableTransactionAudit();
  String CL_UT_GetTransactionAuditData();

  int CL_UT_GetSVBalance();

  void CL_UT_AuthorizeSVNegativeBalance();

  void CL_UT_SelectFileByLID(byte[] LID);

  String CL_UT_GetSAMProductType();
  String CL_UT_GetSAMSerialNumber();
  String CL_UT_GetSAMPlatform();
  String CL_UT_GetSAMApplicationType();
  String CL_UT_GetSAMApplicationSubType();
  String CL_UT_GetSAMSoftwareIssuer();
  String CL_UT_GetSAMSoftwareVersion();
  String CL_UT_GetSAMSoftwareRevision();

}

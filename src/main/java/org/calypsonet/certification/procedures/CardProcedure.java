package org.calypsonet.certification.procedures;

import java.util.List;

public interface CardProcedure {

  void RL_UC_InitializeContext();

  void RL_UC_ResetContext();

  void RL_UC_CreateCardSelection();

  void RL_UC_CreateCardSelection(String aid);

  void RL_UC_InitializeGenericCardTransactionManager();

  void RL_UC_PrepareApdu(String apduCommand);

  void RL_UC_PrepareApdu(byte[] apduCommand);

  void RL_UC_PrepareApdu(byte cla, byte ins, byte p1, byte p2, byte[] dataIn, Byte le);

  void RL_UC_PrepareReleaseChannel();

  void RL_UC_ProcessApdusToByteArrays();

  List<String> RL_UC_ProcessApdusToHexStrings();

  boolean RL_UC_CheckLastSW(List<String> apduresponse, String expectedstatusword);

  void RL_UC_CreateCardSelectionWithOccurrence(String aid, String occurrence);


}

package org.calypsonet.certification.procedures;

import org.calypsonet.terminal.reader.ConfigurableCardReader;

public interface ReaderProcedure {

  void RL_UR_InitializeContext();

  void RL_UR_ResetContext();

  void RL_UR_SetupCardReader(String readerName, boolean isContactless, String cardProtocol);

  boolean RL_UR_IsCardPresent();

  void RL_UR_SelectCard();

  void RL_UR_ActivateSingleObservation();

  void RL_UR_WaitMilliSeconds(int delay);

  void RL_UR_WaitForCardInsertion();

  void RL_UR_WaitForCardRemoval();

  void RL_UR_DeActivateSingleObservation();

  void RL_UR_ActivateProtocol(String ReaderProtocol, String CardProtocol);
  void RL_UR_DeActivateProtocol(String CurrentProtocol);
  String RL_UR_GetReaderName();
  boolean RL_UR_IsContactless();

  String RL_UR_GetPowerOnData();

  String RL_UR_SelectApplicationResponse();

  void PrepareReleaseChannel();

  void RL_UR_CardSelection();

  void RL_UR_ActivateRemovalProcedure(String RemovalMode);
  
  void RL_UR_APDUForRemovalProcedure(byte[] apduCommand);

}

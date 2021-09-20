package org.calypsonet.certification.procedures;

public interface ReaderProcedure {

  void RL_UR_InitializeContext();

  void RL_UR_ResetContext();

  void RL_UR_SetupCardReader(String readerName, boolean isContactless, String cardProtocol);

  void RL_UR_IsCardPresent();

  void RL_UR_SelectCard();

  void RL_UR_ActivateSingleObservation();

  void RL_UR_WaitMilliSeconds(int delay);

  void RL_UR_WaitForCardInsertion();

  void RL_UR_WaitForCardRemoval();
}

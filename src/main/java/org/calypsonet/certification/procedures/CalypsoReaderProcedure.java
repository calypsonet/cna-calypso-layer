package org.calypsonet.certification.procedures;

public interface CalypsoReaderProcedure extends ReaderProcedure {

  void RL_UR_SetupSamReader(String readerName);

  void RL_UR_IsSamPresent();

  void RL_UR_SelectSam();
}

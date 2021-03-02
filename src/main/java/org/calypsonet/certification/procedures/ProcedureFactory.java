package org.calypsonet.certification.procedures;

public class ProcedureFactory {

  public static Procedure getProcedure()
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    Class<Procedure> procedureClass =
        (Class<Procedure>)
            Thread.currentThread()
                .getContextClassLoader()
                .loadClass("org.calypsonet.certification.procedures.ProcedureAdapter");
    return procedureClass.newInstance();
  }
}

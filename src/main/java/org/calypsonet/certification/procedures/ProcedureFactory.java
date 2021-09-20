package org.calypsonet.certification.procedures;

public class ProcedureFactory {

  public static CalypsoProcedure getProcedure()
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    Class<CalypsoProcedure> procedureClass =
        (Class<CalypsoProcedure>)
            Thread.currentThread()
                .getContextClassLoader()
                .loadClass("org.calypsonet.certification.procedures.CalypsoProcedureAdapter");
    return procedureClass.newInstance();
  }
}

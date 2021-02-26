package org.calypsonet.certification;

public class ProcedureFactory {

  public static Procedure getProcedure()
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    Class<Procedure> procedureClass =
        (Class<Procedure>)
            Thread.currentThread()
                .getContextClassLoader()
                .loadClass("org.calypsonet.certification.ProcedureAdapter");
    return procedureClass.newInstance();
  }
}

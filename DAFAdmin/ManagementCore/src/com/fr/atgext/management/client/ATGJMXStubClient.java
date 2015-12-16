package com.fr.atgext.management.client;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class ATGJMXStubClient extends GenericJMXStubClient {
  /**
   * 
   * @param pDomainName
   * @param pBeanPath
   * @return
   * @throws MalformedObjectNameException
   */
  public ObjectName getObjectName(String pDomainName, String pBeanPath) 
    throws MalformedObjectNameException
  {
    return new ObjectName(pDomainName + ":absoluteName=" + pBeanPath);
  }
}

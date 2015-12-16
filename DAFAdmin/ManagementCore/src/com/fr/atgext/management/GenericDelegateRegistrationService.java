package com.fr.atgext.management;

import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;

public abstract class GenericDelegateRegistrationService extends GenericService {
  MBeanServer mMbeanServer;
  String mApplicationName;
  String mServiceType;
  

  /**
   * Creates a MBean object name from a Nucleus absolute path
   * @param pAbsoluteName
   * @return
   * @throws Exception
   */
  public ObjectName createObjectName(String pAbsoluteName)
    throws Exception
  {
    ObjectName objName = null;
    String domain = getMbeanServer().getDefaultDomain();
    Hashtable table = new Hashtable();
    
//    StringBuffer buffer = new StringBuffer("ATG,");
//    buffer.append("application=");
//    if (!StringUtils.isBlank(getApplicationName())) {
//      buffer.append(getApplicationName());
//    } else {
//      buffer.append("Core");
//    }
//    
//    buffer.append(",type=");
//    
//    if (!StringUtils.isBlank(getServiceType())) {
//      buffer.append(getServiceType());
//    } else {
//      buffer.append("generic service");
//    }
//    
//    buffer.append(",name=");
//    buffer.append(pAbsoluteName);
//    
//    table.put("absoluteName", buffer.toString());
    table.put("absoluteName", pAbsoluteName);
    
    objName = new ObjectName(domain, table);
    return objName;
  }
  
  public MBeanServer getMbeanServer() {
    return mMbeanServer;
  }
  public void setMbeanServer(MBeanServer pMbeanServer) {
    mMbeanServer = pMbeanServer;
  }

  public String getApplicationName() {
    return mApplicationName;
  }

  public void setApplicationName(String pApplicationName) {
    mApplicationName = pApplicationName;
  }
  
  public String getServiceType() {
    return mServiceType;
  }
  public void setServiceType(String pServiceType) {
    mServiceType = pServiceType;
  }
}

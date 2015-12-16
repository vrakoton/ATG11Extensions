package com.fr.atgext.management.jdbc;

import java.util.Iterator;
import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.fr.atgext.management.GenericDelegateRegistrationService;

import atg.nucleus.GenericService;
import atg.nucleus.Nucleus;
import atg.nucleus.ServiceException;
import atg.service.jdbc.MonitoredDataSource;
import atg.service.jdbc.SwitchingDataSource;

public class DataSourceRegistrationService extends GenericDelegateRegistrationService {
  List<String> mDataSources;
  

  /**
   * 
   */
  public void doStartService ()
    throws ServiceException
  {
    super.doStartService();
    if (getDataSources() != null && !getDataSources().isEmpty()) {
      Iterator<String> it = getDataSources().iterator();
      String source = null;
      while (it.hasNext()) {
        source = it.next();
        addDataSourceMbean(Nucleus.getGlobalNucleus().resolveName(source));
      }
    }
  }
  
  /**
   * Add the data source to the Mbean Server
   * @param pDataSource
   */
  public void addDataSourceMbean (Object pDataSource) 
  {
    Object dataSourceMbean = null;
    ObjectName name = null;
    
    if (pDataSource == null)
      return;
    
    if (isLoggingDebug())
      logDebug("Data source is of type " + pDataSource.getClass().getName());
    
    // --- get the name of the Mbean
    try {
      if (pDataSource instanceof GenericService) {
        name = createObjectName(((GenericService)pDataSource).getAbsoluteName());
      }
    } catch (Exception e) {
      if (isLoggingError())
        logError("Unable to create MBean name ", e);
    }
    
    // --- deal with the different data source types
    if (pDataSource instanceof SwitchingDataSource) {
      dataSourceMbean = new SwitchingDataSourceDelegate((SwitchingDataSource)pDataSource);
    } else if (pDataSource instanceof MonitoredDataSource) {
      dataSourceMbean = new MonitoredDataSourceDelegate((MonitoredDataSource)pDataSource);
    }  else {
      if (isLoggingWarning()) {
        logWarning("Unsupported data source type " + pDataSource.getClass().getName() + ". " +
        		"If this is a 3rd party data source type, please address monitoring " +
        		"using the application server JMX monitoring tools directly.");
      }
      return;
    }
    
    
    // --- register the delegate under the nucleus service path
    if (name != null && dataSourceMbean != null && !getMbeanServer().isRegistered(name)) {
      try {
        getMbeanServer().registerMBean(dataSourceMbean, name);
      } catch (InstanceAlreadyExistsException iaee) {
        if (isLoggingError())
          logError("Duplicate Mbean name ", iaee);
      } catch (MBeanRegistrationException mre) {
        if (isLoggingError())
          logError("Unable to register the Mbean ", mre);
      } catch (NotCompliantMBeanException nce) {
        if (isLoggingError())
          logError("The Mbean is not compliant ", nce);
      }
    }
  }
  
  public List<String> getDataSources() {
    return mDataSources;
  }
  public void setDataSources(List<String> pDataSources) {
    mDataSources = pDataSources;
  }
}

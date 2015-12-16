/**
 * @author vrakoton@re-enter.fr
 * @version 1.0
 * 
 * Description :
 * 
 * This class is a delegate for data source objects in ATG
 * 
 */
package com.fr.atgext.management.jdbc;

import atg.nucleus.GenericService;
import atg.service.jdbc.MonitoredDataSource;

public class MonitoredDataSourceDelegate extends GenericService
    implements MonitoredDataSourceDelegateMBean {

  MonitoredDataSource mDataSource;
  
  public MonitoredDataSourceDelegate (MonitoredDataSource pDataSource) {
    mDataSource = pDataSource;
  }
  
  /**
   * Get the maximum resource number the pool can create
   */
  public int getMax() {
    if (getDataSource() != null) {
      return getDataSource().getMax();
    }
    return 0;
  }

  /**
   * Get the maximum resource number which can be let free. Any ;other resource which is beyond this number
   * are destroyed by the resource pool automatically.
   */
  public int getMaxFree() {
    if (getDataSource() != null) {
      return getDataSource().getMaxFree();
    }
    return 0;
  }

  /**
   * 
   */
  public int getMaxSimultaneousResourcesOut() {
    if (getDataSource() != null) {
      return getDataSource().getMaxSimultaneousResourcesOut();
    }
    return 0;
  }

  /**
   * Gets the minimum number of resources in the data source
   */
  public int getMin() {
    if (getDataSource() != null) {
      return getDataSource().getMin();
    }
    return 0;
  }

  /**
   * Invalidates all resources in the pool and forces ATG to renew them 
   */
  public void invalidateAllResources() {
    if (getDataSource() != null) {
      getDataSource().invalidateAllResources();
    } else {
      throw new RuntimeException("Can not invoke the invalidateAllResources() method, data source is null");
    }
  }
  
  /**
   * Prunes the free resources
   */
  public void pruneFreeResources() {
    if (getDataSource() != null) {
      getDataSource().pruneFreeResources();
    } else {
      throw new RuntimeException("Can not invoke the pruneFreeResources() method, data source is null");
    }
  }

  /**
   * Unblock the data source if it is blocked. Data sources can be blocked if the max ressources is reached and the
   * blocking boolean property is set to "true"
   */
  public void unblock() {
    if (getDataSource() != null) {
      getDataSource().unblock();
    } else {
      throw new RuntimeException("Can not invoke the unblock() method, data source is null");
    }
  }

  public MonitoredDataSource getDataSource() {
    return mDataSource;
  }
  public void setDataSource(MonitoredDataSource pDataSource) {
    mDataSource = pDataSource;
  }

}

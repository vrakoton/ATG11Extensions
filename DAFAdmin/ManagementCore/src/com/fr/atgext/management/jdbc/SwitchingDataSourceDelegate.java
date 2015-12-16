package com.fr.atgext.management.jdbc;

import atg.nucleus.GenericService;
import atg.service.jdbc.SwitchingDataSource;

public class SwitchingDataSourceDelegate extends GenericService implements SwitchingDataSourceDelegateMBean
{
  SwitchingDataSource mDataSource;
  
  /**
   * public constructor
   * @param pDataSource
   */
  public SwitchingDataSourceDelegate (SwitchingDataSource pDataSource) {
    mDataSource = pDataSource;
  }
  
  public void prepareSwitch() throws Exception
  {
    if (getDataSource() != null)
      getDataSource().prepareSwitch();
  }
  
  public void performSwitch() {
    if (getDataSource() != null)
      getDataSource().performSwitch();
  }
  
  /**
   * Gets the Nucleus path of the current live data source
   * @return the nucleus path of the used data source
   */
  public String getLiveDataStore() {
    if (getDataSource() != null) {
      GenericService datasource = (GenericService)getDataSource().getLiveDataStore();
      if (datasource != null) {
        return datasource.getAbsoluteName();
      }
    }
    return null;
  }
  
  /**
   * Gets the Nucleus path of the current staging data source
   * @return the nucleus path of the inactive data source
   */
  public String getStagingDataStore() {
    if (getDataSource() != null) {
      GenericService datasource =  (GenericService)getDataSource().getStagingDataStore();
      if (datasource != null) {
        return datasource.getAbsoluteName();
      }
    }
    return null;
  }

  public SwitchingDataSource getDataSource() {
    return mDataSource;
  }
  public void setDataSource(SwitchingDataSource pDataSource) {
    mDataSource = pDataSource;
  }
  
}

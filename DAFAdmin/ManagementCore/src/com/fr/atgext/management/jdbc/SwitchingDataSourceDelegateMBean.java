package com.fr.atgext.management.jdbc;

public interface SwitchingDataSourceDelegateMBean {
  public void prepareSwitch() throws Exception;
  public void performSwitch();
  public String getLiveDataStore();
  public String getStagingDataStore();
}

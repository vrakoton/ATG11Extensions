package com.fr.atgext.management.jdbc;

public interface MonitoredDataSourceDelegateMBean {
  // --- properties
  public int getMin();
  public int getMax();
  public int getMaxFree();
  public int getMaxSimultaneousResourcesOut();
  
  // --- methods
  public void pruneFreeResources();
  public void invalidateAllResources();
  public void unblock();
}

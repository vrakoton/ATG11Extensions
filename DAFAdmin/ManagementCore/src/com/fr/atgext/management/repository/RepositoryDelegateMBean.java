package com.fr.atgext.management.repository;

public interface RepositoryDelegateMBean {
  public String getRepositoryPath();
  public String [] getItemDescriptorNames();
  public String getStatisticsAsString();
  public void invalidateCaches();
}

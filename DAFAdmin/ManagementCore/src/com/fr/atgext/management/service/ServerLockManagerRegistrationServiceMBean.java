/**
 * @author vrakoton@re-enter.fr
 * @version 1.0
 * 
 * Description : mbean interface for the server lock manager MBEAN
 */
package com.fr.atgext.management.service;

public interface ServerLockManagerRegistrationServiceMBean {
  public boolean isOutOfMemoryDetected ();
  public boolean isBackupServer ();
  public int getLockEntryTableSize ();
  public int getLockOwnerTableSize ();
}

package com.fr.atgext.management.service;

import com.fr.atgext.management.GenericDelegateRegistrationService;

import atg.service.lockmanager.ServerLockManager;


public class ServerLockManagerRegistrationService extends GenericDelegateRegistrationService implements ServerLockManagerRegistrationServiceMBean {
  
  String mServerLockManagerPath = "/atg/dynamo/service/ServerLockManager";
  ServerLockManager mServerLockManager;
  long mPollingInterval = 10000;
  int mMaxPollIteration = 10;
  ServerLockManagerPollerThread mPoller;
  
  public void doStartService () {
    if (isLoggingDebug())
      logDebug("Starting the SLM poller");
    mPoller = new ServerLockManagerPollerThread(this);
    mPoller.start();
  }
  
  /**
   * returns wether the service is backup server or not
   * @return
   */
  public boolean isBackupServer () {
    if (getServerLockManager() != null) {
      return getServerLockManager().getIsBackupServer();
    }
    return true;
  }
  
  /**
   * returns whether an out of memory has been detected on the JVM or not
   * @return
   */
  public boolean isOutOfMemoryDetected () {
    if (getServerLockManager() != null) {
      return getServerLockManager().isOutOfMemoryDetected();
    }
    return false;
  }
  
  /**
   * Returns the size of the lock entry table
   * @return
   */
  public int getLockEntryTableSize () {
    if (getServerLockManager() != null) {
      return getServerLockManager().getLockEntryTableSize();
    }
    return 0;
  }
  
  /**
   * Returns the size of the lock owner table
   * @return
   */
  public int getLockOwnerTableSize () {
    if (getServerLockManager() != null) {
      return getServerLockManager().getLockOwnerTableSize();
    }
    return 0;
  }

  public String getServerLockManagerPath() {
    return mServerLockManagerPath;
  }
  public void setServerLockManagerPath(String pServerLockManagerPath) {
    mServerLockManagerPath = pServerLockManagerPath;
  }

  public ServerLockManager getServerLockManager() {
    return mServerLockManager;
  }
  public void setServerLockManager(ServerLockManager pServerLockManager) {
    mServerLockManager = pServerLockManager;
  }

  public long getPollingInterval() {
    return mPollingInterval;
  }
  public void setPollingInterval(long pPollingInterval) {
    mPollingInterval = pPollingInterval;
  }

  public int getMaxPollIteration() {
    return mMaxPollIteration;
  }

  public void setMaxPollIteration(int pMaxPollIteration) {
    mMaxPollIteration = pMaxPollIteration;
  }
}

package com.fr.atgext.management.service;

import javax.management.ObjectName;

import atg.nucleus.Nucleus;
import atg.service.lockmanager.ServerLockManager;

public class ServerLockManagerPollerThread extends Thread {
  ServerLockManagerRegistrationService mLauncher;
  
  public ServerLockManagerPollerThread(ServerLockManagerRegistrationService pLauncher) {
    mLauncher = pLauncher;
  }
  
  public boolean isLoggingDebug() {
    return mLauncher.isLoggingDebug();
  }
  public boolean isLoggingInfo() {
    return mLauncher.isLoggingInfo();
  }
  public boolean isLoggingWarning() {
    return mLauncher.isLoggingWarning();
  }
  public boolean isLoggingError() {
    return mLauncher.isLoggingError();
  }
  
  public void logInfo (String pMessage) {
    mLauncher.logInfo(pMessage);
  }
  public void logDebug (String pMessage) {
    mLauncher.logDebug(pMessage);
  }
  
  public void logError (String pMessage, Throwable pException) {
    mLauncher.logError(pMessage, pException);
  }
  
  /**
   * 
   */
  public void run() {
    boolean stopThread = false;
    int iteration = 1;
    while (!stopThread) {
      try {
        if (isLoggingDebug())
          logDebug("Running poller iteration " + iteration);
        
        ServerLockManager lockmanager = (ServerLockManager)Nucleus.getGlobalNucleus().resolveName(mLauncher.getServerLockManagerPath(), false);
        if (lockmanager != null) {
          if (isLoggingDebug())
            logDebug("Registering Server lock manager MBean in the registry");
          mLauncher.setServerLockManager(lockmanager);
          ObjectName objName = mLauncher.createObjectName(mLauncher.getServerLockManagerPath());
          if (!mLauncher.getMbeanServer().isRegistered(objName)) {
            mLauncher.getMbeanServer().registerMBean(mLauncher, objName);
          } else {
            if (isLoggingDebug())
              logDebug("Can not register repository with name " + mLauncher.getServerLockManagerPath() + ", another MBean is " +
                  "already registered under this name");
          }
          stopThread = true;
        } else {
          if (isLoggingDebug())
            logDebug("No server lock service detected on instance");
        }
      } catch (Exception exc) {
        if (isLoggingError())
          logError("Could not start MBean delegate ", exc);
      }
      try {
        iteration++;
        if (iteration > mLauncher.getMaxPollIteration()) {
          stopThread = true;
        }
        if (isLoggingDebug())
          logDebug("Poller thread sleeping for " + mLauncher.getPollingInterval() + " milliseconds");
        if (!stopThread)
          sleep(mLauncher.getPollingInterval());
      } catch (InterruptedException ie) {
        if (isLoggingError())
          logError("Could not sleep ", ie);
      }
    }
    if (isLoggingDebug()) {
      logDebug("SLM poller thread is exiting");
    }
  }
}

package com.fr.atgext.admin.logging;

import java.util.List;

import atg.nucleus.logging.LogListenerQueue;

public class AdminLogListenerQueue extends LogListenerQueue {
  boolean mOverrideLoggingQueue = true;
  List<String> mOverrideLogListenerList;
  
  public void doStartService()
  {
    super.doStartService();
    if (isOverrideLoggingQueue()) {
      LogListenerQueueUtil.overrideLogListeners(this, getOverrideLogListenerList());
    }
  }
  
  public boolean isOverrideLoggingQueue() {
    return mOverrideLoggingQueue;
  }
  public void setOverrideLoggingQueue(boolean pOverrideLoggingQueue) {
    mOverrideLoggingQueue = pOverrideLoggingQueue;
  }
  public List<String> getOverrideLogListenerList() {
    return mOverrideLogListenerList;
  }
  public void setOverrideLogListenerList(List<String> pOverrideLogListenerList) {
    mOverrideLogListenerList = pOverrideLogListenerList;
  }
}

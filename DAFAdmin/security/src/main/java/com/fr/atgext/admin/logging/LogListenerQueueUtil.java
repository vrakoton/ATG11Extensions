package com.fr.atgext.admin.logging;

import java.util.List;

import atg.nucleus.GenericService;
import atg.nucleus.Nucleus;
import atg.nucleus.logging.LogListener;

public class LogListenerQueueUtil {
  public static void overrideLogListeners(GenericService service, List<String> listenerPaths) {
    if (listenerPaths == null || listenerPaths.isEmpty()) {
      if (service.isLoggingWarning()) {
        service.logWarning("Can not override log listeners as no override value has been set");
      }
      return;
    }
    
    if (service.isLoggingInfo()) {
      service.logInfo("Overriding logging queue");
    }
    
    LogListener [] currentListeners = service.getLogListeners();
    if (currentListeners != null) {
      for (int i = currentListeners.length - 1; i >= 0; i--) {
        service.removeLogListener(currentListeners[i]);
      }
      if (service.isLoggingInfo()) {
        service.logInfo("Log listeners have been overriden");
      }
    }
    
    for (String path: listenerPaths) {
        LogListener l = (LogListener)Nucleus.getGlobalNucleus().resolveName(path);
        if (l != null) {
          service.addLogListener(l);
        } else {
          if (service.isLoggingError()) {
            service.vlogError("Could not resolve service with path {}", path);
          }
        }
        
    }
  }
}

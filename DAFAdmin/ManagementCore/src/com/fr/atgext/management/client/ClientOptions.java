/**
 * @author vrakoton@re-enter.fr
 * @version 1.0
 * 
 * Description : 
 * 
 * Client options for client call
 * 
 */
package com.fr.atgext.management.client;

import java.util.HashMap;
import java.util.Map;

public class ClientOptions {
  public static final String OPTION_DEBUG = "debug";
  public static final String OPTION_TRUE = "true";
  
  Map<String, String> props;
  
  public ClientOptions() {
  }
  
  /**
   * Sends an option value if found. Null otherwise.
   * @param pOptionName
   * @return
   */
  public String getOptionValue(String pOptionName) {
    if (!props.isEmpty() && !ClientUtils.isBlankString(pOptionName)) {
      return props.get(pOptionName);
    }
    return null;
  }
  
  /**
   * checks if there are options set or not
   * @return true if options were added to the object
   */
  public boolean isEmpty() {
    return (props == null || props.isEmpty());
  }
  
  
  
  /**
   * Adds an option to the object
   * @param pOptionName
   * @param pOptionValue
   */
  public void addOption (String pOptionName, String pOptionValue)  {
    if (ClientUtils.isBlankString(pOptionName)) {
      ClientUtils.printError("Option name can not be null or empty");
      return;
    }
    
    if (ClientUtils.isBlankString(pOptionValue)) {
      ClientUtils.printError("Option value can not be null or empty");
      return;
    }
    
    if (props == null) {
      props = new HashMap<String, String>();
    }
    
    props.put(pOptionName, pOptionValue);
  }
}

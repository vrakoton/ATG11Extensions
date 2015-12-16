/**
 * @author vrakoton@re-enter.fr
 * @version 1.0
 * 
 * Description : 
 * 
 * This is the actual JMX client which manages the connection, the attribute value printing
 */
package com.fr.atgext.management.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;



public class GenericJMXStubClient {
  static final String PRINT_SEPARATOR = ",";
  static final String PRINT_PROP_VALUE_SEPARATOR = ":";
  static final String PRINT_SUB_PROP_NAME_SEPARATOR = "";
  static final String PRINT_PROP_PREFIX_SEPARATOR ="";
  
  
  boolean mDebug = false;
  String mEndPointType;
  String mAdapterName;
  

  String mUser;
  String mPassword;
  String mServerUrl;
  
  
  public GenericJMXStubClient() {
    
  }
  
  /**
   * 
   * @param pOptions
   */
  void readOptions (ClientOptions pOptions) {
    if (pOptions == null || pOptions.isEmpty())
      return;
    
    String value = pOptions.getOptionValue(ClientOptions.OPTION_DEBUG);
    if (value != null && ClientOptions.OPTION_TRUE.equals(value)) {
      mDebug = true;
    }
  }
  
  /**
   * Tests if debug is enabled
   * @return
   */
  public boolean isLoggingDebug() {
    return mDebug;
  }
  
  
  /**
   * Adds a property and its value to the buffer to be printed
   * @param pBeanPath
   * @param pPropertyName
   * @param pPropValue
   * @param buffer
   */
  void addPropertyToBuffer(String pPrefix, String pBeanPath, String pPropertyName, Object pPropValue, StringBuffer buffer) {
    
    Object attrValue = pPropValue;
    if (pPropValue == null) {
      attrValue = "null";
    } else {
      if (attrValue instanceof CompositeDataSupport) {
        CompositeDataSupport compValue = (CompositeDataSupport)attrValue;
        CompositeType compType = compValue.getCompositeType();
        Set<String> valueNames = compType.keySet();
        if (valueNames != null && !valueNames.isEmpty()) {
          Iterator<String> valNameIt = valueNames.iterator();
          String subPropertyName = null;
          while (valNameIt.hasNext()) {
            subPropertyName = valNameIt.next().toString();
            buffer.append(pBeanPath);
            buffer.append(PRINT_SEPARATOR);
            if (!ClientUtils.isBlankString(pPrefix)) {
            	buffer.append(pPrefix);
            	if (!ClientUtils.isBlankString(PRINT_PROP_PREFIX_SEPARATOR))
            		buffer.append(PRINT_PROP_PREFIX_SEPARATOR);
            }
            buffer.append(pPropertyName);
            if (!ClientUtils.isBlankString(PRINT_SUB_PROP_NAME_SEPARATOR)) {
            	buffer.append(PRINT_SUB_PROP_NAME_SEPARATOR);
            }
            buffer.append(subPropertyName);
            buffer.append(PRINT_PROP_VALUE_SEPARATOR);
            buffer.append(compValue.get(subPropertyName));
            buffer.append("\n");
          }
        }
        
      } else {
        buffer.append(pBeanPath);
        buffer.append(PRINT_SEPARATOR);
        if (!ClientUtils.isBlankString(pPrefix)) {
        	buffer.append(pPrefix);
        	if (!ClientUtils.isBlankString(PRINT_PROP_PREFIX_SEPARATOR))
        		buffer.append(PRINT_PROP_PREFIX_SEPARATOR);
        }
        buffer.append(pPropertyName);
        buffer.append(PRINT_PROP_VALUE_SEPARATOR);
        buffer.append(attrValue);
      }
    }
    
    buffer.append("\n");
  }
  
  public ObjectName getObjectName(String pDomainName, String pBeanPath) 
  throws MalformedObjectNameException
  {
    return new ObjectName(pBeanPath);
  }
  
  /**
   * Returns the display name for the MBean for the output file. If a name override is found in the display names
   * map, we print that name, otherwise, we print the MBean path as usual 
   * @param pBeanPath
   * @param pBeanDisplayNames
   * @return
   */
  public String getBeanDisplayName(String pBeanPath, Map<String, String> pBeanDisplayNames) {
	  if (pBeanDisplayNames == null || pBeanDisplayNames.isEmpty()) {
		  return pBeanPath;
	  }
	  
	  String displayName = pBeanDisplayNames.get(pBeanPath);
	  if (!ClientUtils.isBlankString(displayName)) {
		  return displayName;
	  }
	  
	  return pBeanPath;
  }
  
  /**
   * Returns the property name to display in the XML result of the JMX call.
   * @param pBeanPath
   * @param pPropertyName
   * @param pPropertyDisplayNames
   * @return
   */
  public String getPropertyDisplayName (String pBeanPath, String pPropertyName, Map<String, String>pPropertyDisplayNames) {
  	if (pPropertyDisplayNames == null || pPropertyDisplayNames.isEmpty())
  		return pPropertyName;
  	String displayName = pPropertyDisplayNames.get(pBeanPath + JMXClient.PROPERTY_SEPARATOR + pPropertyName);
  	if (!ClientUtils.isBlankString(displayName)) {
  		return displayName;
  	} 
  	return pPropertyName;
  }
  
  /**
   * Prints the result of the mbean property queries
   * @param pDomainName
   * @param pBeanPath
   * @param pConn
   * @param mFilteredProperties
   */
  void printBeanProperties (String pDomainName, 
  		String pBeanPath, 
  		MBeanServerConnection pConn, 
  		Map<String, String> pBeanDisplayNames, 
  		Map<String, List<String>> pFilteredProperties, 
  		Map<String, String>pPropertyPrefixes,
  		Map<String, String>pPropertyDisplayNames) {
    StringBuffer buffer = new StringBuffer();
    
    try {
      ObjectName mbeanName = getObjectName(pDomainName, pBeanPath);
      ObjectInstance mbean = pConn.getObjectInstance(mbeanName);
      
      String displayName = getBeanDisplayName(pBeanPath, pBeanDisplayNames);
      String prefix = null;
      
      if (pPropertyPrefixes != null && !pPropertyPrefixes.isEmpty()) {
    	  prefix = pPropertyPrefixes.get(pBeanPath);
      }
      
      if (mbean != null) {
        Object attrValue = null;
        MBeanInfo info = pConn.getMBeanInfo(mbeanName);
        List <String> propertyList = pFilteredProperties.get(pBeanPath);
        
        String propName = null;
        String propDisplayName = null;
        
        if (propertyList != null && !propertyList.isEmpty()) {
          // --- we should print a subset of the properties only
          Iterator<String> propIt = propertyList.iterator();
          while (propIt.hasNext()) {
            propName = propIt.next();
            propDisplayName = getPropertyDisplayName(pBeanPath, propName, pPropertyDisplayNames);
           	attrValue = pConn.getAttribute(mbeanName, propName);
            addPropertyToBuffer(prefix, displayName, propDisplayName, attrValue, buffer);
          }
        } else {
          // --- no restriction on property list so we print everything 
          MBeanAttributeInfo[] attributes = info.getAttributes();
          if (attributes != null) {
            for (int i = 0; i < attributes.length; i++) {
              propName = attributes[i].getName();
              propDisplayName = getPropertyDisplayName(pBeanPath, attributes[i].getName(), pPropertyDisplayNames);
              attrValue = pConn.getAttribute(mbeanName, propName);
              addPropertyToBuffer(prefix, displayName, propDisplayName, attrValue, buffer);
            }
          }
        }
      }
     
    } catch (MalformedObjectNameException mfone) {
      ClientUtils.printError("An error occured during MBean query", mfone);
    } catch (IOException ioe) {
      ClientUtils.printError("An I/O error occured during MBean query", ioe);
    } catch (ReflectionException re) {
      ClientUtils.printError("An error occured during MBean search", re);
    } catch (IntrospectionException ie) {
      ClientUtils.printError("An error occured during MBean search", ie);
    } catch (InstanceNotFoundException infe) {
      ClientUtils.printError("An error occured during MBean search", infe);
    } catch (AttributeNotFoundException anfee) {
      ClientUtils.printError("Attribute search failed");
      throw new RuntimeException(anfee);
    } catch (MBeanException mbeae) {
      ClientUtils.printError("An error occured during MBean search", mbeae);
    }
    
    System.out.println(buffer.toString());
  }
  
  
  /**
   * returns a JMX connection object. This method can be overriden for specific connection
   * creation.
   * @return
   */
  @SuppressWarnings("rawtypes")
  public MBeanServerConnection getMbeanServerConnection () {
    try {
	    JMXServiceURL url;
	    url = new JMXServiceURL(getServerUrl());
	    Map env = null;
	    if (!ClientUtils.isBlankString(getUser())) {
	      env = new HashMap();
	      String [] credentials = new String [2];
	      credentials[0] = getUser();
	      credentials[1] = getPassword();
	      env.put(JMXConnector.CREDENTIALS, credentials);
	      env.put("jmx.remote.x.request.waiting.timeout", "3000");
	    }
	    JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
	    
	    return jmxc.getMBeanServerConnection();
    } catch (IOException ioe) {
      ioe.printStackTrace(System.err);
    }
    
    return null;
  }
  
  /**
   * Makes the call to the Mbean server
   */
  public void callMBeanServer (List<String> pBeanList, 
  		Map<String,String>pBeanDisplayNames, 
  		Map<String, List <String>> pFilteredProperties,
  		Map<String, String>pPropertyPrefixes,
  		Map<String, String>pPropertiyDisplayNames) {
    
    try {
      MBeanServerConnection mbsc = getMbeanServerConnection();
      
      if (isLoggingDebug())
        ClientUtils.printDebug("default domain : " + mbsc.getDefaultDomain());
      
      if (pBeanList != null && !pBeanList.isEmpty()) {
        Iterator<String> it = pBeanList.iterator();
        String beanPath = null;
        while (it.hasNext()) {
          beanPath = it.next();
          printBeanProperties(mbsc.getDefaultDomain(), beanPath, mbsc, pBeanDisplayNames, pFilteredProperties, pPropertyPrefixes, pPropertiyDisplayNames);
        }
      } else {
        ClientUtils.printWarning("No bean has been defined");
      }
      
    } catch (MalformedURLException mfe) {
      mfe.printStackTrace(System.err);
    } catch (IOException ioe) {
      ioe.printStackTrace(System.err);
    }
  }

  public String getEndPointType() {
    return mEndPointType;
  }
  public void setEndPointType(String pEndPointType) {
    mEndPointType = pEndPointType;
  }

  public String getUser() {
    return mUser;
  }
  public void setUser(String pUser) {
    mUser = pUser;
  }

  public String getPassword() {
    return mPassword;
  }
  public void setPassword(String pPassword) {
    mPassword = pPassword;
  }

  public String getServerUrl() {
    return mServerUrl;
  }
  public void setServerUrl(String pServerUrl) {
    mServerUrl = pServerUrl;
  }

  public String getAdapterName() {
    return mAdapterName;
  }
  public void setAdapterName(String pAdapterName) {
    mAdapterName = pAdapterName;
  }
}

/**
 * @author vrakoton@re-enter.fr
 * @version 1.0
 * 
 * Description : 
 * 
 * Delegate class for reporitory services. this delegate class calculates the repository
 * statisctics the same way the dyn/admin page does.
 * 
 */
package com.fr.atgext.management.repository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import atg.adapter.gsa.GSAItemDescriptor;
import atg.adapter.gsa.GSARepository;
import atg.adapter.gsa.ItemDescriptorStatistics;
import atg.beans.DynamicBeans;
import atg.beans.PropertyNotFoundException;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;
import atg.repository.RepositoryException;

public class RepositoryDelegate extends GenericService implements DynamicMBean {
  final static String HIT_RATIO = "hitRatio";
  final static String USED_RATIO = "usedRatio";
  final static String MISS_COUNT = "missCount";
  final static String METHOD_RESET_CACHE = "resetCache";
  
  
  final static String ATTRIBUTE_SEPARATOR = "|";
  
  GSARepository mReferencedRepository;
  Map<String, ItemDescriptorStatistics> mStatistics;
  
  /**
   * Starting the service : a referenced repository needs to be specified in
   * the .properties configuration file, otherwise, we throw a service exception.
   */
  public void doStartService ()
    throws ServiceException
  {
    super.doStartService();
    if (getReferencedRepository() == null) {
      throw new ServiceException("The referenced repository can not be null");
    }
  }
  
  /**
   * Returns a list of the item descriptor names
   */
  public String [] getItemDescriptorNames() {
    return getReferencedRepository().getItemDescriptorNames();
  }
  
  
  /**
   * Gets the statistics objects from the repository item descriptors
   * @return
   */
  public Map getStatistics () {
    if (mStatistics != null) {
      return mStatistics;
    }
    
    Map stats = new HashMap<String,ItemDescriptorStatistics>();
    
    GSARepository rep = getReferencedRepository();
    GSAItemDescriptor desc = null;
    String [] itemDescNames = rep.getItemDescriptorNames();
    if (itemDescNames != null && itemDescNames.length > 0) {
      try {
        for (int i = 0; i < itemDescNames.length; i++) {
          desc = (GSAItemDescriptor)rep.getItemDescriptor(itemDescNames[i]);
          stats.put(itemDescNames[i],new ItemDescriptorStatistics(desc));
        }
      } catch (RepositoryException re) {
        if (isLoggingError()) {
          logError("A repository exception occured ", re);
        }
      }
    }
    
    mStatistics = stats;
    
    return mStatistics;
  }
  
  /**
   * Invalidates all item descriptor caches
   */
  public void invalidateCaches() {
    if (getReferencedRepository() != null) {
      getReferencedRepository().invalidateCaches();
    }
  }
  
  /**
   * Returns the Nucleus path for the repository this delegate represents
   * @return
   */
  public String getRepositoryPath() {
    if (getReferencedRepository() != null) {
      return getReferencedRepository().getAbsoluteName();
    }
    return null;
  }
  
  public GSARepository getReferencedRepository() {
    return mReferencedRepository;
  }
  public void setReferencedRepository(GSARepository pReferencedRepository) {
    mReferencedRepository = pReferencedRepository;
  }

  public void setStatistics(Map pStatistics) {
    mStatistics = pStatistics;
  }

  /**
   * Gets an attribute value
   * 
   */
  public Object getAttribute(String pAttribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException {
    StringTokenizer tokenizer = new StringTokenizer(pAttribute, ATTRIBUTE_SEPARATOR);
    
    String itemName = tokenizer.nextToken();
    String propertyName = tokenizer.nextToken();
    
    ItemDescriptorStatistics stats = (ItemDescriptorStatistics)getStatistics().get(itemName);
    try {
      if (stats != null) {
        return DynamicBeans.getPropertyValue(stats, propertyName);
      }
    } catch (PropertyNotFoundException pnfe) {
      if (isLoggingError())
        logError("Could not find property " + propertyName, pnfe);
    }
    
    return null;
  }

  /**
   * @todo to be implemented 
   */
  public AttributeList getAttributes(String[] pAttributes) {
    return null;
  }

  /**
   * Constructs the mbean info using Repository statistics
   */
  public MBeanInfo getMBeanInfo() {
    // --- building the attribute lists
    GSARepository rep = getReferencedRepository();
    GSAItemDescriptor desc = null;
    String [] itemDescNames = rep.getItemDescriptorNames();

    if (itemDescNames != null && itemDescNames.length > 0) {
      MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[itemDescNames.length*3];
      try {
        for (int i = 0; i < itemDescNames.length; i++) {
          desc = (GSAItemDescriptor)rep.getItemDescriptor(itemDescNames[i]);
          attrs[3*i] = new MBeanAttributeInfo(
              itemDescNames[i] + ATTRIBUTE_SEPARATOR + HIT_RATIO,
              "java.lang.Double",
               itemDescNames [i] + " item cache hit ratio",
              true,   // isReadable
              false,   // isWritable
              false); // isIs
          ;
          attrs[3*i+1] = new MBeanAttributeInfo(
              itemDescNames[i] + ATTRIBUTE_SEPARATOR + USED_RATIO,
              "java.lang.Double",
               itemDescNames [i] + " item cache used Ratio",
              true,   // isReadable
              false,   // isWritable
              false); // isIs
          ;
          attrs[3*i+2] = new MBeanAttributeInfo(
              itemDescNames[i] + ATTRIBUTE_SEPARATOR + MISS_COUNT,
              "java.lang.Integer",
               itemDescNames [i] + " item cache miss count",
              true,   // isReadable
              false,   // isWritable
              false); // isIs
          ;
        }
      } catch (RepositoryException re) {
        if (isLoggingError()) {
          logError("A repository exception occured ", re);
        }
      }
      
      MBeanOperationInfo[] opers = {
        new MBeanOperationInfo(
                METHOD_RESET_CACHE,
                "Reset all caches",
                null,   // no parameters
                "void",
                MBeanOperationInfo.ACTION)
      };

      return new MBeanInfo(
          this.getClass().getName(),
          "Property Manager MBean",
          attrs,
          null,  // constructors
          opers,
          null); // notifications

    }
    return null;
  }
  
  /**
   * Resets all statistics 
   */
  private void resetStatistics() {
    Map stats = getStatistics();
    
    if (stats != null && !stats.isEmpty()) {
      Iterator keyIt = stats.keySet().iterator();
      String key = null;
      ItemDescriptorStatistics statObject = null;
      
      while (keyIt.hasNext()) {
        key = (String)keyIt.next();
        statObject = (ItemDescriptorStatistics)stats.get(key);
        statObject.resetStatistics();
      }
    }
  }
  
  public Object invoke(String pActionName, Object[] pParams, String[] pSignature)
      throws MBeanException, ReflectionException {
    if (StringUtils.isBlank(pActionName))
      return null;
    
    if (METHOD_RESET_CACHE.equals(pActionName)) {
      resetStatistics();
    }
    return null;
  }

  public void setAttribute(Attribute pAttribute)
      throws AttributeNotFoundException, InvalidAttributeValueException,
      MBeanException, ReflectionException {
    // TODO Auto-generated method stub
  }

  public AttributeList setAttributes(AttributeList pAttributes) {
    // TODO Auto-generated method stub
    return null;
  }

}

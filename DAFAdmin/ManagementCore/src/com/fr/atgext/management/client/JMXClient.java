/**
 * @author vrakoton@re-enter.fr
 * @version 1.0
 * 
 * Description :
 * 
 * JMX client command line class. This is the class to call from the command line.
 */
package com.fr.atgext.management.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class JMXClient {
  final static String PARAM_CONFIG = "-config";
  final static String PARAM_DEBUG = "-debug";
  final static String PARAM_USER = "-user";
  final static String PARAM_PASSWORD = "-password";
  final static String PARAM_URL = "-url";
  
  static final String CONN_JBOSS_END_POINT = "JBOSS";
  static final String CONN_ATG_END_POINT = "ATG";
  
  static final String XML_ELEMENT_CONN_PROP = "connection-properties";
  static final String XML_ELEMENT_URL = "url";
  static final String XML_ELEMENT_USER = "user";
  static final String XML_ELEMENT_PASSWORD = "password";
  static final String XML_ELEMENT_ENDPOINT_TYPE = "endpoint-type";
  
  static final String XML_ATTR_PATH = "path";
  static final String XML_ATTR_DISPLAY_NAME = "displayName";
  static final String XML_ATTR_NAME = "name";
  static final String XML_ATTR_PREFIX = "prefix";
  
  static final String XML_ELEMENT_BEANS = "beans";
  static final String XML_ELEMENT_BEAN_DESC = "bean";
  static final String XML_ELEMENT_PROPERTY = "property";
  
  public static final String PROPERTY_SEPARATOR = ".";
  
  ClientOptions mOptions;
  
  String mConfigFilePath;
  String mServerUrl;
  String mUser;
  String mPassword;
  String mEndPointType;
  String mAdapterName;
  List<String> mBeanList;
  Map<String, String> mBeanDisplayNames;
  Map <String, List <String>> mFilteredProperties = new HashMap();
  Map <String, String> mPropertyPrefixes;
  Map<String, String> mPropertyDisplayNames;
  
  public void printUsage () {
    System.out.println("usage : java -cp atgmanagement.jar com.fr.atgext.management.client.ATGJMXClient -config <path to config file>");
  }
  
  void addOption (String pOptionName, String pOptionValue) {
    if (mOptions == null)
      mOptions = new ClientOptions();
    mOptions.addOption(pOptionName, pOptionValue);
  }
  
  /**
   * read arguments from the command line
   * @param args
   * @return
   */
  public boolean readArgs (String [] args) {
    if (args == null || args.length < 2) {
      ClientUtils.printError("Invalid arguments ");
      return false;
    }
    
    String currentArg = null;
    String argValue = null;
    for (int i = 0; i < args.length; i++) {
      currentArg = args[i];
      if (!ClientUtils.isBlankString(currentArg)) {
        if (i < args.length -1) {
          argValue = args[i+1];
          i++;
        }
        setParameter(currentArg, argValue);
      }
    }
    return checkParameters();
  }
  
  /**
   * Sets a client parameter from what is read from the command line
   * @param pParameter
   * @param pValue
   */
  public void setParameter(String pParameter, String pValue) {
    // --- set the config file path
    if (PARAM_CONFIG.equals(pParameter)) {
      mConfigFilePath = pValue;
    }
    if (PARAM_DEBUG.equals(pParameter)) {
      if (pValue != null && ClientOptions.OPTION_TRUE.equals(pValue)) {
        addOption(ClientOptions.OPTION_DEBUG, ClientOptions.OPTION_TRUE);
      }
    }
    
    // --- add user name for JMX connection
    if (PARAM_USER.equals(pParameter)) {
      if (pValue != null ) {
        mUser = pValue;
      }
    }
    
    // --- set the password for the connection
    if (PARAM_PASSWORD.equals(pParameter)) {
      if (pValue != null ) {
        mPassword = pValue;
      }
    }
    
    if (PARAM_URL.equals(pParameter)) {
      if (pValue != null ) {
        mServerUrl = pValue;
      }
    }
  }
  
  /**
   * Checks that all parameters are set for the client to run properly 
   * @return
   */
  public boolean checkParameters () {
    // -- check config file
    if (ClientUtils.isBlankString(getConfigFilePath())) {
      ClientUtils.printError("Config file path can not be null, please use " + PARAM_CONFIG + " to specify a config file");
      return false;
    }
    File config = new File(getConfigFilePath());
    if (!config.exists() || !config.isFile() || !config.canRead()) {
      ClientUtils.printError("Config file " +  getConfigFilePath() + " does not exist or is not readable");
      return false;
    }
    return true;
  }
  
  /**
   * Reads the configuration file and sets :
   * <ul>
   * <li>The list of bean to query
   * <li>The list of property to print for each bean if specified in the config file
   * </ul>
   * @param pRootElement
   */
  void configureBeans(Element pRootElement) {
    Element beansElement = pRootElement.getChild(XML_ELEMENT_BEANS);
    if (beansElement == null) {
      throw new RuntimeException("XML configuration sould contain at least a <" + XML_ELEMENT_BEANS + "> tag");
    }
    List <Element> beanDescElements = beansElement.getChildren(XML_ELEMENT_BEAN_DESC);
    if (beanDescElements != null && !beanDescElements.isEmpty()) {
      Iterator<Element> it = beanDescElements.iterator();
      Element beanDescElement = null;
      while (it.hasNext()) {
        beanDescElement = it.next();
        Attribute path = beanDescElement.getAttribute(XML_ATTR_PATH);
        if (path == null || ClientUtils.isBlankString(path.getValue())) {
          throw new RuntimeException("Missing bean path attribute");
        }
        addBeanToList(path.getValue());
        
        // --- should we add a bean display name override?
        Attribute displayName = beanDescElement.getAttribute(XML_ATTR_DISPLAY_NAME);
        if (displayName != null && !ClientUtils.isBlankString(displayName.getValue())) {
        	addBeanDisplayName (path.getValue(), displayName.getValue());
        }
        
        // --- should we addd a prefix to all bean properties
        Attribute prefix = beanDescElement.getAttribute(XML_ATTR_PREFIX);
        if (prefix != null && !ClientUtils.isBlankString(prefix.getValue())) {
        	addBeanPropertyPrefix(path.getValue(), prefix.getValue());
        }
        
        List <Element> properties = beanDescElement.getChildren(XML_ELEMENT_PROPERTY);
        if (properties != null && !properties.isEmpty()) {
          Iterator<Element> propIt = properties.iterator();
          List<String> propertyList = new ArrayList<String>(properties.size());
          while (propIt.hasNext()) {
            Element property = propIt.next();
            String propName = property.getAttributeValue(XML_ATTR_NAME);
            if (!ClientUtils.isBlankString(propName)) {
              propertyList.add(propName);
              String propDisplayName = property.getAttributeValue(XML_ATTR_DISPLAY_NAME);
              if (!ClientUtils.isBlankString(propDisplayName)) {
              	addPropertyDisplayName(path.getValue(), propName, propDisplayName);
              }
            } else {
            	throw new RuntimeException("Property must have a 'name' attribute");
            }
          }
          if (!propertyList.isEmpty()) {
            addToPropertyFilter(path.getValue(), propertyList);
          }
        }
      }
    }
  }
  
  void addBeanToList (String pBeanPath) {
    if (!ClientUtils.isBlankString(pBeanPath)) {
      if (mBeanList == null)
        mBeanList = new ArrayList<String>();
      mBeanList.add(pBeanPath);
    }
  }
  
  /**
   * Registers a bean display name override. This display name will be printed out instead
   * of the bean JMX object Name
   * @param pBeanPath
   * @param pBeanDisplayName
   */
  void addBeanDisplayName (String pBeanPath, String pBeanDisplayName) {
	 if (!ClientUtils.isBlankString(pBeanPath) && !ClientUtils.isBlankString(pBeanDisplayName)) {
		if (mBeanDisplayNames == null) {
			mBeanDisplayNames = new HashMap<String, String>();
		}
		mBeanDisplayNames.put(pBeanPath,pBeanDisplayName);
	 }
  }
  
  void addBeanPropertyPrefix (String pBeanPath, String pPropertyPrefix) {
	  if (mPropertyPrefixes == null) {
		  mPropertyPrefixes = new HashMap<String, String>();
	  }
	  mPropertyPrefixes.put(pBeanPath, pPropertyPrefix);
  }
  
  /**
   * Adding a specific list of properties to display for a JMX bean. If a list is not specified
   * for a bean, all of its properties are displayed
   * @param pBeanPath
   * @param pPropertyNames
   */
  void addToPropertyFilter (String pBeanPath, List<String>pPropertyNames) {
    if (mFilteredProperties == null) {
      mFilteredProperties = new HashMap<String, List<String>>();
    }
    if (pPropertyNames != null)
      mFilteredProperties.put(pBeanPath, pPropertyNames);
  }
  
  /**
   * Adds a custom display name for a bean property. Custom display names are stored in a map
   * with (key, value) constructed as follows : (<beanpath>.<propertyname>, <customdisplayname)
   * @param pBeanPath JMX bean path
   * @param pPropName property name
   * @param pPropDisplayName the custom display name
   */
  void addPropertyDisplayName(String pBeanPath, String pPropName, String pPropDisplayName) {
  	if (mPropertyDisplayNames == null) {
  		mPropertyDisplayNames = new HashMap<String, String>();
  	}
  	mPropertyDisplayNames.put(pBeanPath + PROPERTY_SEPARATOR + pPropName, pPropDisplayName);
  }
  
  /**
   * Parses the connection properties configuration and sets the client connection
   * parameters:
   * <ul>
   * <li> The connection url 
   * <li> The user login to use (not required)
   * <li> The user password to use (not required)
   * </ul>
   * @param pElement
   */
  void configureConnection (Element pElement) {
    // --- get the user name if any
    Element param = pElement.getChild(XML_ELEMENT_USER);
    if (param != null) {
      mUser = param.getValue();
    }
    
    // --- get the user password if any
    param = pElement.getChild(XML_ELEMENT_PASSWORD);
    if (param != null) {
      mPassword = param.getValue();
    }
    
    // --- get the connection url
    param = pElement.getChild(XML_ELEMENT_URL);
    if (param != null) {
      mServerUrl = param.getValue();
    }
    
    // --- get the end point type
    param = pElement.getChild(XML_ELEMENT_ENDPOINT_TYPE);
    if (param != null) {
      mEndPointType = param.getValue();
    }
  }
  
  /**
   * Reads the config File and configures the client for the JMX query
   * @param pConfigFile
   */
  public void readConfigFile (File pConfigFile) {
    // --- build document object
    SAXBuilder builder = new SAXBuilder();
    try {
      Document doc = builder.build(pConfigFile);
      Element root= doc.getRootElement();
      Element connParams = root.getChild(XML_ELEMENT_CONN_PROP);
      if (connParams != null) {
        configureConnection(connParams);
      }
      configureBeans(root);
      
    } catch (JDOMException e) {
      e.printStackTrace(System.err);
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
  }
  
  /**
   * 
   * @return
   */
  public boolean runClient () {
    File configFile = new File (getConfigFilePath());
    readConfigFile(configFile);
    
    // --- verify that we have at least a connection URL
    if (ClientUtils.isBlankString(this.getServerUrl())) {
      printUsage();
      throw new RuntimeException("No URL specified. Either use the config XML file to " +
      		"specify the connection properties or add the -url argument to your command line call");
    }
    
    GenericJMXStubClient client = null;
    
    if (CONN_ATG_END_POINT.equals(getEndPointType()))
      client = new ATGJMXStubClient();
    else if (CONN_JBOSS_END_POINT.equals(getEndPointType()))
      client = new JBossJMXStubClient();
    else
    	client = new GenericJMXStubClient();
    
    if (client == null)
      client = new GenericJMXStubClient();
    
    
    
    client.setServerUrl(this.getServerUrl());
    client.setUser(this.getUser());
    client.setPassword(this.getPassword());
    client.setEndPointType(this.getEndPointType());
    
    client.callMBeanServer(mBeanList, mBeanDisplayNames, mFilteredProperties, mPropertyPrefixes, mPropertyDisplayNames);
    
    return true;
  }
  
  // GETTERS and SETTERS
  public String getConfigFilePath() {
    return mConfigFilePath;
  }
  public void setConfigFilePath(String pConfigFilePath) {
    mConfigFilePath = pConfigFilePath;
  }
  
  public String getServerUrl() {
    return mServerUrl;
  }

  public void setServerUrl(String pServerUrl) {
    mServerUrl = pServerUrl;
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

  public String getEndPointType() {
    return mEndPointType;
  }

  public void setEndPointType(String pEndPointType) {
    mEndPointType = pEndPointType;
  }

  public String getAdapterName() {
    return mAdapterName;
  }

  public void setAdapterName(String pAdapterName) {
    mAdapterName = pAdapterName;
  }
  
  // END OF GETTERS and SETTERS
  
  /**
   * Main methods
   * @param args command line args
   */
  public static void main(String [] args) {
    JMXClient client = new JMXClient();
    if (client.readArgs(args)) {
      client.runClient();
    } else {
      client.printUsage();
    }
  }

}

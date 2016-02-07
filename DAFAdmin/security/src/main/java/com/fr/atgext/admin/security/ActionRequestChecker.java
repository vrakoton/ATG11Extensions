package com.fr.atgext.admin.security;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import atg.core.util.StringUtils;
import atg.nucleus.ServiceException;
import atg.security.Persona;
import atg.security.User;
import atg.servlet.DynamoHttpServletRequest;

/**
 * 
 * @author vrakoton
 * 
 * Description:
 * 
 * This class is responsible for checking incoming requests and determine whether the current user has
 * the right to call that url (POST or GET).
 * 
 * <ul>
 * 	<li> The static file requests (css, images etc...) are not checked and not filtered
 * 	<li> The <strong>allowedAccounts</strong> property should contain a list of the admin user group which are allowed to 
 * 		access the dyn/admin interface
 * 
 * 	<li> The <strong>noFilterExtensions</strong> holds a list of file extensions which are never filtered
 * 
 * 	<li> The <strong>noFilterPath</strong> property let you specify a list of path which are never filtered no matter what parameter is passed within the request.
 * 
 * 	<li> The <strong>unAllowedPropertyChangeAccounts</strong> property allows you to specify which action is NOT authorized for a given user group. Action is
 * detected by intercepting request parameters so this property is built as a Map of (group name, list of parameters which are not allowed in requests)
 * 
 * 	<li> The <strong>partialUnallowedPropertyChangeAccounts</strong> specifies which values are NOT allowed for a given parameter and for a user group. This is mainly used to allow users 
 *  to use features like repository queries when using some specific verbs only :
 *  	<ul>
 *  		<li><em>developers-group=xmltext:add-item:remove-item</em> means that developers can not use the <string>add-item</strong> and <strong>remove-item</strong> 
 *  		verbs in the repository XML queries in dyn/admin
 *  	</ul>
 *     
 * </ul> 
 * It should be inserted in the admin dynamo pipeline
 *
 */
public class ActionRequestChecker extends AbstractRequestCheckerService implements RequestCheckerService {
	final static String SEPARATOR = "|";
	final static String VALUE_SEPARATOR = ":";
  List<String> mNoFilterExtensions;
  List <String> mNoFilterPath;
  Map<String, String> mUnAllowedPropertyChangeAccounts;
  List <String> mIgnoredPropertyValue;
  Map<String, String> mPartialUnallowedPropertyChangeAccounts;
  
  Map<String, Map<String, List<String>>> mDisallowedParameterValues;
  
  
  /**
   * Initialize the service at startup. We build a map of rights for the partial authorization.
   * @see atg.nucleus.GenericService#doStartService()
   */
  public void doStartService()
  	throws ServiceException
  {
	 super.doStartService();
	 if (getPartialUnallowedPropertyChangeAccounts() != null && !getPartialUnallowedPropertyChangeAccounts().isEmpty()) {
		 Iterator<String> keyIt = getPartialUnallowedPropertyChangeAccounts().keySet().iterator();
		 String groupName = null;
		 String propName = null;
		 String properties = null;
		 String propertyValues = null; 
		 while (keyIt.hasNext()) {
			 groupName = keyIt.next();
			 properties = getPartialUnallowedPropertyChangeAccounts().get(groupName);
			 if (properties != null) {
				 final StringTokenizer tokenizer = new StringTokenizer(properties, SEPARATOR);
				 while (tokenizer.hasMoreTokens()) {
					 propertyValues = tokenizer.nextToken();
					 if (!StringUtils.isBlank(propertyValues)) {
						 final StringTokenizer valueTokenizer = new StringTokenizer(propertyValues, VALUE_SEPARATOR);
						 propName = valueTokenizer.nextToken();
						 while(valueTokenizer.hasMoreTokens()) {
							 addDisallowedValueForProperty(groupName, propName, valueTokenizer.nextToken());
						 }
					 }
				 }
			 }
		 }
	 }
  }
  
  /**
   * adds a partial disalow right for a given admin user group
   * @param pGroupName the name of the admin user group
   * @param pPropertyName the property which is allowed for the given group
   * @param pValue the 
   */
  void addDisallowedValueForProperty (String pGroupName, String pPropertyName, String pValue) {
	  if (StringUtils.isBlank(pGroupName) || StringUtils.isBlank(pPropertyName) || StringUtils.isBlank(pValue)) {
		  return;
	  }
	  
	  if (mDisallowedParameterValues == null) {
		  mDisallowedParameterValues = new HashMap<String, Map<String, List<String>>>();
	  }
	  Map <String, List<String>> groupProperties = mDisallowedParameterValues.get(pGroupName);
	  if (groupProperties == null) {
		  groupProperties = new HashMap<String, List<String>>();
		  mDisallowedParameterValues.put(pGroupName, groupProperties);
	  }
	  
	  List<String> allowedValues = groupProperties.get(pPropertyName);
	  if (allowedValues == null) {
		  allowedValues = new ArrayList<String>();
		  groupProperties.put(pPropertyName, allowedValues);
	  }
	  allowedValues.add(pValue);
  }
  
  /**
   * Checks if the request does not contain a parameter with a forbidden value
   * @param pRequest
   * @param pAccountName
   * @return true if the request is safe, false if it contains unallowed parameter(s)
   */
  boolean hasPartialRight (DynamoHttpServletRequest pRequest, String pAccountName) {
	  Enumeration<String> paramaeterNames = pRequest.getParameterNames();
	  
	  if (paramaeterNames == null || mDisallowedParameterValues == null)
		  return true;
	  
	  Map<String, List<String>> partialRights = mDisallowedParameterValues.get(pAccountName);
	  if (partialRights == null) {
		  return true;
	  }
	  
	  String paramName = null;
	  String paramValue = null;
	  while(paramaeterNames.hasMoreElements()) {
		  paramName = paramaeterNames.nextElement();
		  List<String> unAllowedValuesForProperty = partialRights.get(paramName);
		  if (unAllowedValuesForProperty == null) {
			  continue;
		  }
		  
		paramValue = pRequest.getParameter(paramName);
		
		if (!StringUtils.isBlank(paramValue)) {
			Iterator<String> it = unAllowedValuesForProperty.iterator();
			while(it.hasNext()) {
				if (paramValue.indexOf(it.next()) >= 0) {
					return false;
				}
			}
		}
	  }
	  
	  return true;
  }
 
  /**
   * Overrides the original authenticate method of ATG class.
   * <ul>
   * 	<li> the list of path which are specified in <strong>noFilterPath</strong> are never filtered
   * 	<li> static web resource requests are ignored and not filtered (css, images...)
   * </ul>
   */
  public void checkRequest(DynamoHttpServletRequest pRequest) {
    if (pRequest != null) {
      String path = pRequest.getPathInfo();

      if (getNoFilterPath() != null) {
        Iterator<String> it = getNoFilterPath().iterator();
        String excludedPath = null;
        while (it.hasNext()) {
          excludedPath = (String) it.next();
          if (path.toUpperCase().equals(excludedPath.toUpperCase())) {
            return;
          }
        }
      }
      if (getNoFilterExtensions() != null) {
        Iterator<String> it = getNoFilterExtensions().iterator();
        String extension = null;
        while (it.hasNext()) {
          extension = it.next();
          if (path.toUpperCase().endsWith(extension.toUpperCase())) {
            if (isLoggingDebug())
              logDebug("Path ends with a static file extension, not filtering");
            return;
          }
        }
      }

    }
    
    User user = getUser(pRequest);
    
    if (user == null) {
      throw new AdminActionNotAllowedException("Can not resolve user");
    }
    
    Persona[] userPersonae = user.getPersonae(getUserAuthority());
    
    // --- check that the user is part of the allowed groups
    super.checkRequest(pRequest);
    
    if (userPersonae == null) {
      throw new UnauthenticatedUserException();
    }

    if ((getUnAllowedPropertyChangeAccounts() != null && !getUnAllowedPropertyChangeAccounts()
        .isEmpty()) || (mDisallowedParameterValues != null && !mDisallowedParameterValues.isEmpty())) {
      for (int i = 0; i < userPersonae.length; i++) {

        // --- does the user have one of the filtered persona
        String groupToCheck = null;

        // --- determine whether we need to check if the user has access to the
        // action he has requested
        for (String groupName : getUnAllowedPropertyChangeAccounts().keySet()) {
          groupToCheck = groupName;
          Persona persona = getUserAuthority().getPersona(groupName);
          if (userPersonae[i].hasPersona(persona)) {
            // --- get the unallowed properties for user
            if (isLoggingDebug())
              logDebug("Checking unallowed properties for persona " + groupName);

            String forbiddenParameters = getUnAllowedPropertyChangeAccounts()
                .get(groupName);

            if (!StringUtils.isBlank(forbiddenParameters)) {
              // --- we found that the user has some forbidden parameters, check
              // that request does not contain these parameters
              Enumeration<String> parameterNames = pRequest.getParameterNames();
              String propName = null;
              String monitoredPropName = null;
              if (parameterNames != null) {
                while (parameterNames.hasMoreElements()) {
                  propName = (String) parameterNames.nextElement();
                  StringTokenizer tokenizer = new StringTokenizer(forbiddenParameters,
                      SEPARATOR);
                  while (tokenizer.hasMoreTokens()) {
                    monitoredPropName = tokenizer.nextToken();
                    if (monitoredPropName.equals(propName)) {
                      if (getIgnoredPropertyValue() != null) {
                        Iterator<String> propValIt = getIgnoredPropertyValue()
                            .iterator();
                        for (String propValue : getIgnoredPropertyValue()) {
                          if (propValue.equals(pRequest.getParameter(propName))) {
                            return;
                          }
                        }
                      }
                      throw new AdminActionNotAllowedException(
                          "You do not have the authorization level to view this page");
                    }
                  }
                }
              }

              // --- manage post parameters
              parameterNames = pRequest.getPostParameterNames();
              if (parameterNames != null) {
                while (parameterNames.hasMoreElements()) {
                  propName = (String) parameterNames.nextElement();
                  StringTokenizer tokenizer = new StringTokenizer(forbiddenParameters,
                      SEPARATOR);
                  while (tokenizer.hasMoreTokens()) {
                    monitoredPropName = tokenizer.nextToken();
                    if (monitoredPropName.equals(propName)) {
                      if (getIgnoredPropertyValue() != null) {
                        Iterator<String> propValIt = getIgnoredPropertyValue()
                            .iterator();
                        String propValue = null;
                        while (propValIt.hasNext()) {
                          propValue = propValIt.next();
                          if (propValue.equals(pRequest
                              .getPostParameter(propName))) {
                            return;
                          }
                        }
                      }
                      throw new AdminActionNotAllowedException(
                          "You do not have the authorization level to do this action");
                    }
                  }
                }
              }
            }
          }
        }

        // --- determine whether we need to check if the user has partial
        if (mDisallowedParameterValues != null && !mDisallowedParameterValues.isEmpty()) {
          for (String groupName : mDisallowedParameterValues.keySet()) {
            Persona persona = getUserAuthority().getPersona(groupName);
            if (userPersonae[i].hasPersona(persona)) {
              if (!hasPartialRight(pRequest, groupName))
                throw new AdminActionNotAllowedException(
                    "You do not have the authorization level to view this page");
            }
          }
        }
      }
    }
  }
  
  
  public List<String> getNoFilterExtensions() {
    return mNoFilterExtensions;
  }
  public void setNoFilterExtensions(List<String> pNoFilterExtensions) {
    mNoFilterExtensions = pNoFilterExtensions;
  }


  public List<String> getNoFilterPath() {
    return mNoFilterPath;
  }


  public void setNoFilterPath(List<String> pNoFilterPath) {
    mNoFilterPath = pNoFilterPath;
  }


  public Map<String, String> getUnAllowedPropertyChangeAccounts() {
    return mUnAllowedPropertyChangeAccounts;
  }
  public void setUnAllowedPropertyChangeAccounts(
      Map<String, String> pUnAllowedPropertyChangeAccounts) {
    mUnAllowedPropertyChangeAccounts = pUnAllowedPropertyChangeAccounts;
  }


  public List<String> getIgnoredPropertyValue() {
    return mIgnoredPropertyValue;
  }
  public void setIgnoredPropertyValue(List<String> pIgnoredPropertyValue) {
    mIgnoredPropertyValue = pIgnoredPropertyValue;
  }


	public Map<String, String> getPartialUnallowedPropertyChangeAccounts() {
		return mPartialUnallowedPropertyChangeAccounts;
	}
	
	
	public void setPartialUnallowedPropertyChangeAccounts(
			Map<String, String> pPartialUnallowedPropertyChangeAccounts) {
		mPartialUnallowedPropertyChangeAccounts = pPartialUnallowedPropertyChangeAccounts;
	}
}

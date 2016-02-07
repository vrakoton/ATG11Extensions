package com.fr.atgext.servlet.adminpipeline;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.fr.atgext.admin.security.AdminActionNotAllowedException;
import com.fr.atgext.admin.security.RequestCheckerService;
import com.fr.atgext.admin.security.UnauthenticatedUserException;

import atg.core.util.StringUtils;
import atg.nucleus.Nucleus;
import atg.nucleus.ServiceException;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.pipeline.InsertableServletImpl;

/**
 * 
 * @author vrakoton
 * @version $Rev$
 * 
 * Description:
 * 
 * This pipeline servlet checks the incoming requests for url substring. If the pattern is found,
 * the corresponding request checker is called 
 *
 */
public class AdminPathSecurityServlet extends InsertableServletImpl {
  Map<String, String> mUrlToRequestCheckerPathMap;
  
  boolean mEnabled = true;
  
  Map<String, RequestCheckerService> mUrlToRequestCheckerMap;
  
  String mAuthenticateHeaderValue;
  
  /**
   * Initializes the service by transforming the map of url to Nucleus service path
   * to a map where keys are urls and the value is a Nucleus object by invoking the
   * Nucleus service name resolution method.
   */
  public void doStartService() throws ServiceException
  {
    super.doStartService();
    
    if (getUrlToRequestCheckerPathMap() != null && !getUrlToRequestCheckerPathMap().isEmpty()) {
      if (mUrlToRequestCheckerMap == null) {
        mUrlToRequestCheckerMap = new HashMap<String, RequestCheckerService>();
      }
      for (String url : getUrlToRequestCheckerPathMap().keySet()) {
        final RequestCheckerService checker = (RequestCheckerService)Nucleus.getGlobalNucleus().resolveName(getUrlToRequestCheckerPathMap().get(url));
        if (checker != null) {
          mUrlToRequestCheckerMap.put(url, checker);
        } else {
          if (isLoggingError()) {
            logError("Could not resolve a component with path: " + getUrlToRequestCheckerPathMap().get(url));
          }
        }
      }
    }
  }
  
  /**
   * this method checks if the requested url contains a string which is protected by a parameter
   * checker service and calls the appropriate service. If the check fails, a 403
   * error is returned to the user.
   */
  public void service (DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse)
    throws IOException, ServletException
  {
    if (!isEnabled()) {
      passRequest(pRequest, pResponse);
      return;
    }
    
    String url = pRequest.getPathInfo();
    if (StringUtils.isBlank(url)) {
      if (isLoggingDebug()) {
        logDebug("Can not filter a blank URL");
      }
      passRequest(pRequest, pResponse);
      return;
    }
    
    for (String u : mUrlToRequestCheckerMap.keySet()) {
      if (url.toUpperCase().indexOf(u.toUpperCase()) > -1) {
        try {
          mUrlToRequestCheckerMap.get(u).checkRequest(pRequest);
        } catch (AdminActionNotAllowedException pcna) {
          pResponse.sendError(403, pcna.getMessage());
          return;
        } catch (UnauthenticatedUserException uue) {
          pResponse.setHeader("WWW-Authenticate", getAuthenticateHeaderValue());
          pResponse.sendError(401);
          return;
        }
      }
    }
    passRequest(pRequest, pResponse);
  }
  
  /**
   * Indicates if this service is enabled. You can either set the enabled property to false
   * or let the Url to request checker paath map to null to disable the service.
   * @return
   */
  public boolean isEnabled() {
    return (mEnabled && mUrlToRequestCheckerMap != null && !mUrlToRequestCheckerMap.isEmpty());
  }
  
  public void setEnabled(boolean pEnabled) {
    mEnabled = pEnabled;
  }
  
  public Map<String, String> getUrlToRequestCheckerPathMap() {
    return mUrlToRequestCheckerPathMap;
  }
  public void setUrlToRequestCheckerPathMap(
      Map<String, String> pUrlToRequestCheckerPathMap) {
    mUrlToRequestCheckerPathMap = pUrlToRequestCheckerPathMap;
  }

  public String getAuthenticateHeaderValue() {
    return mAuthenticateHeaderValue;
  }

  public void setAuthenticateHeaderValue(String pAuthenticateHeaderValue) {
    mAuthenticateHeaderValue = pAuthenticateHeaderValue;
  }

  
}

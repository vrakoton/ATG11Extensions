package com.fr.atgext.admin.security;

import java.util.List;

import atg.core.util.Base64;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.security.LoginUserAuthority;
import atg.security.Persona;
import atg.security.ThreadSecurityManager;
import atg.security.User;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.pipeline.Authenticator;

/**
 * 
 * @author vrakoton
 * @version $Rev$
 *
 */
public abstract class AbstractRequestCheckerService extends GenericService implements
    RequestCheckerService {
  
  String mUserPath = "/atg/dynamo/security/User"; 
  LoginUserAuthority mUserAuthority;
  Authenticator mAuthenticator;
  List<String> mAllowedGroups;
  
  /**
   * resolves the user which is attached to the current request.
   * @param pRequest
   * @return
   */
  public User getUser(DynamoHttpServletRequest pRequest) {
    User u = ThreadSecurityManager.currentUser();
    // --- user is already in request, just return it
    if (u != null) {
      return u;
    }
    
    if (!StringUtils.isBlank(mUserPath)) {
      u = (User)pRequest.resolveName(mUserPath);
    }
    
    if (u != null && u.getPersonae(getUserAuthority()) != null) {
      ThreadSecurityManager.setThreadUser(u);
      return u;
    }
    
    if (u == null) {
      u = new User();
      ThreadSecurityManager.setThreadUser(u);
    }
    
    // --- try to resolve user from request
    String auth = pRequest.getHeader("Authorization");
    if (!StringUtils.isBlank(auth)) {
      int ix = auth.indexOf(' ');
      if (ix < 0) {
        return u;
      }
      String authscheme = auth.substring(0, ix);
      if (authscheme.equalsIgnoreCase("Basic")) {
        String authval = auth.substring(ix + 1);
        String plain = Base64.decodeToString(authval);
        String [] tokens = plain.split(":");
        if (tokens.length < 2) {
          return u;
        }
        String userId = tokens[0];
        String password = tokens[1];
        
       boolean success = getAuthenticator().authenticate(userId, password);
       if (!success) {
         throw new UnauthenticatedUserException();
       }
      } else {
        if (isLoggingError()) {
          logError("Unsupported authentication scheme. Can not resolve user");
        }
      }
    }
    return u;
  }
  
  /**
   * In the basic implementation of the request checker service, the user should have at least on of
   * the allowed groups in his personae. If the allowedGroups property is empty or null, this means
   * the url is forbidden for everybody.
   */
  @Override
  public void checkRequest(DynamoHttpServletRequest pRequest)
      throws AdminActionNotAllowedException {
    if (getAllowedGroups() == null || getAllowedGroups().isEmpty()) {
      throw new AdminActionNotAllowedException("No group is allowed for this url");
    }
    
    User u = getUser(pRequest);
    if (u == null) {
      throw new UnauthenticatedUserException();
    }
    
    Persona[] personae = u.getPersonae();
    if (personae == null || personae.length < 1) {
      throw new UnauthenticatedUserException("User has no group");
    }
    
    for (String group : getAllowedGroups()) {
      final Persona p = getUserAuthority().getPersona(group);
      for (int i = 0; i < personae.length; i++) {
        if (personae[i].hasPersona(p)) {
          return;
        }
      }
    }
    throw new AdminActionNotAllowedException("User does not have the right groups");
  }

  public String getUserPath() {
    return mUserPath;
  }

  public void setUserPath(String pUserPath) {
    mUserPath = pUserPath;
  }

  public LoginUserAuthority getUserAuthority() {
    return mUserAuthority;
  }

  public void setUserAuthority(LoginUserAuthority pUserAuthority) {
    mUserAuthority = pUserAuthority;
  }

  public Authenticator getAuthenticator() {
    return mAuthenticator;
  }

  public void setAuthenticator(Authenticator pAuthenticator) {
    mAuthenticator = pAuthenticator;
  }

  public List<String> getAllowedGroups() {
    return mAllowedGroups;
  }

  public void setAllowedGroups(List<String> pAllowedGroups) {
    mAllowedGroups = pAllowedGroups;
  }

}

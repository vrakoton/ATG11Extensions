package com.fr.atgext.admin.security;

import atg.core.util.Base64;
import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.security.LoginUserAuthority;
import atg.security.ThreadSecurityManager;
import atg.security.User;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.pipeline.Authenticator;


public abstract class AbstractRequestCheckerService extends GenericService implements
    RequestCheckerService {
  
  String mUserPath = "/atg/dynamo/security/User"; 
  LoginUserAuthority mUserAuthority;
  Authenticator mAuthenticator;
  
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
  
  @Override
  public void checkRequest(DynamoHttpServletRequest pRequest)
      throws AdminActionNotAllowedException {
    throw new RuntimeException("Unimplemented method");
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

}

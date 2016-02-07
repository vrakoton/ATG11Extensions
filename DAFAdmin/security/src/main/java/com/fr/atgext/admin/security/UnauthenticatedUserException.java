package com.fr.atgext.admin.security;

public class UnauthenticatedUserException extends RuntimeException {
  private static final long serialVersionUID = -3127325951441224645L;

  public UnauthenticatedUserException () {
    super();
  }
  
  public UnauthenticatedUserException (String pMessage) {
    super(pMessage);
  }
}

package com.fr.atgext.admin.security;

public class AdminActionNotAllowedException extends RuntimeException {
  private static final long serialVersionUID = -1736851197047031223L;

  public AdminActionNotAllowedException () {
    super();
  }
  
  public AdminActionNotAllowedException (String pMessage) {
    super(pMessage);
  }
}

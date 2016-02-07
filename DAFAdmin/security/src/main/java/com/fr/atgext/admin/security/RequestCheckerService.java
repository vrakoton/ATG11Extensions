package com.fr.atgext.admin.security;

import atg.servlet.DynamoHttpServletRequest;

public interface RequestCheckerService {
  public void checkRequest(DynamoHttpServletRequest pRequest) throws AdminActionNotAllowedException;
}

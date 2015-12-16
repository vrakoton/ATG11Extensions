package com.fr.atgext.servlet.pipeline;

public class PropertyChangeNotAllowedException extends RuntimeException {
  public PropertyChangeNotAllowedException () {
    super();
  }
  
  public PropertyChangeNotAllowedException (String pMessage) {
    super(pMessage);
  }
}

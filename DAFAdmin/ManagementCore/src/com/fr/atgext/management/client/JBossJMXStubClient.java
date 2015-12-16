package com.fr.atgext.management.client;

import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

public class JBossJMXStubClient extends GenericJMXStubClient {
  public JBossJMXStubClient() {
    super();
  }
  
  public MBeanServerConnection getMbeanServerConnection () {
    try {
      InitialContext ctx;
      if (getServerUrl() == null)
      {
        ctx = new InitialContext();
      }
      else
      {
        Hashtable props = new Hashtable(System.getProperties());
        props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory"); 
        props.put("java.naming.provider.url", this.getServerUrl());
        ctx = new InitialContext(props);
      }
      
      if (!ClientUtils.isBlankString(getUser())) {
        SecurityAssociation.setPrincipal(new SimplePrincipal(getUser()));
        SecurityAssociation.setCredential(getPassword());
      }
  
      if (getAdapterName() == null)
      {
        setAdapterName("jmx/invoker/RMIAdaptor");
      }
  
      Object obj = ctx.lookup(getAdapterName());
      ctx.close();
  
      if (!(obj instanceof RMIAdaptor))
      {
        throw new ClassCastException("Object not of type: RMIAdaptorImpl, but: " + ((obj == null) ? "not found" : obj.getClass().getName()));
      }
  
      return ((MBeanServerConnection)obj);
    } catch (NamingException ne) {
      ne.printStackTrace(System.err);
    }
    return null;
  }
}

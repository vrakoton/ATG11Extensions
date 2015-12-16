/**
 * @author vrakoton
 * @version 1.0
 * 
 * Description :
 * 
 * This class registers all content repository to the mbean server. To do so,
 * We reference the content repository registry service (/atg/registry/ContentRepositories)
 * and loop on the initial repositories to register a delegate on the MBean server.
 */
package com.fr.atgext.management.repository;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.fr.atgext.management.GenericDelegateRegistrationService;

import atg.adapter.gsa.GSARepository;
import atg.adapter.secure.GenericSecuredMutableContentRepository;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;
import atg.repository.Repository;
import atg.repository.nucleus.RepositoryRegistryService;

public class ContentRepositoryRegistrationService extends GenericDelegateRegistrationService {
  RepositoryRegistryService mContentRepositoryRegistry;
  
  
  /*
   * Starts the service. We look at all inital repositories on the /atg/registry/Contentrepositories
   * service and create the corresponding MBean in the MBean Server
   *  
   * @see atg.nucleus.GenericService#doStartService()
   */
  public void doStartService()
    throws ServiceException
  {
    if (getContentRepositoryRegistry() == null) {
      throw new ServiceException("The content repository registry can not be null, please correct the configuration file");
    }
    
    if (getMbeanServer() == null) {
      throw new ServiceException("The MBean server can not be null");
    }
    
    Repository [] repositories = getContentRepositoryRegistry().getInitialRepositories();
    
    if (repositories != null && repositories.length > 0) {
      for (int i = 0; i < repositories.length; i++) {
        RepositoryDelegate delegate = new RepositoryDelegate();
        if (repositories[i] instanceof GenericSecuredMutableContentRepository) {
          delegate.setReferencedRepository((GSARepository)((GenericSecuredMutableContentRepository)repositories[i]).getRepository());
        } else if (repositories[i] instanceof GSARepository) {
          delegate.setReferencedRepository((GSARepository)repositories[i]);
        }
      
        try {
          ObjectName objName = createObjectName(((GenericService)repositories[i]).getAbsoluteName());
          // --- check that this object has not been registered already
          if (!getMbeanServer().isRegistered(objName)) {
            getMbeanServer().registerMBean(delegate, objName);
          } else {
            if (isLoggingInfo())
              logInfo("Can not register repository with name " + ((GenericService)repositories[i]).getAbsoluteName() + ", another MBean is " +
              		"already registered under this name");
          }
        } catch (NotCompliantMBeanException ncbe) {
          throw new ServiceException(ncbe);
        } catch (Exception exc) {
          throw new ServiceException(exc);
        }
      }
    } else {
      if (isLoggingInfo())
        logInfo("No repository registered in the content repository registry");
    }
  }
  
  
  
  public RepositoryRegistryService getContentRepositoryRegistry() {
    return mContentRepositoryRegistry;
  }
  public void setContentRepositoryRegistry(
      RepositoryRegistryService pContentRepositoryRegistry) {
    mContentRepositoryRegistry = pContentRepositoryRegistry;
  }
}

/**
 * @author vrakoton@re-enter.fr
 * @version 1.0
 * 
 * 
 */
package com.fr.atgext.management.epub;

import atg.deployment.agent.DeploymentAgent;
import atg.deployment.common.Status;
import atg.nucleus.GenericService;

public class DeploymentAgentInfo extends GenericService {
  DeploymentAgent mDeploymentAgent;
  
  /**
   * Returns the status of the deployment agent
   * @return
   */
  public String getAgentStatus () {
    if (getDeploymentAgent() != null) {
     Status status = getDeploymentAgent().getStatus();
     return status.getStateString();
    }
    return "UNKNOWN";
  }
  
  /**
   * Returns the target snapshot
   * @return
   */
  public String getDeploymentSnapshot() {
    if (getDeploymentAgent() != null) {
     Status status = getDeploymentAgent().getStatus();
     return status.getDeployedSnapshot();
    }
    return "NULL";
  }

  public DeploymentAgent getDeploymentAgent() {
    return mDeploymentAgent;
  }
  public void setDeploymentAgent(DeploymentAgent deploymentAgent) {
    mDeploymentAgent = deploymentAgent;
  }
  
}

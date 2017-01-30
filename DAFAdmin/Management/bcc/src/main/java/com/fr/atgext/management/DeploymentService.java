package com.fr.atgext.management;

import java.util.HashMap;
import java.util.Map;

import atg.deployment.common.DeploymentException;
import atg.deployment.server.Deployment;
import atg.deployment.server.DeploymentServer;
import atg.deployment.server.Target;
import atg.nucleus.GenericService;

public class DeploymentService
  extends GenericService {

  DeploymentServer deploymentServer;

  public DeploymentServer getDeploymentServer() {
    return deploymentServer;
  }

  public void setDeploymentServer(DeploymentServer deploymentServer) {
    this.deploymentServer = deploymentServer;
  }

  public String getDeploymentStatus() {
    String deploymentStatus = "NO_DEPLOYMENTS_RUNNING";
    try {
      Target targets[] = getDeploymentServer().getTargets();
      for (int idx = 0; targets != null && idx < targets.length; idx++) {
        Target target = targets[idx];
        Deployment deployment = target.getCurrentDeployment();
        if (deployment != null) {
          deploymentStatus = deployment.getStatus().getStateString();
        }
      }
    } catch (Exception ex) {
      deploymentStatus = "ERROR";
    }

    return deploymentStatus;
  }
  
  /**
   * returns the state of deployment on each target
   * @return
   * @throws DeploymentException
   */
  public Map<String, Boolean> getHalted() throws DeploymentException {
	  Map<String, Boolean> res = new HashMap<String,Boolean>();
	  Target [] targets = getDeploymentServer().getTargets();
	  if (targets == null) return res;
	  for(Target t : targets) {
		  res.put(t.getName(), Boolean.valueOf(t.isHalted()));
	  }
	  return res;
  }

  public String haltAllTargets() {
    String status = "START";
    try {
      Target targets[] = getDeploymentServer().getTargets();
      for (int idx = 0; targets != null && idx < targets.length; idx++) {
        Target target = targets[idx];
        target.haltQueue();
        status = "SUCCESS";
      }

    } catch (Exception ex) {
      status = "ERROR";
    }
    return status;
  }

  public String resumeAllTargets() {
    String status = "START";
    try {
      Target targets[] = getDeploymentServer().getTargets();
      for (int idx = 0; targets != null && idx < targets.length; idx++) {
        Target target = targets[idx];
        target.resumeQueue();
        status = "SUCCESS";
      }
    } catch (Exception ex) {
      status = "ERROR";
    }
    return status;
  }

}

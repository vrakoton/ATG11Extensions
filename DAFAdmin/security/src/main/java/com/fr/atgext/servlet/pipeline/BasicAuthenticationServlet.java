package com.fr.atgext.servlet.pipeline;

import java.io.IOException;

import javax.servlet.ServletException;

import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.pipeline.BasicAuthenticationPipelineServlet;

/**
 * 
 * @author vrakoton
 * @version $Rev$
 * 
 * Description:
 * 
 * This is an override of the original class which catches @see {@link PropertyChangeNotAllowedException} and returns 403
 * http code to the connected user.
 *
 */
public class BasicAuthenticationServlet extends
    BasicAuthenticationPipelineServlet {
  
  public void service (DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse)
     throws IOException, ServletException
  {
    if (isLoggingDebug())
      logDebug("Path info is: " + pRequest.getPathInfo());
    
    try {
      super.service(pRequest, pResponse);
    } catch (PropertyChangeNotAllowedException pce) {
      pResponse.sendError(403, pce.getMessage());
    }
  }
}

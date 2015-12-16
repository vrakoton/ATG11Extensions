package com.fr.atgext.servlet.pipeline;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import atg.core.util.StringUtils;
import atg.security.LoginUserAuthority;
import atg.security.Persona;
import atg.security.ThreadSecurityManager;
import atg.security.User;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;
import atg.servlet.pipeline.InsertableServletImpl;

public class AdminLoggingServlet extends InsertableServletImpl {
	boolean mAdminLogEnabled = true;
	boolean mPrintHeaders = false;
	List<String> mNoLogExtensions;
	List<String> mNoLogUsers;
	LoginUserAuthority mUserAuthority;
	
	public void service(DynamoHttpServletRequest pRequest, DynamoHttpServletResponse pResponse)
		throws ServletException, IOException
	{
		User user = ThreadSecurityManager.currentUser();
		
		if (user == null) {
			if (isLoggingDebug())
				logDebug("User probably not logged in");
			passRequest(pRequest, pResponse);
			return;
		}
		
		if (getNoLogExtensions() != null && !getNoLogExtensions().isEmpty()) {
			Iterator<String> it = getNoLogExtensions().iterator();
			while (it.hasNext()) {
				if (pRequest.getPathInfo().toUpperCase().endsWith("." + it.next().toUpperCase())) {
					passRequest(pRequest, pResponse);
					return;
				}
			}
		}
		
		if (isAdminLogEnabled()) {
			Persona pers = user.getPrimaryPersona(getUserAuthority());
			
			String userName = (user == null) ? "null" : pers.getName();
			
			// --- check if logs about this user should be sent to admin log
			if (getNoLogUsers() != null && !getNoLogUsers().isEmpty() && !StringUtils.isBlank(userName)) {
				Iterator<String> it = getNoLogUsers().iterator();
				String name = null; 
				while(it.hasNext()) {
					name = it.next();
					if (name.equals(userName)) {
						passRequest(pRequest, pResponse);
						return;
					}
				}
			}
			
			logInfo("------------- User '" + userName + "' is accessing " + pRequest.getPathInfo() + " from "+ pRequest.getRemoteAddr() + "\n");
			Enumeration<String> headerNames = pRequest.getHeaderNames();
			String headerName = null;
			String headerValue = null;
			String paramName = null;
			String paramValue = null;
			if (isPrintHeaders()) {
				if (headerNames != null) {
					logInfo ("HEADERS ========\n");
					while (headerNames.hasMoreElements()) {
						headerName = headerNames.nextElement();
						headerValue = (pRequest.getHeader(headerName) == null) ? "null" : pRequest.getHeader(headerName) ;
						logInfo(headerName + ":  " + headerValue);
					}
					logInfo ("\nEND OF HEADERS ========\n");
				} else {
					logInfo("NO HEADER!\n\n");
				}
			}
			
			Enumeration <String>paramNames = pRequest.getParameterNames();
			if (paramNames != null) {
				logInfo ("GET PARAMETERS ========\n");
				while(paramNames.hasMoreElements()) {
					paramName = paramNames.nextElement();
					paramValue = (pRequest.getParameter(paramName) == null) ? "null" : pRequest.getParameter(paramName) ;
					logInfo(paramName + ":  " + paramValue);
				}
				logInfo ("\nEND OF PRAMETERS========\n");
			} else {
				logInfo("NO PARAMETER!\n\n");
			}
			logInfo("------------- END of request\n\n");
		}
		passRequest(pRequest, pResponse);
	}
	
	public boolean isAdminLogEnabled() {
		return mAdminLogEnabled;
	}
	public void setAdminLogEnabled(boolean mAdminLogEnabled) {
		this.mAdminLogEnabled = mAdminLogEnabled;
	}

	public boolean isPrintHeaders() {
		return mPrintHeaders;
	}

	public void setPrintHeaders(boolean mPrintHeaders) {
		this.mPrintHeaders = mPrintHeaders;
	}

	public List<String> getNoLogExtensions() {
		return mNoLogExtensions;
	}

	public void setNoLogExtensions(List<String> pNoLogExtensions) {
		mNoLogExtensions = pNoLogExtensions;
	}
	
	public LoginUserAuthority getUserAuthority() {
		return mUserAuthority;
	}

	public void setUserAuthority(LoginUserAuthority mUserAuthority) {
		this.mUserAuthority = mUserAuthority;
	}

	public List<String> getNoLogUsers() {
		return mNoLogUsers;
	}

	public void setNoLogUsers(List<String> mNoLogUsers) {
		this.mNoLogUsers = mNoLogUsers;
	}
}

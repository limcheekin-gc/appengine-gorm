/**
 * 
 */
package com.vobject.appengine;

import java.util.HashMap;
import java.util.Map;

import com.google.apphosting.api.ApiProxy.Environment;

/**
 * @author limcheekin
 *
 */
public class AppEngineTestEnvironment implements Environment {

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#getAppId()
	 */
	@Override
	public String getAppId() {
		return "test";
	}

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#getAttributes()
	 */
	@Override
	public Map<String, Object> getAttributes() {
		return new HashMap<String, Object>();
	}

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#getAuthDomain()
	 */
	@Override
	public String getAuthDomain() {
		throw new UnsupportedOperationException();

	}

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#getEmail()
	 */
	@Override
	public String getEmail() {
		throw new UnsupportedOperationException();

	}

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#getRequestNamespace()
	 */
	@Override
	public String getRequestNamespace() {
		return "";
	}

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#getVersionId()
	 */
	@Override
	public String getVersionId() {
		// TODO Auto-generated method stub
		return "1.0";
	}

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#isAdmin()
	 */
	@Override
	public boolean isAdmin() {
		throw new UnsupportedOperationException();

	}

	/* (non-Javadoc)
	 * @see com.google.apphosting.api.ApiProxy.Environment#isLoggedIn()
	 */
	@Override
	public boolean isLoggedIn() {
		throw new UnsupportedOperationException();

	}

}

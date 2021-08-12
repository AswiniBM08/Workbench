package com.ibm.safr.we.model;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import junit.framework.TestCase;

import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.TestDataLayerHelper;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.security.UserSession;

public class TestUserSession extends TestCase {
	TestDataLayerHelper helper = new TestDataLayerHelper();

	public void dbStartup() {
		helper.initDataLayer();
	}
	
	public void tearDown() {
		helper.closeDataLayer();
	}
	

	public void testGetCurrentEnvironment() {
		dbStartup();
		// test for the admin user.
		User user = null;
		Environment environment = null;
		try {
			user = SAFRApplication.getSAFRFactory().getUser("ADMIN");
			environment = SAFRApplication.getSAFRFactory().getEnvironment(1);
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		UserSession userSession = new UserSession(user, environment, null);
		try {
			SAFRApplication.setUserSession(userSession);
		} catch (DAOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertEquals(environment, SAFRApplication.getUserSession()
				.getEnvironment());

	}

	public void testLoggedInUser() {
		dbStartup();
		// test for the admin user.
		User user = null;
		Environment environment = null;
		try {

			user = SAFRApplication.getSAFRFactory().getUser("ADMIN");
			environment = SAFRApplication.getSAFRFactory().getEnvironment(1);
		} catch (SAFRException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		UserSession userSession = new UserSession(user, environment, null);
		try {
			SAFRApplication.setUserSession(userSession);
		} catch (DAOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertEquals(user, SAFRApplication.getUserSession().getUser());
	}

}

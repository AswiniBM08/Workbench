package com.ibm.safr.we.constants;

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


/**
 * This enum is used in the find replace logic text utility.It is used for
 * refining the search according to created and modified status of the view.
 * 
 */
public enum SearchCriteria {
	None("No"), SearchCreated("CD"), SearchModified("MD");
	private String typeValue;

	SearchCriteria(String typeValue) {
		this.typeValue = typeValue;
	}

	/**
	 * This method is used to get the {@link String} value of the type of the
	 * {@link SearchCriteria} selected which is defined in the enum.
	 * 
	 * @return the value of the type of {@link SearchCriteria}.
	 */
	public String getTypeValue() {
		return typeValue;
	}
}

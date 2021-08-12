package org.genevaers.sycadas;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.genevaers.sycadas.dataprovider.SycadaDataProvider;
import org.genevaers.sycadas.grammar.ExtractColumnBaseListener;
import org.genevaers.sycadas.grammar.ExtractColumnParser;


public class ExtractColumnDependencyListener extends ExtractColumnBaseListener {

	private Set<Integer> pfAssocIDs = new HashSet<>();
	private Set<Integer> writeExits = new HashSet<>();
	private Set<Integer> fields = new HashSet<>();
	private Map<Integer, List<Integer>> lookups = new HashMap<>();
	private SycadaDataProvider dataProvider;
	private List<String> errors = new ArrayList<>();
	
@Override
	public void enterLookupRef(ExtractColumnParser.LookupRefContext ctx) {
		StringBuilder sb;
		if (ctx.getChildCount() > 1) {
		    sb = new StringBuilder(ctx.getChild(1).getText());
		} else {
			sb = new StringBuilder(ctx.getText());
   			sb.deleteCharAt(0);
			sb.deleteCharAt(sb.length()-1);
		}	
		String fullName = sb.toString();
		System.out.println("Found lookupRef " + fullName);
		Integer lookupID = dataProvider.findLookupID(fullName);
		if(lookupID != null) {
				lookups.put(lookupID, null);
		} else {
			errors.add("Lookup " + ctx.getText() + " not found");			
		}

	}

	@Override
	public void exitLookupRef(ExtractColumnParser.LookupRefContext ctx) {
		// TODO Auto-generated method stub
		

	}

	public void enterFieldRef(ExtractColumnParser.FieldRefContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitFieldRef(ExtractColumnParser.FieldRefContext ctx) {
		StringBuilder sb;
		if (ctx.getChildCount() > 1) {    
			sb = new StringBuilder(ctx.getChild(2).getText());
		} else {
			sb = new StringBuilder(ctx.getText()); 
		}
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length()-1);
		String fieldName = sb.toString();
		Integer fieldID = dataProvider.findLRFieldID(fieldName);
		if(fieldID != null) {
			fields.add(fieldID);
		} else {
			errors.add("Yo man, we need to let the world know that field " + ctx.getText() + " ain't cool");
		}
	}

	@Override
	public void enterLookupField(ExtractColumnParser.LookupFieldContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitLookupField(ExtractColumnParser.LookupFieldContext ctx) {
		StringBuilder sb = new StringBuilder(ctx.getChild(1).getText());
		String fullName = sb.toString();
		System.out.println("Found lookup field " + fullName);
		String[] parts = fullName.split("\\.");
		System.out.println("Lookup name " + parts[0]);
		System.out.println("Field " + parts[1]);
		Integer lookupID = dataProvider.findLookupID(parts[0]);
		if(lookupID != null) {
			List<Integer> fieldList = lookups.get(lookupID);
			if(fieldList == null) {
				fieldList = new ArrayList<Integer>();
				lookups.put(lookupID, fieldList);
				Integer lkfield = dataProvider.findLookupFieldID(lookupID, parts[1]);
				if(lkfield != null) {
					fieldList.add(lkfield);
				} else {
					errors.add("Lookup " + parts[0] + " does not reference field " + parts[1]);								
				}
			}
		} else {
			errors.add("Lookup " + ctx.getText() + " not found");			
		}
	}

	@Override
	public void enterWriteExit(ExtractColumnParser.WriteExitContext ctx) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exitWriteExit(ExtractColumnParser.WriteExitContext ctx) {
		StringBuilder sb = new StringBuilder(ctx.getText());
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length()-1);
		Integer exitID = dataProvider.findExitID(sb.toString());
		if(exitID != null) {
			writeExits.add(exitID);
		} else {
			errors.add("Write Exit " + ctx.getText() + " not found");						
		}
	}
	
	@Override 
	public void exitFile(ExtractColumnParser.FileContext ctx) { 
		StringBuilder sb = new StringBuilder(ctx.getText());
		sb.deleteCharAt(0);
		sb.deleteCharAt(sb.length()-1);
		String fullName = sb.toString();
		String[] parts = fullName.split("\\.");
		System.out.println("LF name " + parts[0]);
		System.out.println("PF Name " + parts[1]);
		Integer pfID = dataProvider.findPFAssocID(parts[0], parts[1]);
		if(pfID != null) {
			pfAssocIDs.add(pfID);
		} else {
			errors.add("Output File " + ctx.getText() + " not found");						
		}
	}


	public void setDataProvider(SycadaDataProvider dataFromHere) {
		dataProvider = dataFromHere;	
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	public Set<Integer> getFieldIDs() {
		return fields;
	}

	public Map<Integer, List<Integer>> getLookupIDs() {
		return lookups;
	}

	public Set<Integer> getExitIDs() {
		return writeExits;
	}

	public Set<Integer> getLFPFAssocIDs() {
		return pfAssocIDs;
	}

}

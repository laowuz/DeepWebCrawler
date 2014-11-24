package com.dwim.index;

import java.util.ArrayList;

import com.dwim.form.analysis.IElement;

public class SearchResultRecord {
	public static ArrayList<IElement> ielements;
	public ArrayList<ArrayList<String>> values;
	

	public SearchResultRecord() {
		if(ielements == null)
			ielements = IElement.getALLIElements();
		
		values = new ArrayList<ArrayList<String>>(ielements.size());
		for (int i = 0 ; i < ielements.size() ; i++) {
			values.add(new ArrayList<String> (1));
		}
		
		
	}
	
	public static void setElements(ArrayList<IElement> es) {
		ielements = es;
		
	}
	
	/**
	 * 
	 * @param ielementno id of infinite elements
	 * @param value
	 */
	public void loadValue(int ielementno, String value) {
		if(ielementno >= ielements.size() || ielementno <0 )	//problem - multi elements
			return;
	
		if(value == null)
			values.get(ielementno).add("");
		else {
			values.get(ielementno).add(value);
		}
			
	}
	
	public void loadValue(IElement ielement, String value) {
		if(ielement == null)
			return;
		if(ielement.getId() >= ielements.size() || ielement.getId() <0 ) //problem - multi elements
			return;
		if(value == null)
			values.get(ielement.getId()).add("");
		else {
			values.get(ielement.getId()).add(value);
		}
	}
	
	
	public void loadValues() {
		
	}
	
	public ArrayList<String> getValues(int ielementno) {
		//problem - multi elements
		if(ielementno >= ielements.size() || ielementno < 0 )
			return null;
		return values.get(ielementno);
	}
	
	public String genID() {
		return null;
	}
	
	public int numOfElements() {
		return ielements.size();
	}


	

	
	
}

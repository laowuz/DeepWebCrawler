package com.dwim.form.analysis;

public class FElement extends Element{
	private String[] domainValues;
	
	public FElement(String eleName,String[] values) {
		super(eleName);
		domainValues = values; 
	}
	

	public String[] getDomainValues() {
		return domainValues;
	}

}

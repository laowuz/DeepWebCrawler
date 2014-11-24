package com.dwim.form.analysis;

import java.util.ArrayList;

public class IElement extends Element {
	
	private static ArrayList<IElement> elements = new ArrayList<IElement> (8);
	
	public IElement(String eleName) {
		super(eleName);
		elements.add(this);
	}
	
	public static ArrayList<IElement> getALLIElements() {
		return elements;
	}
	

	
	public static int ielementsNum() {
		return elements.size();
	}
}

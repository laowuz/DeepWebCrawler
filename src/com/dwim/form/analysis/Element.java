package com.dwim.form.analysis;

import java.util.ArrayList;

public abstract class Element {
	
	private int id;
	private String elementName;
	private static int counter = 0;
	private static ArrayList<Element> elements = new ArrayList<Element> (8);
	
	
	public Element(String eleName) {
		elementName = eleName;
		id = counter++;
		elements.add(this);
	}
	
	public int getId() {
		return id;
	}
	
	public static Element getByName(String name) {
		return null;
	}
	
	public static Element getByID(int id) {
		if(id < 0 || id > elements.size() -1)	return null;
		return elements.get(id);
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	public static int elementsNum() {
		return elements.size();
	}
	
	
	
	
}

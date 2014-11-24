package com.dwim.form.analysis;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.dwim.kv.policy.Keyword;
import com.dwim.util.DWIMException;


public class EP {
	public static int id;
	private int tid;
	private ArrayList<Element> elements;
	
	public EP(ArrayList<Element> input) throws DWIMException {
		if(input == null)
			throw new DWIMException("The input elements are null");
		elements = input;
	}
	
	public EP(Element input) throws DWIMException {
		if(input == null)
			throw new DWIMException("The input element is null");
		elements = new ArrayList<Element>(1);
		elements.add(input);
	}

	/**
	 * 
	 * @return the number of form elements in the executable pattern
	 */
	public int numOfElement() {
		return elements.size();
	}
	
	public Element getIthElement(int i) {
		return  elements.get(i);	
	}
	
	public Element getElement(FElement e) {
		if(e == null)	return null;
		for(int i = 0 ; i < elements.size() ; i++) {
			if(e.equals(elements.get(i)))
				return elements.get(i);
		}
		return  elements.get(id);	
	}
	
	/**
	 * Assign the values to the corresponding elements in the EP
	 * @param values the values to assign
	 * @return the encode URL with the assigned value
	 */
	public String assignValue(ArrayList<String> values)  {
		if(elements.size() < 1 || values.size() != elements.size())	return null;
		String url = null;
		try {
			url = "?"+elements.get(0).getElementName() + "=" + URLEncoder.encode(values.get(0),"utf-8");
		 
			for(int i = 1 ; i < elements.size() ; i++) 
				url += "&" + elements.get(0).getElementName() + "=" + URLEncoder.encode(values.get(i),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}
	
	public String assignKeywords1(ArrayList<Keyword> words)  {
		if(elements.size() < 1 || words.size() != elements.size())	return null;
		String url = null;
		try {
			url = "?"+elements.get(0).getElementName() + "=" + URLEncoder.encode(words.get(0).getWord(),"utf-8");
		 
			for(int i = 1 ; i < elements.size() ; i++) 
				url += "&" + elements.get(0).getElementName() + "=" + URLEncoder.encode(words.get(i).getWord(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}
	
	public String assignValue1(String value)  {
		if(elements.size() < 1 || 1 != elements.size())	return null;
		String url = null;
		try {
			url = "?"+elements.get(0).getElementName() + "=" + URLEncoder.encode(value,"utf-8");
		 
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}
	
	public int getID() {
		return tid;
	}
	
	public boolean containIElement() {
		boolean result = false;
		for(int i = 0 ; i < elements.size() ; i++) {
			if(elements.get(i) instanceof IElement) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public boolean isIEP() {
		return containIElement();
	}
	
	public boolean isFEP() {
		return !containIElement();
	}
	
	public boolean containFElement() {
		boolean result = false;
		for(int i = 0 ; i < elements.size() ; i++) {
			if(elements.get(i) instanceof FElement) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public boolean equals(Object epin) {
		if(epin == null || !(epin instanceof EP))
			return false;
		if(((EP) epin).getID() == this.tid)
			return true;
		else
			return false;
	}
	
	public ArrayList<FElement> getAllFElements() {
		ArrayList<FElement> result = new ArrayList<FElement>(elements.size());
		for(int i = 0 ; i < elements.size() ; i++) {
			if(elements.get(i) instanceof FElement) 
				result.add((FElement) elements.get(i));
		}
		return result;
	}
	
	public ArrayList<Element> getAllElements() {
		ArrayList<Element> result = new ArrayList<Element>(elements.size());
		for(int i = 0 ; i < elements.size() ; i++) {
				result.add(elements.get(i));
		}
		return result;
	}
	
}

package com.dwim.wrapper;

import java.util.ArrayList;

import com.dwim.index.SearchResultRecord;

public interface IWrapper {
	
	
	/**
	 * 
	 * @param searchResultPage
	 * @return
	 */
	public ArrayList<String> getRecordEntries(String searchResultPage) throws Exception;
	
	
	/**
	 * 
	 * @param recordPage
	 * @return
	 */
	public SearchResultRecord extract(String recordPage) throws Exception;
	
	
	
}

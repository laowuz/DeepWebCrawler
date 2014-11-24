package com.dwim.main;

import com.dwim.form.parser.PageRetrievalManager;
import com.dwim.index.IndexManager;

public class SampleCrawler {
	private PageRetrievalManager prmanager;
	private IndexManager imanager;
	
	
	
	
	public SampleCrawler() {
		this.config();
		try {
			prmanager = new PageRetrievalManager();
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void config() {
		
	}
	
	public static void main(String args[]) {
		
	}
}

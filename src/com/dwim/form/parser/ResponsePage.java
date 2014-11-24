package com.dwim.form.parser;

import java.util.ArrayList;

import com.dwim.index.SearchResultRecord;

public class ResponsePage {
	
	public String content;
	private ArrayList<SearchResultRecord> srrs;
	
	public ResponsePage(String pageContent) {
		content = pageContent;
	}
	
}

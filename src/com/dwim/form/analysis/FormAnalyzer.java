package com.dwim.form.analysis;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dwim.util.URLDistance;

public class FormAnalyzer {
	
	//assign each IMEP a ID 
	
	
	
	public int numofIMEP() {
		return 0;
	}
	
	
	
	
	
	
	
	
	
	public Element getPageElement(String searchResultPage,String queryURL) throws MalformedURLException {
		URL url = new URL(queryURL);
		String protocal = url.getProtocol();
		Matcher urlMatcher = Pattern.compile(protocal+"://").matcher(searchResultPage);
		ArrayList<String> urls = new ArrayList<String>(64); 
		while(urlMatcher.find()) {
			urls.add(urlMatcher.group());
		}
		double[] sims = new double[urls.size()];
		double simMax = 0.0d;
 		for(int i = 0 ; i < urls.size() ; i++) {
			sims[i] = URLDistance.similar(queryURL,urls.get(i));
			if(sims[i] > simMax) 
				simMax = sims[i];
		}
 		for(int i = 0 ; i < urls.size() ; i++) {
 			
 		}
		
		return null;
		
	}
	
	
	public Element[] getElements(String in) throws MalformedURLException {
		URL  url = new URL(in);
		return null;
		
	}
}

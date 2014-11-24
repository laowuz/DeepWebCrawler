package com.dwim.form.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.dwim.form.analysis.EP;
import com.dwim.form.analysis.FElement;
import com.dwim.kv.policy.Keyword;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;

/**
 * Query format
 * query->ielement{0,1},felement* as the pattern format
 * @author JL
 *
 */
public class Query {
	private EP pattern;
	private Keyword keyword;
	private int[] boudaries;
	private int[] ids;

	
	private Query() {}

	public Query(EP pat, Keyword keyword) throws DWIMException {
		if(pat ==null || pat.numOfElement() < 1 || !pat.isIEP() || keyword == null)
			throw new DWIMException("keyword does not match the pattern");
		this.keyword = keyword;
		this.pattern = pat;
		if(pattern.containFElement()) {
			boudaries = new int[pattern.numOfElement()-1];
			ids = new int[pattern.numOfElement()-1];
			for(int k = 0 ; k < boudaries.length ; k++) {
				boudaries[k] = ((FElement)pattern.getIthElement(k+1)).getDomainValues().length;	//skip the ielement
				ids[k] = 0;
			}
			ids[ids.length-1] = -1;
		} else {
			//IMEP
			boudaries = new int[1];
			ids = new int[1];
			boudaries[0] = 1;
			ids[0] = -1;
		}
		
	}
	
	
	public EP getPattern() {
		return pattern;
	}
	
	public void setPattern(EP pattern) {
		this.pattern = pattern;
	}
	
	public Keyword getKeyword() {
		return keyword;
	}
	
	public String next() {
		ids[ids.length-1]++;
		int j = ids.length-1;
		while(ids[j] >= boudaries[j]) {
			//carry
			ids[j] = 0;
			ids[j-1]++;
			j--;
		}
		return makeUrl();
	}
	
	private String next1() {
		ids[ids.length-1]++;
		int j = ids.length-1;
		while(ids[j] >= boudaries[j]) {
			//carry
			ids[j] = 0;
			ids[j-1]++;
			j--;
		}
		String r ="";
		for(int i = 0 ; i < ids.length ; i++) {
			r += ids[i] +" ";
		}
		return r;
	}
	
	public boolean hasNext() {
		for(int i = 0 ; i < ids.length ; i++) {
			if(ids[i] != boudaries[i]-1)
				return true;
		}
		return false;
		

	}
	
	public String genURL() {
		return null;
	}
	
	private String makeUrl() {
		String url = null;
		try {
			url = "?"+pattern.getIthElement(0).getElementName() + "=" + URLEncoder.encode(keyword.getWord(),ConfigMan.URL_CONTENT_CODESET);
		 
			for(int i = 1 ; i < pattern.numOfElement() ; i++) {
				FElement fe = (FElement) pattern.getIthElement(i);
				url += "&" + fe.getElementName() + "=" + URLEncoder.encode(fe.getDomainValues()[ids[i-1]],ConfigMan.URL_CONTENT_CODESET);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;

	}
	

	
	private void setBoudaries(int[] boudaries) {
		this.boudaries = boudaries;
	}


	private void setIds(int[] ids) {
		this.ids = ids;
	}
	
	public static void main(String args[]) throws Exception {
		Query q = new Query();
		q.setBoudaries(new int[]{2,2,10});
		q.setIds(new int[]{0,0,-1});
		
		int c = 0;
		while(q.hasNext()) {
			System.out.println(q.next1());
		}
		

	}


	
	
	
	
	
	
}

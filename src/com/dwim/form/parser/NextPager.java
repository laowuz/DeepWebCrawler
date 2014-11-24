package com.dwim.form.parser;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.dwim.util.ConfigMan;

public class NextPager implements ITokenGenerator {
	public static final int GET_METHOD = 1, POST_METHOD = 2;
	public static int METHOD_TYPE;
	private String url;
	private String pageElement;
	private int stepLength;
	private int pn;
	private HttpMethodParams para;
	private boolean ttr;						//termination token received
	private int maxPageOffset;

	

	
	public NextPager(String thisURL) {
		para = new HttpMethodParams();
		para.setContentCharset(ConfigMan.HTTP_CONTENT_CODESET);
		url = thisURL;
		pn = 1;
		ttr = false;
		METHOD_TYPE = GET_METHOD;
		maxPageOffset = 100000;
	}
	
	public void initialize(String pageElement, int stepLength) {
		this.pageElement = pageElement;
		this.stepLength = stepLength;
	}
	
	public synchronized HttpMethod nextToken() {
	
		if(ttr)	return null;
		HttpMethod token = null;
		//no page solve
		if(pageElement == null)	{
			token = new GetMethod();
			token.setParams(para);
			token.setFollowRedirects(true);
			token.setPath(url);
			//only one page need to retrieve
			ttr = true;
		}
		else if(METHOD_TYPE == GET_METHOD) {
			if(pn > maxPageOffset) {
				//reach the maximum offset
				ttr = true;
				return null;
			}
			token = new GetMethod();
			token.setParams(para);
			token.setFollowRedirects(true);
			token.setPath(url + "&" + pageElement + "=" + pn);
			pn += stepLength;
		}
		else if(METHOD_TYPE == POST_METHOD) {
			System.err.println("not implemented!");
		}
		else {
			token = null;
		}
		return token;
		
	}
	
	public void terminate() {
		ttr = true;
	}

	public boolean isTerminate() {
		return ttr;
	}

	/**
	 * @deprecated
	 */
	public synchronized HttpMethod nextToken(String para) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMaxPageOffset(int maxPageOffset) {
		this.maxPageOffset = maxPageOffset;
	}
}

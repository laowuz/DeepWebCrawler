package com.dwim.form.parser;

import org.apache.commons.httpclient.HttpMethod;

public interface ITokenGenerator {
	
	public HttpMethod nextToken();
	
	public HttpMethod nextToken(String para);
	
}

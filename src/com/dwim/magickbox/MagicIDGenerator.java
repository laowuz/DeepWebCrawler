package com.dwim.magickbox;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.lucene.index.CorruptIndexException;

import com.dwim.form.parser.ITokenGenerator;
import com.dwim.index.IDIndexer;
import com.dwim.util.ConfigMan;

public class MagicIDGenerator implements ITokenGenerator{
	private String prefix;
	/**
	 * 
	 */
	private String postfix;
	
	private HttpMethodParams para;
	
	private String[] vcomponents;
	private char[] idvalues;
	private char[] basevalues;
	private int[] types;

	private int lastVariantIndex;
	
	private int pointer;

	protected MagicIDGenerator(String[] vcomponents, int[] types, String prefix, String postfix) throws IOException {
		para = new HttpMethodParams();
		para.setContentCharset(ConfigMan.HTTP_CONTENT_CODESET);
	
		this.types = types;
		this.vcomponents = vcomponents;
		this.prefix = prefix;
		this.postfix = postfix;
		
	}
	

	
	public void turnOn() {
		if(this.types == null || vcomponents == null || vcomponents.length < 1)	return;
		idvalues = new char[types.length];
		basevalues = new char[types.length];
	
		

		//initialize the id buffer
		for(int i = 0 ; i < idvalues.length ; i++) {
		
			switch(types[i]) {
				case 1: 		
					basevalues[i] = vcomponents[0].charAt(i);
					break;
				case MagicID.DIGITAL: 	
					basevalues[i] = '0';
					break;
				case MagicID.LETTER: 
					basevalues[i] = 'a';
					break;
				case MagicID.ASCII: 	
					basevalues[i] = 0;
					break;
				default: 		
					basevalues[i] = 0;
					break;
			}
		}
		
		//find the last variant index
		int k = types.length - 1;
		for( ; k >=0 ; k--) {
			if(types[k] != 1)
				break;
		}
		lastVariantIndex = k;
		
		this.resetLowBits(-1);
		
	}
	
	protected String nextUrl() {
		if(idvalues == null || types == null || prefix == null || postfix ==null)	
			return null;
		
	
		int step = 1;
		
		//skip calculation
		if(MagicRetriver.missCount < 4) {
			step = 1;
		} else if(MagicRetriver.missCount < 8) {
			step = 2;
		} else if(MagicRetriver.missCount < 12) {
			step = 4;
		} else {
			step = 8;
		}
		
		//boundary check
		if(idvalues[pointer] + step >= basevalues[pointer] + types[pointer]) {
			//carry
			movePointerForward();
			if(pointer < 0)	return null;
			while(idvalues[pointer] + 1 >= basevalues[pointer] + types[pointer]) {
				movePointerForward();
				if(pointer < 0)	return null;
			}
			idvalues[pointer] = (char) (idvalues[pointer]+1);
			resetLowBits(pointer);	
		} else {
			//in position
			idvalues[pointer] = (char) (idvalues[pointer]+step);
		}

		
		return prefix + new String(idvalues) + postfix;
			
		
		
	}
	
	private void resetLowBits(int si) {
		//reset the lowest bit
		for(int i = idvalues.length-1 ; i > si ; i--) {
			idvalues[i] = basevalues[i];
		}
		pointer = lastVariantIndex;
	}
	
	

	
	private void movePointerForward() {
		pointer--;
		while(pointer >= 0 && types[pointer] == 1) {
			pointer--;
		}
	}
	

	
	public synchronized HttpMethod nextToken() {
	
		HttpMethod token;
		token = new GetMethod();
		token.setParams(para);
		token.setFollowRedirects(true);
		String url = nextUrl();
		if(url == null)	return null;
		token.setPath(url);
		//record the miss hit
	
		return token;
	
	}

	/**
	 * @deprecated
	 */
	public synchronized HttpMethod nextToken(String para) {
		return this.nextToken();
	}



	public String getPrefix() {
		return prefix;
	}



	public String getPostfix() {
		return postfix;
	}





	
	
	

}

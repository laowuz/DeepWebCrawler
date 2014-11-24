package com.dwim.form.parser;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.lucene.index.CorruptIndexException;

import com.dwim.index.IDIndexer;
import com.dwim.util.ConfigMan;

public class NextRecorder implements ITokenGenerator {
	private int samplePoint;
	private IDIndexer idindexer;
	private HttpMethodParams para;
	private int accessDocs, newDocs;
	

	
	public NextRecorder() throws IOException {
		this(ConfigMan.SRR_SAMPLING_RATE,ConfigMan.HTTP_CONTENT_CODESET);
	}
	
	public NextRecorder(double sampleRate,String contentCode) throws IOException {
		samplePoint = (int) (1.0d/sampleRate);
		para = new HttpMethodParams();
		para.setContentCharset(contentCode);
		idindexer = IDIndexer.getInstance();
		accessDocs = 0;
		newDocs = 0;
	}
	
	
	public synchronized HttpMethod nextToken(String url) {
		
		String furl = idindexer.format(url);
		accessDocs++;
	
		try {
			
			if(idindexer.exists(furl)) {
				return null;
			}
			if (idindexer.numOfIDs % samplePoint == 0){
				newDocs++;
				idindexer.index(furl);
				//crate a method to sample the record
				HttpMethod token = new GetMethod();
				token.setParams(para);
				token.setFollowRedirects(true);
				token.setPath(url);
				return token;
			} else {
				newDocs++;
				idindexer.index(furl);
				return null;
			}
		} catch (CorruptIndexException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public void commitIndex() throws CorruptIndexException, IOException {
		idindexer.commitIndex();
		idindexer.optimize();
	}
	
	public void markDocNumbers() {
		accessDocs = 0;
		newDocs = 0;
	}
	
	
	/**
	 * @deprecated
	 */
	public synchronized HttpMethod nextToken() {
		return null;
	

	}

	protected int getAccessDocs() {
		return accessDocs;
	}

	protected int getNewDocs() {
		return newDocs;
	}


	
	
}

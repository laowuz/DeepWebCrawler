package com.dwim.form.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

import com.dwim.index.SRRBufferPool;
import com.dwim.index.SearchResultRecord;
import com.dwim.util.ConfigMan;
import com.dwim.util.HTMLDocument;
import com.dwim.wrapper.IWrapper;

/**
 * This version does not support the cookies parallel access
 * @author JL
 *
 */
public class PageRetriever extends Thread {

	public static final int OK = 100, RUNNING = 200, LAST_PAGE = 300, END = 500,UNKNOW = 400;
	private static ArrayList<String> lastUrls;
	private static Pattern offsetPattern;
	
	
	private static int idcounter = 0;
	private int id;

	private HttpClient httpClient;
	private NextRecorder nextRecorder; 
	
	private HttpMethod method;
	private IWrapper wrapper;
	private int lastExeStaus;
	private int faultTimes;
	//optional
	
	

	

	public PageRetriever(HttpClient httpClient, IWrapper wrapper, NextRecorder recorder) throws IOException {
		id = idcounter++;
		this.httpClient = httpClient;
		this.wrapper = wrapper;
		nextRecorder = recorder; 	//revise
		faultTimes = 0;
		if(offsetPattern == null && ConfigMan.MAX_PAGE_OFFSET_REX != null) {
			offsetPattern = Pattern.compile(ConfigMan.MAX_PAGE_OFFSET_REX);
		}

	}

	public void run() {
		while(true) {
			try {

				if(method == null)	
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME);
				else {
					
					int thisExeStatus = RUNNING;
					if(ConfigMan.DEBUG) System.out.println("¡ý Retriver " + id + " is about to get something from " + method.getURI());
					// execute the method
					httpClient.executeMethod(method);
					//System.out.println(id + " - get executed");
					// get the response body as an array of bytes
					int statusCode = method.getStatusCode();
					//method.getResponseHeader("Cookies");
					String content = method.getResponseBodyAsString();
					ArrayList<String> urls = wrapper.getRecordEntries(content);	
					
					if(urls.size() > 0) {
						thisExeStatus = PageRetriever.OK;
						/*if(offsetPattern != null) {
							Matcher offset = offsetPattern.matcher(content);
							if(offset.find()) {
								
							}
						}*/
						
					//	to be improved (inaccuracy estimation)
					  if(this.getThreadID() == 0) {
							if(!sameUrls(urls)) {
								this.setLastUrls(urls);
							}
							else {
								thisExeStatus = PageRetriever.LAST_PAGE;
								lastExeStaus = thisExeStatus;
							}
						}
					  	//when the sampling is complete, release method
				
						
					} 
					else if(statusCode/100 == 2) {
						//Reach the end of the end of the record
						//Send a terminal token
						thisExeStatus = PageRetriever.LAST_PAGE;
						method.releaseConnection();
						method = null;
					}
					else {
						//Let alone improved by slide window(detect the failures)
						System.err.println("Please check your network. status code = " + statusCode);
						thisExeStatus = PageRetriever.UNKNOW;
						method.releaseConnection();
						method = null;
					}

					//index the result page url
					for(int i = 0 ; i < urls.size() ; i++) {
						HttpMethod token = nextRecorder.nextToken(urls.get(i));
						SearchResultRecord srr;
						if(token != null) {
							
							if(ConfigMan.EXPERIMENTAL_MODE) {
								String pageContent = HTMLDocument.getCachedPage(token.getPath());
								if(pageContent != null) {
								
									 srr = wrapper.extract(pageContent);
									 if(ConfigMan.DEBUG) System.out.print("from local chache ");
								} else {						
									httpClient.executeMethod(token);
									pageContent = token.getResponseBodyAsString();
									HTMLDocument.write(ConfigMan.SAVE_HTML_PATH, 
											HTMLDocument.formatName(token.getPath()), 
											ConfigMan.SAVE_HTML_CODESET, pageContent);
									 srr = wrapper.extract(pageContent);
										token.releaseConnection();
									if(ConfigMan.DEBUG) System.out.print("from the host ");
								}
							} else {
								//generic mode
								httpClient.executeMethod(token);
								String pageContent = token.getResponseBodyAsString();
								HTMLDocument.write(ConfigMan.SAVE_HTML_PATH, 
										HTMLDocument.formatName(token.getPath()), 
										ConfigMan.SAVE_HTML_CODESET, pageContent);
								srr = wrapper.extract(pageContent);
								token.releaseConnection();
								if(ConfigMan.DEBUG) System.out.print("from the host ");
							}
							boolean ir = SRRBufferPool.addSRR(srr);
							//looply detect whether the insertion is successfully in the next version
							if(ir == true);
						}
					}
					
					
					lastExeStaus = thisExeStatus;
					thisExeStatus = END;
					if(method != null)
						method.releaseConnection();
					method = null;
					faultTimes = 0;
					
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (HttpException e) {
				e.printStackTrace();
				faultTimes++;
				if(faultTimes < 6)
					continue;
				else
					return;
			} catch (IOException e) {
				e.printStackTrace();
				faultTimes++;
				if(faultTimes < 6)
					continue;
				else
					return;
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			
		}
	}

	protected void setToken(HttpMethod m) {
		
		method = m;
	}

	protected boolean hasToken() {
		return method != null;
	}
	
	
	protected synchronized boolean sameUrls(ArrayList<String> in) {
		if(in == null || lastUrls==null || lastUrls.size() != in.size())
			return false;
		else {
			for(int i = 0 ; i < lastUrls.size() ; i++) {
				if(!lastUrls.get(i).equals(in.get(i)))
					return false;
			}
		}
		return true;
	}
	
	protected synchronized void setLastUrls(ArrayList<String> in) {
		PageRetriever.lastUrls = in;
	}
	
	protected int getThreadID() {
		return id;
	}
	
	/**
	 * 
	 * @return
	 */
	protected int lastExeStatus() {
		return lastExeStaus;
	}
	
	


}

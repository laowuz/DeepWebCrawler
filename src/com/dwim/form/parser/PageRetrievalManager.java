package com.dwim.form.parser;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.lucene.index.CorruptIndexException;

import com.dwim.util.ConfigMan;
import com.dwim.wrapper.IWrapper;
import com.dwim.wrapper.WrapperFactory;


public class PageRetrievalManager extends Thread{
	
	protected NextPager task;
	private PageRetriever[] threadgroup;
	private HttpClient httpClient;
	private IWrapper wrapper;
	private NextRecorder recorder;
	

	
	/**
	 * 
	 * @param protocal
	 * @param url
	 * @param port
	 * @param wrapperName The nick name of a wrapper(transparent)
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public PageRetrievalManager(String protocal, String url, int port, String wrapperName, NextRecorder recorder) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		httpClient.getHostConfiguration().setHost(url, port, protocal);
		threadgroup = new PageRetriever[ConfigMan.THREAD_IN_USE];
		wrapper = WrapperFactory.getWrapper(wrapperName);
		this.recorder = recorder;
		this.initializeThread();
		task = null;
	}
	
	public PageRetrievalManager(String protocal, String url, int port, IWrapper wrapper, NextRecorder recorder) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		httpClient.getHostConfiguration().setHost(url, port, protocal);
		threadgroup = new PageRetriever[ConfigMan.THREAD_IN_USE];
		this.wrapper = wrapper;
		this.recorder = recorder;
		this.initializeThread();
		task = null;

		
	}
	
	

	
	private void initializeThread() {
		for(int i = 0 ; i < threadgroup.length ; i++) {
			try {
				threadgroup[i] = new PageRetriever(httpClient,wrapper,recorder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			threadgroup[i].start();
		}
	}
	
	public void nextTask(NextPager task) {
		this.task = task;
		recorder.markDocNumbers();		//reset the counter for access record and new access record
	}
	
	public NextPager thisTask() {
		return task;
	}
	
	public void run() {
		while(true) {
			if(task == null) {
				try {
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME );
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {	
				HttpMethod nextToken = task.nextToken();
				int tid = 0;
				while(nextToken != null) {
					
					//check whether terminate
					threadgroup[tid].setToken(nextToken);
					nextToken = task.nextToken();
					tid = (tid+1)%threadgroup.length;
					
					while(threadgroup[tid].hasToken()) {		
						try {
							Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						tid = (tid+1)%threadgroup.length;
					}
					
					for(int i = 0 ; i < threadgroup.length ; i++) {
						if(threadgroup[i].lastExeStatus() == PageRetriever.LAST_PAGE) {
							task.terminate();
							break;
						}
					}
				
				}	//end the session
				
				try {
					//looply check all child threads haven finished
					while(true) {
						
						boolean allend = true;
						for(int i = 0 ; i < threadgroup.length ; i++) {
							allend = allend && !threadgroup[i].hasToken();
						}
						if(allend == true)		break;
						else {
							try {
								Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 2);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					
					if(ConfigMan.DEBUG) System.out.println("¡ñ retrival session is completed");
					//end the session
					recorder.commitIndex();	
					task = null;
					
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
				
		}
	
	}
	
	public void colaspse() {
		threadgroup = null;
	}
	
	public boolean hasTask() {
		return task != null;
	}
	
	public int getAccessDocs() {
		return recorder.getAccessDocs();
	}

	public int getNewDocs() {
		return recorder.getNewDocs();
	}
	
	
	public static void main(String args[]) {
		ConfigMan.THREAD_IN_USE = 6;
		PageRetrievalManager pm;
		try {
			pm = new PageRetrievalManager("http","202.117.1.14",80,(IWrapper)null, (null));
			ConfigMan.CRAWLER_SLEEP_TIME = 1000;
			NextPager.METHOD_TYPE = NextPager.GET_METHOD;
			NextPager np = new NextPager("/newmusic/musicsearch.php?musicsearch_name=a&searchtype=3&Submit=search");
			np.initialize("pagenum", 1);
			pm.nextTask(np);
			pm.start();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
}

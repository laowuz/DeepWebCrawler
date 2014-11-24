package com.dwim.magickbox;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import com.dwim.form.parser.NextPager;
import com.dwim.form.parser.PageRetriever;
import com.dwim.util.ConfigMan;
import com.dwim.wrapper.IWrapper;
import com.dwim.wrapper.WrapperFactory;

public class MagicRetriverManager extends Thread {

	
	protected MagicIDGenerator task;
	private MagicRetriver[] threadgroup;
	private HttpClient httpClient;
	private IWrapper wrapper;
	
	
	public MagicRetriverManager() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(ConfigMan.WRAPPER_NAME);
	}
	
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
	public MagicRetriverManager(String wrapperName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		threadgroup = new MagicRetriver[ConfigMan.THREAD_IN_USE];
		wrapper = WrapperFactory.getWrapper(wrapperName);
		this.initializeThread();
	}
	
	public MagicRetriverManager(IWrapper wrapper) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
		threadgroup = new MagicRetriver[ConfigMan.THREAD_IN_USE];
		this.wrapper = wrapper;
		this.initializeThread();
	}
	
	

	
	private void initializeThread() {
		
	
		for(int i = 0 ; i < threadgroup.length ; i++) {
			try {
				threadgroup[i] = new MagicRetriver(httpClient,wrapper);
			} catch (IOException e) {
				e.printStackTrace();
			}
			threadgroup[i].start();
		}
		
	}
	
	public void setTask(MagicIDGenerator task) {
		this.task = task;
		MagicRetriver.generateFileNamePattern(task.getPrefix(), task.getPostfix());
	}
	

	
	public void run() {
		if(task == null) return;
		HttpMethod nextToken = task.nextToken();
		int tid = 0;
		while(nextToken != null) {
			while(threadgroup[tid].hasToken()) {		
				try {
					Thread.sleep(PageRetriever.SLEEP_TIME/2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				tid = (tid+1)%threadgroup.length;
			}
			
			threadgroup[tid].setToken(nextToken);
			nextToken = task.nextToken();
			
			tid = (tid+1)%threadgroup.length;
		}
		task = null;
		this.colaspse();
	}
	
	
	public void colaspse() {
		threadgroup = null;
	}
	
}

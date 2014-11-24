package com.dwim.form.parser;

import java.util.ArrayList;

import com.dwim.util.ConfigMan;

/**
 * In this version of DWIM we assume the buffer page is big enough to store all
 * pages downloaded within a session
 * @author JL
 *
 */
public class URLBufferPool {
	private final int POOL_MAX_SIZE = ConfigMan.MAX_SRR_BUFFER;
	private final int FETCH_SIZE =  ConfigMan.SRR_FETCH_SIZE;
	private int pos;
	
	private String[] srrs = null;
	private static URLBufferPool instance;
	
	
	private URLBufferPool() {
		srrs = new String[POOL_MAX_SIZE];
		pos = 0;
		instance = null;
	}
	
	/**
	 * Add a 
	 * @param srrs
	 * @return true if the input search result record are correctly added into the buffer
	 * 		   false if can not be stored e.g.the buffer pool is full
	 */
	public static synchronized boolean addSRRs(ArrayList<String> ins) {
		if(ins == null)		return false;
		if(instance == null) {
			instance = new URLBufferPool();
		}
		if(instance.pos >= instance.POOL_MAX_SIZE-ins.size()) {
			return false;
		}
		for(int i = 0 ; i < ins.size() ; i++) {
			instance.srrs[instance.pos]= ins.get(i);
			instance.pos++;
		}
		if(ConfigMan.DEBUG) System.out.println("+ URL pool added " + ins.size() +" records.");
		return true;
	}
	
	/**
	 * Get a buffer ssrs
	 * @return
	 */
	public static synchronized ArrayList<String> getSSRs() {
	
		if(instance == null) {
			return null;
		}
		if(instance.pos <= 0) {
			return null;
		}
		ArrayList<String> result;
		int fetchsize = (instance.FETCH_SIZE > instance.pos) ? 
				instance.pos : instance.FETCH_SIZE;
		result = new ArrayList<String>((int) (fetchsize*1.25));
		for(int i = 0 ; i < fetchsize ; i++) {
			result.add(instance.srrs[instance.pos]);
			instance.pos--;
		}
		return result;
	}
	
	public static boolean hasMore(int num) {
		if(instance == null) {
			return false;
		}
		return instance.pos >= num;
	}
	
	public static boolean hasMore() {
		if(instance == null) {
			return false;
		}
		return instance.pos >= 1;
	}
	
	public static void clear() {
		if(instance != null) {
			instance.pos = 0;
		}
	}
	
	
	
	
}

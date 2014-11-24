package com.dwim.index;

import java.util.ArrayList;

import com.dwim.util.ConfigMan;

/**
 * In the first version of DWIM we assume the buffer page is big enough to store all
 * pages downloaded within a session
 * @author JL
 *
 */
public class SRRBufferPool {
	private final int POOL_MAX_SIZE;
	private final int FETCH_SIZE;
	private int pos;
	
	private SearchResultRecord[] srrs = null;
	private static SRRBufferPool instance;
	
	
	private SRRBufferPool() {
		
		POOL_MAX_SIZE = ConfigMan.MAX_SRR_BUFFER;
		FETCH_SIZE =  ConfigMan.SRR_FETCH_SIZE;
		
		srrs = new SearchResultRecord[POOL_MAX_SIZE];
		pos = 0;
		instance = null;
	}
	
	/**
	 * Add a 
	 * @param srrs
	 * @return true if the input search result record are correctly added into the buffer
	 * 		   false if can not be stored e.g.the buffer pool is full
	 */
	public static synchronized boolean addSRRs(ArrayList<SearchResultRecord> ins) {
		if(ins == null)		return false;
		if(instance == null) {
			instance = new SRRBufferPool();
		}
		if(instance.pos >= instance.POOL_MAX_SIZE-ins.size()) {
			return false;
		}
		for(int i = 0 ; i < ins.size() ; i++) {
			instance.srrs[instance.pos]= ins.get(i);
			instance.pos++;
		}
		if(ConfigMan.DEBUG) System.out.println("+ SSR pool added " + ins.size() +" records.");
		return true;
	}
	
	public static synchronized boolean addSRR(SearchResultRecord in) {
		if(in == null)		return false;
		if(instance == null) {
			instance = new SRRBufferPool();
		}
		if(instance.pos >= instance.POOL_MAX_SIZE-1) {
			return false;
		}

			instance.srrs[instance.pos]= in;
			instance.pos++;

		if(ConfigMan.DEBUG) System.out.println("+ SSR pool added " + 1 +" records.");
		return true;
	}
	
	
	/**
	 * Get a buffer ssrs
	 * @return
	 */
	public static synchronized ArrayList<SearchResultRecord> getSSRs() {
	
		if(instance == null) {
			instance = new SRRBufferPool();
		}
		if(instance.pos <= 0) {
			return null;
		}
		ArrayList<SearchResultRecord> result;
		int fetchsize = (instance.FETCH_SIZE > instance.pos) ? 
				instance.pos : instance.FETCH_SIZE;
		result = new ArrayList<SearchResultRecord>((int) (fetchsize));
		for(int i = 0 ; i < fetchsize ; i++) {
			result.add(instance.srrs[instance.pos-1]);
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

package com.dwim.util;
import org.apache.log4j.*;

public class DWIMLogger {
	private static Logger instance;
	
	private DWIMLogger() {
		instance = null;
	}
	
	public static Logger getLogger() {
		if(instance == null) {
			instance = Logger.getLogger("COM.DWIM.INFO"); 
			instance.setLevel(Level.INFO);

		}
		return instance;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

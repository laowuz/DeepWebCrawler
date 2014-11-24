package com.dwim.util;

public class DWIMException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String sysMes;
	private String mes;
	
	public DWIMException(String info) {
		mes = info;
	}
	
	public DWIMException(Exception e,String info){
		super();
		sysMes = e.getMessage();
		mes = info;
	}
		
	public String getSysMes(){
		return sysMes;
	}
	
	public String getMes() {
		return mes;
	}
	
	public String getMessage(){
		String result = "DWIM encounters an Exception. Due to: " + this.getMes();
		if(this.getSysMes() != null)
			result += this.getSysMes();
		return result;
	}

}

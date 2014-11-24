package com.dwim.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.dwim.magickbox.MagicID;
import com.dwim.magickbox.MagicIDGenerator;
import com.dwim.magickbox.MagicRetriverManager;
import com.dwim.util.ConfigMan;

public class MagicCrawler extends Thread{
	private MagicRetriverManager manager;
	
	public MagicCrawler(MagicIDGenerator in) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		manager = new MagicRetriverManager( ConfigMan.WRAPPER_NAME);
		manager.setTask(in);
	}
	

	 
	public void run() {
		manager.start();
	}
	
	
	public static void main(String args[]) {
		//config
		ConfigMan.HOSTURL = "202.117.1.11";
		ConfigMan.PROTOCAL = "http";
		ConfigMan.PORTNUM = 80;
		ConfigMan.URL_INDEX_DIR = "D:\\url_index";
		ConfigMan.THREAD_IN_USE = 8;
		ConfigMan.ALL_DOCUMENT_IN_DB = ConfigMan.DEFAULT_SMALLDB_DOCUMENT;
		ConfigMan.CRAWLER_SLEEP_TIME = 1000;
		ConfigMan.SAVE_HTML_RECORD = true;
		ConfigMan.SAVE_HTML_CODESET = "gb2312";
		ConfigMan.SAVE_HTML_PATH = "D:\\content\\vod";
		ConfigMan.WRAPPER_NAME = "vod";
		ConfigMan.DEBUG = true;
		ConfigMan.RE_WRAPPER_SRR_REXS = new String[]{"name=\"filmid\" value=\""};
		
		
		
		MagicID magic = new MagicID();
		boolean applicable = false;
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://202.117.1.11/showfilm.php?id=2582");
		ins.add("http://202.117.1.11/showfilm.php?id=2579");
		ins.add("http://202.117.1.11/showfilm.php?id=1816");
		ins.add("http://202.117.1.11/showfilm.php?id=1205");
		ins.add("http://202.117.1.11/showfilm.php?id=1388");
		ins.add("http://202.117.1.11/showfilm.php?id=1327");
		ins.add("http://202.117.1.11/showfilm.php?id=1250");
		ins.add("http://202.117.1.11/showfilm.php?id=1247");
		ins.add("http://202.117.1.11/showfilm.php?id=1987");
		try {
			applicable = magic.scan(ins);
			if(!applicable)	{
				System.out.println("Cannot use magic crawler");
				return; 
			}
			MagicCrawler crawler = new MagicCrawler(magic.getIDGenerator());
			crawler.start();
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}

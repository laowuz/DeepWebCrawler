package com.dwim.magickbox;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dwim.util.ConfigMan;


public class MagicIDGeneratorTest {

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNextUrl1() {
		ConfigMan.ALL_DOCUMENT_IN_DB = 50;
		ConfigMan.URL_INDEX_DIR = "D:\\url_index";
		MagicID magic = new MagicID();
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=6999");
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=6991");
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=6999");
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=4");
		try {
			boolean re = magic.scan(ins);
		
			System.out.println(re);
			if(re) {
				MagicIDGenerator mg = magic.getIDGenerator();
				//mg.setMissCount(16);
				if(mg == null) return;
				String url = mg.nextUrl() ;
				while(url!= null) {
					System.out.println(url);
					Thread.sleep(100);
					url = mg.nextUrl();
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testNextUrl2() {
		ConfigMan.ALL_DOCUMENT_IN_DB = Long.MAX_VALUE/100;
		ConfigMan.URL_INDEX_DIR = "D:\\url_index";
		MagicID magic = new MagicID();
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://v.youku.com/v_show/id_Iw.html");
		ins.add("http://v.youku.com/v_show/id_Qa.html");
		ins.add("http://v.youku.com/v_show/id_XM.html");
		ins.add("http://v.youku.com/v_show/id_XM.html");
		ins.add("http://v.youku.com/v_show/id_Ua.html");
		ins.add("http://v.youku.com/v_show/id_Xj.html");
		try {
			boolean re = magic.scan(ins);
		
			System.out.println(re);
			if(re) {
				MagicIDGenerator mg = magic.getIDGenerator();
				mg.setMissCount(30);
				if(mg == null) return;
				String url = mg.nextUrl() ;
				while(url!= null) {
					System.out.println(url);
					Thread.sleep(100);
					url = mg.nextUrl();
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testNextToken() {
		
	}

	@Test
	public void testNextTokenString() {
		
	}

}

package com.dwim.magickbox;


import java.net.MalformedURLException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dwim.util.ConfigMan;

public class MagicIDTest {
	private MagicID subject;
	@Before
	public void setUp() throws Exception {
		ConfigMan.ALL_DOCUMENT_IN_DB = 10000;
		subject =  new MagicID();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testScan1() {
		
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=6999");
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=6991");
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=6999");
		ins.add("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=4");
		try {
			System.out.println(subject.scan(ins));

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testScan2() {
		System.out.println("--------------------------------");
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://v.youku.com/v_show/id_XMTI5NzEzOTIw.html");
		ins.add("http://v.youku.com/v_show/id_XMTI5NzQwNzQ4.html");
		ins.add("http://v.youku.com/v_show/id_XMTI5Njc4Nzcy.html");
		ins.add("http://v.youku.com/v_show/id_XMTI5NTU5NjEy.html");
		ins.add("http://v.youku.com/v_show/id_XMTI5NzIyMDU2.html");
		ins.add("http://v.youku.com/v_show/id_XMTI5NTU5NjEy.html");
		try {
			System.out.println(subject.scan(ins));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testScan3() {
		System.out.println("--------------------------------");
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://movies.yahoo.com/movie/1800129993/info");
		ins.add("http://movies.yahoo.com/movie/1808404374/info");
		ins.add("http://movies.yahoo.com/movie/1800096376/info");
		ins.add("http://movies.yahoo.com/movie/1800091029/info");
		ins.add("http://movies.yahoo.com/movie/1809415855/info");
		ins.add("http://movies.yahoo.com/movie/1809354112/info");
		try {
			System.out.println(subject.scan(ins));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testScan4() {
		System.out.println("--------------------------------");
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://product.dangdang.com/product.aspx?product_id=20039611");
		ins.add("http://product.dangdang.com/product.aspx?product_id=20128423");
		ins.add("http://product.dangdang.com/product.aspx?product_id=9040533");
		ins.add("http://product.dangdang.com/product.aspx?product_id=20246019");
		ins.add("http://product.dangdang.com/product.aspx?product_id=8862120");
		ins.add("http://product.dangdang.com/product.aspx?product_id=20630568");
		try {
			System.out.println(subject.scan(ins));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testScan5() {
		ConfigMan.ALL_DOCUMENT_IN_DB = 1900000;
		System.out.println("--------------------------------");
		ArrayList<String> ins = new ArrayList<String>();
		ins.add("http://www.imdb.com/title/tt1323577/");
		ins.add("http://www.imdb.com/title/tt0384293/");
		ins.add("http://www.imdb.com/title/tt0089610/");
		ins.add("http://www.imdb.com/title/tt0049438/");
		ins.add("http://www.imdb.com/title/tt1474465/");
		ins.add("http://www.imdb.com/title/tt0372509/");
		try {
			System.out.println(subject.scan(ins));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

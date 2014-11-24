package com.dwim.magickbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;

import com.dwim.index.IDIndexer;
import com.dwim.index.SearchResultRecord;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.util.HTMLDocument;
import com.dwim.wrapper.IWrapper;

public class MagicRetriver extends Thread{
	public static final int HIT = 1000, MISS = 1400;
	
	
	private static int idcounter = 1;
	private int id;

	private HttpClient httpClient;
	private IDIndexer idindexer;
	
	private HttpMethod method;
	private IWrapper wrapper;
	private static Pattern fileNamePattern;
	protected static int missScale;
	protected static int missCount;

	

	public MagicRetriver(HttpClient httpClient, IWrapper wrapper) throws IOException {
		id = idcounter++;
		this.httpClient = httpClient;
		this.wrapper = wrapper;
		idindexer = IDIndexer.getInstance();
		missScale = 0;
		missCount = 0;
		method = null;


	}

	public void run() {
		if(ConfigMan.DEBUG) System.out.println("Magic retriver " + id + " started" );
		while(true) {
			try {
				if(method == null)	Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME);
				else {
					int status = httpClient.executeMethod(method);
					String thisURL = method.getPath();
					if(status / 100 == 2)	{
						String pageContent = method.getResponseBodyAsString();
						ArrayList<SearchResultRecord> srrs = wrapper.extract(pageContent);
						method.releaseConnection();					
					
						if(srrs != null && srrs.size() > 0) {
							if(ConfigMan.DEBUG) System.out.println("¡ýretriver " + id + " hits " + thisURL);
							if(!idindexer.exists(idindexer.format(thisURL))) 
								idindexer.index(thisURL);
							//record the hit
							if((missScale & 0x1000) == 0x1000) {
								//delete the old record
								missCount--;
							}
							missScale = missScale<<1;
							if(ConfigMan.SAVE_HTML_RECORD) 
								HTMLDocument.write(ConfigMan.SAVE_HTML_PATH, generateFileName(thisURL), ConfigMan.SAVE_HTML_CODESET, pageContent);
							method = null;
						}
						
						else {
							if(ConfigMan.DEBUG) System.out.println("retriver " + id + " miss " + thisURL);
							if((missScale & 0x1000) == 0x1000) {
								//delete the old record
								missCount--;
							}
							missScale = missScale<<1 + 1;
							missCount++;
							method = null;
						}
						
					} else {
						if(ConfigMan.DEBUG) System.out.println("retriver " + id + " miss " + thisURL);
						missScale = missScale<<1 + 1;
						missCount++;
						method = null;
						method = null;
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (HttpException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} catch (DWIMException e) {
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
	
	public static void generateFileNamePattern(String prefix, String postfix) {
		if(!ConfigMan.SAVE_HTML_RECORD)
			return;
		fileNamePattern = Pattern.compile(prefix+"(.+)"+postfix);
	}
	
	private String generateFileName(String in) throws DWIMException {
		if(fileNamePattern == null)
			throw new DWIMException("Please call method generateFileNamePattern() before the method");
		Matcher matcher = fileNamePattern.matcher(in);
		if(matcher.find()) {
			in = matcher.group(1);
		} 
		in = fileNameFormat(in);
		return in;
	}
	
	private String fileNameFormat(String in) {
		if(in == null)	return null;
		char[] buffer = in.toCharArray();
		for(int i = 0 ; i < buffer.length ; i++) {
			if(buffer[i] == '?' || buffer[i] == '/'  || buffer[i] == ':')
				buffer[i] = '_';
			else if(buffer[i] == '\\' || buffer[i] == '*' ||  buffer[i] == '"'	
				|| buffer[i] == '<' || buffer[i] == '>' || buffer[i] == '|' || buffer[i] == '\\')
				buffer[i] = ' ';
		}
		return new String(buffer);
	}

	protected static void setMissCount(int in) {
		missCount = in;
	}
	


	
}

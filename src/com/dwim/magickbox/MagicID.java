package com.dwim.magickbox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.lucene.index.CorruptIndexException;

import com.dwim.form.parser.ITokenGenerator;
import com.dwim.index.IDIndexer;
import com.dwim.util.ConfigMan;

/**
 * This version only supports the single variant component id generation
 * @author JL
 *
 */

public class MagicID {
	public final static int MIN_VARIANT_COMPONENT = 1;
	
	public final static int DIGITAL = 10;
	public final static int CNCHARACTER = 21886;
	public final static int LETTER = 26;
	public final static int ASCII = 128;
	
	private static String[] idRegex= {"10\\.1\\.\\d.\\d+.\\d+"};
	private static String[] idPatternName = {"doi"};
	

	
	
	private String[] vcomponents;
	private int[] spaceType;
	private String prefix;
	private String postfix;
	

	
	private boolean applicable;

	               
	   
	
	public MagicID() {
		applicable = false;
	
	}
	
	               
	               
	/**
	 * provide enough sample record for precise estimations.
	 * @param ins
	 * @return
	 * @throws MalformedURLException
	 */               
	public boolean scan(ArrayList<String> ins) throws MalformedURLException {
		if(ins == null || ins.size() < 2)
			return false;
		
		int[] columnLength = new int[ins.size()];
		String[][] components = new String[ins.size()][];
		String[] hosts = new String[ins.size()];
		
	
		for(int i = 0 ; i < components.length ; i++) {
			URL temp = new URL(ins.get(i));
			hosts[i] = temp.getHost();
			components[i] = temp.getFile().split("[&/\\?]");
			columnLength[i] = components[i].length;
			
		}
		//test the host consistency and build up the component array
		for(int j = 0 ; j < components.length ; j++) {
			for(int k = j+1 ; k < components.length ; k++) {
				if(!hosts[j].equals(hosts[k]))
					return false;
			}
		}
		//test the align of columns
		for(int j = 0 ; j < components.length ; j++) {
			for(int k = j+1 ; k < components.length ; k++) {
				if(columnLength[j] !=columnLength[k])
					return false;
			}
		}
		
		
		boolean[] ics = new boolean[columnLength[0]];
		for(int i = 0 ; i < ics.length ; i++) {
			ics[i] = true;
		}
		
		//detect the variant component	
		for(int i = 1 ; i < columnLength[0] ; i++) {
			for(int j = 0 ; j < components.length ; j++) {
				
				for(int k = j+1 ; k < components.length ; k++) {
					if(!components[j][i].equals(components[k][i])) {
						ics[i] = false;
						break;
					}
				}
			}
		}
			
		int numOfvc = 0;			//number of variant component
		int vcid = -1;				//variant component id
		for(int i = 0 ; i < ics.length ; i++) {
			if(!ics[i]) {
				numOfvc++;
				vcid = i;
			}
		}
		if(numOfvc > MIN_VARIANT_COMPONENT || vcid == -1)	{
			vcomponents = null;
			return false;
		}
		vcomponents = new String[components.length];
		for(int i = 0 ; i < components.length ; i++) {
			vcomponents[i] = components[i][vcid];
		}
		
		
		int pos = ins.get(0).indexOf(vcomponents[0]);
		if(pos == -1)	return false;
		prefix = ins.get(0).substring(0,pos);
		postfix = ins.get(0).substring(pos+vcomponents[0].length(),ins.get(0).length());
		
		long spaceSize = calculateIDSpace();
		
		//simple rule should probe in the next version
		if(spaceSize > ConfigMan.ALL_DOCUMENT_IN_DB*100)
			return false;
		
		
		
		applicable = true;
		return applicable;
	}
	
	
	protected String cleanQuery(String url) {
		return url;
		
	}
	
	protected long calculateIDSpace() {
		if(vcomponents == null)	return Long.MAX_VALUE;
		boolean aces = true;								//all contain equal sign
		int lcl = 0, lci = -1;								//longest component length an index 
		int scl = Integer.MAX_VALUE;				//shortest component length an index 
		int[] epos = new int[vcomponents.length];
		
		//test all contain equal sign and pick the longest component
		for(int i = 0 ; i < vcomponents.length ; i++) {
			if(aces) {
				epos[i] = vcomponents[i].indexOf("=");
				if(epos[i] == -1) {
					aces = false;
				}
			}
			if(vcomponents[i].length() > lcl) {
				lcl = vcomponents[i].length();
				lci = i;
			} else if(vcomponents[i].length() < scl) {
				scl = vcomponents[i].length();
			}
			
		}
	
		if(aces) {
			String paraName = vcomponents[lci].substring(0,vcomponents[lci].indexOf("=")+1);
			prefix += paraName;
			//adjust longest and shortest component length containing equal sign
			lcl -= paraName.length();
			scl -= paraName.length();
			for(int i = 0 ; i < vcomponents.length ; i++) 
				vcomponents[i] = vcomponents[i].substring(epos[i]+1, vcomponents[i].length());
			
			
		}
		

		//optimistic estimation
		spaceType = new int[lcl];
		int type = -1;
		//initialize the space type array
		for(int i = 0 ; i < spaceType.length ; i++) {
			spaceType[i] = 1;
		}
		
		
		for(int i = 0 ; i < idRegex.length ; i++) {
			if(vcomponents[lci].matches(idRegex[i])) {
				type = i;
				break;
			}
		}
		if(type == -1)
			this.universalSpace(vcomponents[lci]);
		else if(idPatternName[type].equals("doi")) {
			this.DOISpace(vcomponents[lci]);
		}
		
		
		
		for(int i = scl ; i < spaceType.length ; i++) {
			char c = vcomponents[lci].charAt(i);
			if(c >= '0' && c <= '9' && spaceType[i] <= DIGITAL) {
				spaceType[i] = DIGITAL;
			} else if(c >= 'a' && c <= 'z' && spaceType[i] <= LETTER) {
				spaceType[i] = LETTER;
			} else if(c >= 'A' && c <= 'Z' && spaceType[i] <= LETTER) {
				spaceType[i] = LETTER;
			} else {
				spaceType[i] = ASCII;
			}
		}
		
		long spaceSize = 1;
		
		for(int i = 0 ; i < spaceType.length ; i++) {
			spaceSize *= spaceType[i];
		}
		
		if(spaceSize == 0)	return Long.MAX_VALUE;
		return spaceSize;
	}
	
	
	
	protected int[] DOISpace(String sc) {
		
		return spaceType;
	}
	
	protected int[] DateSapce(String sc) {
		
		return spaceType;
	}
	
	
	
	
	
	protected int[] universalSpace(String sc) {
		for(int i = 0 ; i < sc.length() ; i++) {
			spaceType[i] = 1;
			char c1 = sc.charAt(i);	
			for(int j = 0 ; j < vcomponents.length ; j++) {
				if(i > vcomponents[j].length() -1) 
					continue;
				char c2 = vcomponents[j].charAt(i);
				if(c1 != c2) {
					if(c2 >= '0' && c2 <= '9' && spaceType[i] <= DIGITAL) {
						spaceType[i] = DIGITAL;
					} else if(c2 >= 'a' && c2 <= 'z' && spaceType[i] <= LETTER) {
						spaceType[i] = LETTER;
					} else if(c2 >= 'A' && c2 <= 'Z' && spaceType[i] <= LETTER) {
						spaceType[i] = LETTER;
					} else {
						spaceType[i] = ASCII;
					}
				}
			}
		}
		return spaceType;
	}
	

	
	
	public MagicIDGenerator getIDGenerator() throws IOException {
		if(!applicable)	return null;
		MagicIDGenerator result = new MagicIDGenerator(vcomponents,spaceType,prefix,postfix);
		result.turnOn();
		return result;
	}
	
	
	protected boolean test(String url) {
		return false;
		
	}


	public String getPrefix() {
		return prefix;
	}



	public String getPostfix() {
		return postfix;
	}
	
	
	
	
}

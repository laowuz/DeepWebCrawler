package com.dwim.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class URLDistance {
	
	
	public static double similar(String s1, String s2) throws MalformedURLException {
		URL url1 = new URL(s1);
		URL url2 = new URL(s2);
		
		if(url1.getHost() == null || url2.getHost() == null ||
		   !url1.getHost().equals(url2) || url1.getPort() != url2.getPort())
			return 0.0d;
		String[][] pvpair1, pvpair2;
		pvpair1 = pvpair(url1.getQuery());
		pvpair2 = pvpair(url2.getQuery());
		if(pvpair1 == null || pvpair2 == null || pvpair1[0] == null || pvpair2[0] == null) 
			return 0.0d;
		double commanpara = 0;
		double similarity = 0;
		
		if(pvpair1[0].length > pvpair1[0].length) {
			String[][] temp = pvpair1;
			pvpair1 = pvpair2;
			pvpair2 = temp;
		}
			
		for(int i = 0 ; i < pvpair1[0].length ; i++) {
			for(int j = 0 ; j < pvpair2[0].length ; j++) {
				if(pvpair1[0][i].equals(pvpair2[0][j])) {
					commanpara++;
					similarity += valueSim(pvpair1[1][i],pvpair1[1][i]);
					
				}
			}
		}
		return similarity;
		
		
		
		
	
	}
	
	/**
	 * 
	 * @param qstring query string
	 * @return
	 */
	protected static String[][] pvpair(String qstring) {
		if(qstring == null)	return null;
		String[] temp = qstring.split("&");
		ArrayList<String> para = new ArrayList<String>(temp.length);
		ArrayList<String> value = new ArrayList<String>(temp.length);
		
		for(int i = 0 ; i < temp.length ; i++) {
			int pos = temp[i].indexOf("=");
			if(pos == -1)
				continue;
			para.add(temp[i].substring(0,pos));
			if(pos > temp[i].length()-2)
				value.add(null);
			else
				value.add(temp[i].substring(pos+1,temp[i].length()));
		}
		
		String[][] result = new String[2][para.size()];
		para.toArray(result[0]);
		value.toArray(result[1]);
		return result;
		
	}
	
	/**
	 * Note here we assume each integer number are encoded in decimal or hex system.
	 * @param v1
	 * @param v2
	 * @return
	 */
	protected static double valueSim(String v1, String v2) {
		if(v1 == null && v2 == null)	
			return 1.0d;
		else if(v1 == null || v2 == null)	
			return 0.0d;
		else{
			boolean v1dec = v1.matches("\\d+");
			boolean v2dec = v2.matches("\\d+");
			if(v1dec&v2dec) {
				//both are decimals
				Integer int1,int2;
				int1 = Integer.parseInt(v1);
				int2 = Integer.parseInt(v2);
				return 1.0d - Math.abs((double)(int2-int1))/(double)Math.max(int2, int1);
			}
			boolean v1hex = v1.matches("[0-9A-Fa-f]+");
			boolean v2hex = v2.matches("[0-9A-Fa-f]+");
			if(v1hex&v2hex) {
				//both are hex
				Integer int1,int2;
				int1 = Integer.parseInt(v1,16);
				int2 = Integer.parseInt(v2,16);
				return 1.0d - Math.abs((double)(int2-int1))/(double)Math.max(int2, int1);
			}
			//both are string
			// edit distance
			
			return Math.max(1.0d-EditDistance.editDistanceRatio(v1, v2)*2,0);
		}
	}
	
	public static void main(String args[]) {

			String s1 ="http://www.shadetrees.org/results.php?type=&street=&maintenance=3|Clearance+prune&colorblock=&order=tag";
			String s2 ="http://www.shadetrees.org/results.php?type=&street=&maintenance=3|Clearance+prune&colorblock=&order=tag";
			System.out.println(valueSim("pig","pig1"));

		
		
	}
}

package com.dwim.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class HTMLDocument {
	
	public static void write(String path, String name, String codeSet, String content) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path,name+".html")),codeSet));
			bw.write(content);
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String read(String path, String name, String codeSet) {
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path,name+".html")),codeSet));
			StringBuffer sb = new StringBuffer(10000);
			String line = br.readLine();
			while(line != null) {
				sb.append(line+"\r\n");
				line = br.readLine();
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void write(String path, String name, String codeSet, String content, String postfix) {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path,name+"."+postfix)),codeSet));
			bw.write(content);
			bw.flush();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String read(String path, String name, String codeSet, String postfix) {
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path,name+"."+postfix)),codeSet));
			StringBuffer sb = new StringBuffer(10000);
			String line = br.readLine();
			while(line != null) {
				sb.append(line+"\r\n");
				line = br.readLine();
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String formatName(String url) {
		if(url == null)	return null;
		char[] buffer = url.toCharArray();
		for(int i = 0 ; i < buffer.length ; i++) {
			if(buffer[i] == '?' || buffer[i] == '/'  || buffer[i] == ':')
				buffer[i] = '_';
			else if(buffer[i] == '\\' || buffer[i] == '*' ||  buffer[i] == '"'	
				|| buffer[i] == '<' || buffer[i] == '>' || buffer[i] == '|' || buffer[i] == '\\')
				buffer[i] = ' ';
		}
		return new String(buffer);
	}
	
	public static void makeDirs(String path, String dirName) {
		File summaryDir = new File(path, dirName);
		if(!summaryDir.exists())
				summaryDir.mkdirs();
	}
	/**
	 * attempt to retrieve the cached page (avoid duplicate aceess to the host)
	 * @param url 
	 * @return null if no cached page
	 */
	public static String getCachedPage(String url) {
		String fileName = HTMLDocument.formatName(url);
		File tempFile = new File(ConfigMan.SAVE_HTML_PATH,fileName+".html");
		if(!tempFile.exists()) return null;
		return HTMLDocument.read(ConfigMan.SAVE_HTML_PATH, 
				fileName, ConfigMan.SAVE_HTML_CODESET);	
	}
	
	public static void main(String[] args) {
		HTMLDocument.write("e:\\", "temp2.txt", "utf-8", "hihi");
	}
}

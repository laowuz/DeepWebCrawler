package com.dwim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DebugFileParser {
	
	public String compareAccumulareNewRecords(File f1, File f2) throws IOException {
		BufferedReader br;

		br = new BufferedReader(new InputStreamReader(new FileInputStream(f1)));
		StringBuffer sb = new StringBuffer(10000);
		String line = br.readLine();
		while(line != null) {
			sb.append(line+"\r\n");
			line = br.readLine();
		}
			
		return null;
		
	}
}

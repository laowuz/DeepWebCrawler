package com.dwim.util;

import java.text.DecimalFormat;

public class FormatPrinter {
	public static final String BLANK = "  ";
	private static DecimalFormat intFormater = new DecimalFormat("###");
	
	public static String spaces(String column,int maxSpaces) {
		if(column.matches("[\\u4e00-\\u9fa5]*"))
			return spacesNarrow(column, maxSpaces);
		else
			return spacesWidth(column, maxSpaces);
	}
	
	public static String spaces(int column,int maxSpaces) {
		return spacesWidth(intFormater.format(column), maxSpaces);
	}
	
	public static String spaces(double column,int maxSpaces) {
		return spacesWidth(Double.toString(column), maxSpaces);

	}
	
	private static String spacesNarrow(String column,int maxSpaces) {
		if(column.length()>=maxSpaces)	return column.substring(0,maxSpaces-2)+BLANK;
		StringBuffer spaces = new StringBuffer();
		if(column.length()<maxSpaces)
			for(int i = 0;i<maxSpaces-column.length();i++)
				spaces.append(BLANK);
		return column+spaces.toString();
	}
	
	private static String spacesWidth(String column,int maxSpaces) {
		if((column.length())>=maxSpaces)	return column.substring(0,maxSpaces)+BLANK;
		StringBuffer spaces = new StringBuffer();
		if(column.length()<maxSpaces)
			for(int i = 0;i<maxSpaces-column.length();i++)
				spaces.append(BLANK);
		return column+spaces.toString();
	}
}

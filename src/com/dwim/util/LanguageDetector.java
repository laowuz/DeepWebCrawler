package com.dwim.util;

/**
 * Decide the language type of a given keyword. It Supports languages including Arabic Number, English, Latin(Partial) and Chinese.
 * @author JL
 *
 */
public class LanguageDetector {
	//Types of language
	public static final int SUPPORT_TYPES_OF_LANUAGE = 3;
	//The language types should be defined in sequence.
	//Be careful when you specify your own language type following the instructions.
	public static final int MULTIPLE_FACTOR = 100;
	public static final int UNKNOWN_TYPE = 0,
							ARABIC_NUMERAL = 1*MULTIPLE_FACTOR,
							PURE_ARABIC_NUMERAL = 1*MULTIPLE_FACTOR+1,
							MAJOR_ARABIC_NUMERAL = 1*MULTIPLE_FACTOR+2,
							ENLGLISH = 2*MULTIPLE_FACTOR,
							PURE_ENGLISH = 2*MULTIPLE_FACTOR+1,
							MAJOR_ENGLISH = 2*MULTIPLE_FACTOR+2,
							CHINESE = 3*MULTIPLE_FACTOR,
							PURE_CHINESE = 3*MULTIPLE_FACTOR+1,
							MAJOR_CHINESE = 3*MULTIPLE_FACTOR+2;
	public static int[] counter = new int[SUPPORT_TYPES_OF_LANUAGE+1];
						
	public static int getLaguageType(String word) {
		if(word == null)	return UNKNOWN_TYPE;
		
		for(int i = 0 ; i < counter.length ; i++) {
			counter[i] = 0;
		}

		for(int i = 0 ; i < word.length() ; i++) {
			String tchar = word.substring(i,i+1);
			if(tchar.matches("[\\u0021-\\u007f]")) 
				counter[ENLGLISH/MULTIPLE_FACTOR]++;
			else if(tchar.matches("[\u4E00-\u9FA5]")) 
				counter[CHINESE/MULTIPLE_FACTOR]++;
			else if(tchar.matches("[\u4E00-\u9FA5]"))
				counter[ARABIC_NUMERAL/MULTIPLE_FACTOR]++;
		}
		int maxValue = 0, maxIndex = 0;
		for(int i = 1 ; i < counter.length ; i++) {
			if(counter[i] > maxValue) {
				maxValue = counter[i];
				maxIndex = i;
			}
		}
		
		return maxIndex*100;
		
	}
	
	public static int getLaguageTypeDetail(String word) {
		if(word == null)	return UNKNOWN_TYPE;
		int[] counter = new int[SUPPORT_TYPES_OF_LANUAGE+1];
		for(int i = 0 ; i < counter.length ; i++) {
			counter[i] = 0;
		}

		for(int i = 0 ; i < word.length() ; i++) {
			String tchar = word.substring(i,i+1);
			if(tchar.matches("[\\u0021-\\u007f]")) 
				counter[ENLGLISH/100]++;
			else if(tchar.matches("[\u4E00-\u9FA5]")) 
				counter[CHINESE/100]++;
			else if(tchar.matches("[\u4E00-\u9FA5]"))
				counter[ARABIC_NUMERAL/100]++;
		}
		int maxValue = 0, maxIndex = 0;
		for(int i = 1 ; i < counter.length ; i++) {
			if(counter[i] > maxValue) {
				maxValue = counter[i];
				maxIndex = i;
			}
		}
		if(maxIndex == 0) return UNKNOWN_TYPE;
		else if(counter[maxIndex] == word.length())
			return maxIndex*MULTIPLE_FACTOR+1;
		else
			return maxIndex*MULTIPLE_FACTOR+2;
	}
	
	
}

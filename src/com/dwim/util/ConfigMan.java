package com.dwim.util;

public class ConfigMan {
	
	
	
	public static String HOSTURL;
	public static String PROTOCAL = "http";
	public static String SEARCH_PAGE_PATH; 
	public static int PORTNUM = 80;

	public static String HTTP_CONTENT_CODESET = "gb2312";
	public static String URL_CONTENT_CODESET = "gb2312";
	public static boolean DEBUG = true;
	
	public static int CRAWLER_SLEEP_TIME = 1000;
	public static int MAX_KEYWORD_LENGTH = 15;
	public static int RECORD_BUFFER_SIZE = 1024;

	public static int MAX_SRR_BUFFER = 10240;
	public static int SRR_FETCH_SIZE = 128;
	
	public static int THREAD_IN_USE = 6;
	
	public static double SRR_SAMPLING_RATE = 0.2;
	
	public static String RECORD_INDEX_DIR;			//compulsory
	public static String URL_INDEX_DIR;				//compulsory
	
	public static long ALL_DOCUMENT_IN_DB;
	public static int DEFAULT_MINIDB_DOCUMENT = 2000,
					  DEFAULT_SMALLDB_DOCUMENT = 10000, 
					  DEFAULT_MEDIUMDB_DOCUMENT = 100000, 
					  DEFAULT_LARGEMDB_DOCUMENT = 1000000;
	

	public static int CANDIDATE_SET_SIZE = 500;			//has to be an even?? equally division??
	public static int TRANING_SET_SIZE = 300;
	
	
	/**
	 * 
	 */
	public static boolean SAVE_HTML_RECORD = true;
	public static String SAVE_HTML_PATH;
	public static String SAVE_HTML_CODESET = "utf-8";
	
	public static String SAVE_CANDIDATE_SET_PATH;
	
	public static int H_STEP = 2;
	public static double PATTERN_REWARD_FADING_FACTOR = 0.65;
	//a discount for TF value 
	public static final int AVAERAGE_WORDS_IN_DOC = 32;
	
	
	public static boolean MULTI_LANUAGE;			//accelerate the speed when multi-language is off (English mode)
	public static String WRAPPER_NAME;
	public static String WRAPPER_ENTRY_REXS;
	public static Object[] WRAPPER_SRR_SCRIPT;
	

	public static boolean EXPERIMENTAL_MODE = false;
	public static String MAX_PAGE_OFFSET_REX;
	//professional
	
	public static boolean USE_STEM = true;
	public static boolean LIMIT_RESULT = false;
	public static boolean MERGE_SAME_TAG_WORD = false;
	
	

	/**
	 * incorporate the feature if its corresponding bit is set to 1, 0 otherwise.
	 * 0 Highlight
	 * 1 RIDF
	 * 2 DFRANK
	 * 3 Keyword length
	 * 4 Element ID
	 * 5 Keyword Tag in DOM tree
	 * 6 Keyword's POS
	 * 7 Keyword's language type
	 * others is reserved for user defined features.
	 */
	public static double[] FEATURE_WEIGHT = new double[]{0,10,0,0,0,2,1,0};
	
	public static int F_HIGHLIGHT = 0,
					   F_RIDF = 1,
					   F_DFRANK = 2,
					   F_LENGTH = 3,
					   F_ELEMENT = 4,
					   F_DOMTAG =5,
					   F_POS = 6,
					   F_LANGUAGE = 7;
					   
	public final static int DF_RANK_BUFFER_SIZE = CANDIDATE_SET_SIZE;
	public static int K_NEARST_NEIGHBOR = 3;
	
	
	
	
	
	
	
	
}

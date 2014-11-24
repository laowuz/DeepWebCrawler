package com.dwim.main;


import java.util.ArrayList;

import com.dwim.form.analysis.EP;
import com.dwim.form.analysis.IElement;
import com.dwim.form.parser.NextPager;
import com.dwim.form.parser.NextRecorder;
import com.dwim.form.parser.PageRetrievalManager;
import com.dwim.form.parser.Query;
import com.dwim.index.IndexManager;
import com.dwim.index.RecordIndexer;
import com.dwim.index.SRRBufferPool;
import com.dwim.kv.policy.FeatureExtractor;
import com.dwim.kv.policy.GenericDFEstimator;
import com.dwim.kv.policy.GenericKeywordEvaluator;
import com.dwim.kv.policy.IEstimator;
import com.dwim.kv.policy.IEvaluator;
import com.dwim.kv.policy.KNNRewardEstimator;
import com.dwim.kv.policy.Keyword;
import com.dwim.kv.policy.RandomRewardEstimator;
import com.dwim.kv.policy.ZipfRewardEstimator;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.wrapper.DOMWrapper;
import com.dwim.wrapper.ISemanticMapping;
import com.dwim.wrapper.UnaryMaping;


public class Wikicfp {
	private String protocal;
	private String host;
	private String path;
	private int port;
	private int queryCounter;
	
	public Wikicfp() {
		queryCounter = 1;
	}
	
	
	public void config() {
		ConfigMan.DEBUG = true;	
		ConfigMan.ALL_DOCUMENT_IN_DB = ConfigMan.DEFAULT_SMALLDB_DOCUMENT;
		ConfigMan.HTTP_CONTENT_CODESET = "utf-8";
		ConfigMan.HOSTURL = "www.wikicfp.com";
		ConfigMan.SEARCH_PAGE_PATH = "/cfp/servlet/tool.search";
		ConfigMan.PROTOCAL = "http";
		ConfigMan.PORTNUM = 80;
		ConfigMan.THREAD_IN_USE = 1;
		ConfigMan.SRR_SAMPLING_RATE = 1.0;
		ConfigMan.RECORD_INDEX_DIR = "D:\\index\\wikicfp\\record";
		ConfigMan.URL_INDEX_DIR = "D:\\index\\wikicfp\\url";

		ConfigMan.CANDIDATE_SET_SIZE = 500;
		ConfigMan.TRANING_SET_SIZE = 500;
		ConfigMan.SAVE_HTML_RECORD = true;
		ConfigMan.SAVE_HTML_PATH = "D:\\records\\wikicfp";
		ConfigMan.SAVE_CANDIDATE_SET_PATH =  "D:\\records\\wikicfpcs";

		ConfigMan.CRAWLER_SLEEP_TIME = 1000;
		ConfigMan.WRAPPER_ENTRY_REXS = "(/cfp/servlet/event\\.showcfp\\?eventid=[0-9]+)";
		ConfigMan.WRAPPER_SRR_SCRIPT = new String[] {"/html/body/div[@class='contsec']/center/table/tr[2]",
				                         			 "/html/body/div[@class='contsec']/center/table/tr[3]/td/table/tr[1]",
													 "/html/body/div[@class='contsec']/center/table/tr[3]/td/table/tr[2]",
													 "/html/body/div[@class='contsec']/center/table/tr[3]/td/table/tr[3]"};
		ConfigMan.RECORD_BUFFER_SIZE = 1024;
		ConfigMan.MULTI_LANUAGE = false;
		ConfigMan.FEATURE_WEIGHT = new double[]{0,10,0,0,0,2,1,0};
		protocal = ConfigMan.PROTOCAL;
		host = ConfigMan.HOSTURL;
		port = ConfigMan.PORTNUM;
		path = ConfigMan.SEARCH_PAGE_PATH;
	

		ConfigMan.EXPERIMENTAL_MODE = true;

	}
	
	
	/**
	 * RANDOM
	 * @throws Exception
	 */
	public void crawlerTask4() throws Exception {
		EP[] epset = new EP[1];
		IElement element = new IElement("q");
		try {
			epset[0] = new EP(element);
		} catch (DWIMException e) {
			e.printStackTrace();
		}
		config();
		
		DOMWrapper wrapper = new DOMWrapper(ConfigMan.WRAPPER_ENTRY_REXS,ConfigMan.WRAPPER_SRR_SCRIPT);
		ISemanticMapping mapping = new UnaryMaping();
		wrapper.setSemanticMapping(mapping);
		
		NextRecorder recorder = new NextRecorder();
		
		PageRetrievalManager prm = new PageRetrievalManager(protocal,host,port,wrapper,recorder);
		SRRBufferPool.clear();
	
		IndexManager im = new IndexManager(RecordIndexer.getInstance());
	
		IEstimator re = new RandomRewardEstimator();
		IEvaluator gke = new GenericKeywordEvaluator(re);	
		//recorder.markDocNumbers();
	
		Keyword firstWord = new Keyword(epset[0].getIthElement(0),"com");
		//initialize the first instance
		firstWord.setDf(8);
		Query query = new Query(epset[0],firstWord);
		
		
		prm.start();
		im.start();
		
		while(queryCounter<1000) {
			//begin a new query session
			NextPager task = new NextPager(path+query.next());
			task.initialize(null, 0);	
			prm.nextTask(task);
		
			//wait until the retrieval process is completed
			while(true) {
				if(!prm.hasTask() && !SRRBufferPool.hasMore()) {	
					im.commitAndOptimizeIndex();
					break;
				} else {
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);				//wait
				}
			}
			if(ConfigMan.DEBUG)		System.out.println("¡ñ index completed...");	
			
			if(ConfigMan.DEBUG)		
				System.out.println("¡ñ Add this instance"+
				" ("+prm.getNewDocs()+"/"+ prm.getAccessDocs()+") to Traning set...");
			
			Keyword lik = query.getKeyword();					  //last issued infinite keyword
			
			if(lik != null) {
				//IEP or IMEP
				lik.setNumOfAccessDocs(prm.getAccessDocs());
				lik.setNumOfAccessNewDocus(prm.getNewDocs());	
			} else {
				//FEP
				//do nothing
			}
			
			re.addTrainingInstance(lik);
			
			if(ConfigMan.DEBUG)		System.out.println("¡ñ generating keyword Candidate Set...");	
			ArrayList<Keyword[]> cadidateSets = RecordIndexer.getInstance().dfCandidateSets();
			Keyword[] set = cadidateSets.get(0);
			
			if(ConfigMan.DEBUG)		System.out.println("¡ñ estimating rewards for keywords in Candiate Set");	
			query = gke.nextOptimalKeyword(epset[0],set);
			if(ConfigMan.DEBUG)	{
				System.out.println("¡ñ next query to issue " + query);
				System.out.println("---------------"+ ++queryCounter +"---------------");
			}

			//record the issued keyword
			im.addIssuedKeyword(query.getPattern().getID(), query.getKeyword().getWord());
			//double[] rewards = re.getRewards(set);
			/*Keyword.printTitle();
			for(int i = 0 ; i < set.length; i++) {
				if(set[i] == null)	break;
				System.out.println(set[i] + " " + rewards[i]);
			}*/
		}
		
		
	}
	
	/**
	 * DF
	 * @throws Exception
	 */
	public void crawlerTask3() throws Exception {
		EP[] epset = new EP[1];
		IElement element = new IElement("q");
		try {
			epset[0] = new EP(element);
		} catch (DWIMException e) {
			e.printStackTrace();
		}
		config();
		
		DOMWrapper wrapper = new DOMWrapper(ConfigMan.WRAPPER_ENTRY_REXS,ConfigMan.WRAPPER_SRR_SCRIPT);
		ISemanticMapping mapping = new UnaryMaping();
		wrapper.setSemanticMapping(mapping);
		
		NextRecorder recorder = new NextRecorder();
		
		PageRetrievalManager prm = new PageRetrievalManager(protocal,host,port,wrapper,recorder);
		SRRBufferPool.clear();
	
		IndexManager im = new IndexManager(RecordIndexer.getInstance());
	
		IEstimator re = new GenericDFEstimator();
		IEvaluator gke = new GenericKeywordEvaluator(re);	
		//recorder.markDocNumbers();
	
		Keyword firstWord = new Keyword(epset[0].getIthElement(0),"com");
		//initialize the first instance
		firstWord.setDf(8);
		Query query = new Query(epset[0],firstWord);
		
		
		prm.start();
		im.start();
		
		while(queryCounter<1000) {
			//begin a new query session
			NextPager task = new NextPager(path+query.next());
			task.initialize(null, 0);	
			prm.nextTask(task);
		
			//wait until the retrieval process is completed
			while(true) {
				if(!prm.hasTask() && !SRRBufferPool.hasMore()) {	
					im.commitAndOptimizeIndex();
					break;
				} else {
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);				//wait
				}
			}
			if(ConfigMan.DEBUG)		System.out.println("¡ñ index completed...");	
			
			if(ConfigMan.DEBUG)		
				System.out.println("¡ñ Add this instance"+
				" ("+prm.getNewDocs()+"/"+ prm.getAccessDocs()+") to Traning set...");
			
			Keyword lik = query.getKeyword();					  //last issued infinite keyword
			
			if(lik != null) {
				//IEP or IMEP
				lik.setNumOfAccessDocs(prm.getAccessDocs());
				lik.setNumOfAccessNewDocus(prm.getNewDocs());	
			} else {
				//FEP
				//do nothing
			}
			
			re.addTrainingInstance(lik);
			
			if(ConfigMan.DEBUG)		System.out.println("¡ñ generating keyword Candidate Set...");	
			ArrayList<Keyword[]> cadidateSets = RecordIndexer.getInstance().dfCandidateSets();
			Keyword[] set = cadidateSets.get(0);
			
			if(ConfigMan.DEBUG)		System.out.println("¡ñ estimating rewards for keywords in Candiate Set");	
			query = gke.nextOptimalKeyword(epset[0],set);
			if(ConfigMan.DEBUG)	{
				System.out.println("¡ñ next query to issue " + query);
				System.out.println("---------------"+ ++queryCounter +"---------------");
			}

			//record the issued keyword
			im.addIssuedKeyword(query.getPattern().getID(), query.getKeyword().getWord());
			//double[] rewards = re.getRewards(set);
			/*Keyword.printTitle();
			for(int i = 0 ; i < set.length; i++) {
				if(set[i] == null)	break;
				System.out.println(set[i] + " " + rewards[i]);
			}*/
		}
		
		
	}
	/**
	 * Zipf
	 * @throws Exception
	 */
	public void crawlerTask2() throws Exception {
		EP[] epset = new EP[1];
		IElement element = new IElement("q");
		try {
			epset[0] = new EP(element);
		} catch (DWIMException e) {
			e.printStackTrace();
		}
		config();
		
	
	
		
		DOMWrapper wrapper = new DOMWrapper(ConfigMan.WRAPPER_ENTRY_REXS,ConfigMan.WRAPPER_SRR_SCRIPT);
		ISemanticMapping mapping = new UnaryMaping();
		wrapper.setSemanticMapping(mapping);
		
		NextRecorder recorder = new NextRecorder();
		
		PageRetrievalManager prm = new PageRetrievalManager(protocal,host,port,wrapper,recorder);
		SRRBufferPool.clear();
	
		IndexManager im = new IndexManager(RecordIndexer.getInstance());
	
		FeatureExtractor fe = new FeatureExtractor();
		IEstimator re = new ZipfRewardEstimator();
		IEvaluator gke = new GenericKeywordEvaluator(re);	
		//recorder.markDocNumbers();
	
		Keyword firstWord = new Keyword(epset[0].getIthElement(0),"com");
		//initialize the first instance
		firstWord.setDf(8);
		firstWord.setDfrank(1);
		firstWord = fe.extractFeatures(firstWord);
		Query query = new Query(epset[0],firstWord);
		
		
		prm.start();
		im.start();
		
		while(queryCounter<1200) {
			//begin a new query session
			NextPager task = new NextPager(path+query.next());
			task.initialize(null, 0);	
			prm.nextTask(task);
		
			//wait until the retrieval process is completed
			while(true) {
				if(!prm.hasTask() && !SRRBufferPool.hasMore()) {	
					im.commitAndOptimizeIndex();
					break;
				} else {
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);				//wait
				}
			}
			if(ConfigMan.DEBUG)		System.out.println("¡ñ index completed...");	
			
			if(ConfigMan.DEBUG)		
				System.out.println("¡ñ Add this instance"+
				" ("+prm.getNewDocs()+"/"+ prm.getAccessDocs()+") to Traning set...");
			
			Keyword lik = query.getKeyword();					  //last issued infinite keyword
			
			if(lik != null) {
				//IEP or IMEP
				lik.setNumOfAccessDocs(prm.getAccessDocs());
				lik.setNumOfAccessNewDocus(prm.getNewDocs());	
			} else {
				//FEP
				//do nothing
			}
			
			re.addTrainingInstance(lik);
			
			if(ConfigMan.DEBUG)		System.out.println("¡ñ generating keyword Candidate Set...");	
			ArrayList<Keyword[]> cadidateSets = RecordIndexer.getInstance().dfCandidateSets();
			Keyword[] set = cadidateSets.get(0);
			
			if(ConfigMan.DEBUG)		System.out.println("¡ñ estimating rewards for keywords in Candiate Set");	
			query = gke.nextOptimalKeyword(epset[0],set);
			if(ConfigMan.DEBUG)	{
				System.out.println("---------------"+ ++queryCounter +"---------------");
			}

			//record the issued keyword
			im.addIssuedKeyword(query.getPattern().getID(), query.getKeyword().getWord());
			//double[] rewards = re.getRewards(set);
			/*Keyword.printTitle();
			for(int i = 0 ; i < set.length; i++) {
				if(set[i] == null)	break;
				System.out.println(set[i] + " " + rewards[i]);
			}*/
		}
		
		
	}
	
	/**
	 * nearest neighbor
	 * @throws Exception
	 */
	public void crawlerTask1() throws Exception {
		EP[] epset = new EP[1];
		IElement element = new IElement("q");
		try {
			epset[0] = new EP(element);
		} catch (DWIMException e) {
			e.printStackTrace();
		}
		config();
		
	
	
		
		DOMWrapper wrapper = new DOMWrapper(ConfigMan.WRAPPER_ENTRY_REXS,ConfigMan.WRAPPER_SRR_SCRIPT);
		ISemanticMapping mapping = new UnaryMaping();
		wrapper.setSemanticMapping(mapping);
		
		NextRecorder recorder = new NextRecorder();
		
		PageRetrievalManager prm = new PageRetrievalManager(protocal,host,port,wrapper,recorder);
		SRRBufferPool.clear();
	
		IndexManager im = new IndexManager(RecordIndexer.getInstance());
	
		FeatureExtractor fe = new FeatureExtractor();
		IEstimator re = new KNNRewardEstimator();
		IEvaluator gke = new GenericKeywordEvaluator(re);	
		//recorder.markDocNumbers();
	
		Keyword firstWord = new Keyword(epset[0].getIthElement(0),"com");
		//initialize the first instance
		firstWord.addTagInDom("e0-0");
		firstWord.setDf(8);
		firstWord.setDf(8);
		firstWord.setRidf(0.6);
		firstWord = fe.extractFeatures(firstWord);
		Query query = new Query(epset[0],firstWord);
		
		
		prm.start();
		im.start();
		
		while(queryCounter<1200) {
			//begin a new query session
			NextPager task = new NextPager(path+query.next());
			task.initialize(null, 0);	
			prm.nextTask(task);
		
			//wait until the retrieval process is completed
			while(true) {
				if(!prm.hasTask() && !SRRBufferPool.hasMore()) {	
					im.commitAndOptimizeIndex();
					break;
				} else {
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);				//wait
				}
			}
			if(ConfigMan.DEBUG)		System.out.println("¡ñ index completed...");	
			
			if(ConfigMan.DEBUG)		
				System.out.println("¡ñ Add this instance"+
				" ("+prm.getNewDocs()+"/"+ prm.getAccessDocs()+") to Traning set...");
			
			Keyword lik = query.getKeyword();					  //last issued infinite keyword
			
			if(lik != null) {
				//IEP or IMEP
				lik.setNumOfAccessDocs(prm.getAccessDocs());
				lik.setNumOfAccessNewDocus(prm.getNewDocs());	
			} else {
				//FEP
				//do nothing
			}
			
			re.addTrainingInstance(lik);
			
			if(ConfigMan.DEBUG)		System.out.println("¡ñ generating keyword Candidate Set...");	
			ArrayList<Keyword[]> cadidateSets = RecordIndexer.getInstance().dfCandidateSets();
			Keyword[] set = cadidateSets.get(0);
			
		
			
			if(ConfigMan.DEBUG)		System.out.println("   ¡ñ extarcting features...");	
			set = fe.extractFeatures(set);
			if(ConfigMan.DEBUG)		System.out.println("¡ñ estimating rewards for keywords in Candiate Set");	
			query = gke.nextOptimalKeyword(epset[0],set);
			if(ConfigMan.DEBUG)	{
				System.out.println("¡ñ next query to issue " + query);
				System.out.println("---------------"+ ++queryCounter +"---------------");
			}

			//record the issued keyword
			im.addIssuedKeyword(query.getPattern().getID(), query.getKeyword().getWord());
			//double[] rewards = re.getRewards(set);
			/*Keyword.printTitle();
			for(int i = 0 ; i < set.length; i++) {
				if(set[i] == null)	break;
				System.out.println(set[i] + " " + rewards[i]);
			}*/
		}
		
		
	}
	
	
	public static void main(String args[]) throws Exception {
		Wikicfp crawler = new Wikicfp();
		crawler.crawlerTask4();
		
		
	}
}

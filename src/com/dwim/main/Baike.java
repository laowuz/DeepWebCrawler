package com.dwim.main;

import java.util.ArrayList;

import org.ictclas4j.bean.SegResult;
import org.ictclas4j.segment.SegTag;

import com.dwim.form.analysis.EP;
import com.dwim.form.analysis.Element;
import com.dwim.form.analysis.FElement;
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
import com.dwim.kv.policy.ZipfRewardEstimator;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.wrapper.DOMWrapper;
import com.dwim.wrapper.ISemanticMapping;
import com.dwim.wrapper.RegulaExperssionWrapper;
import com.dwim.wrapper.UnaryMaping;

public class Baike {
	private String protocal;
	private String host;
	private String path;
	private int port;
	private int queryCounter;
	private String pageElement;
	private int pageOffset;
	
	public void config() {
		ConfigMan.DEBUG = true;	
		ConfigMan.ALL_DOCUMENT_IN_DB = ConfigMan.DEFAULT_LARGEMDB_DOCUMENT;
		ConfigMan.HTTP_CONTENT_CODESET = "gb2312";
		ConfigMan.SAVE_HTML_CODESET = "gb2312";
		ConfigMan.HOSTURL = "baike.baidu.com";
		ConfigMan.SEARCH_PAGE_PATH = "/taglist";
		ConfigMan.PROTOCAL = "http";
		ConfigMan.PORTNUM = 80;
		ConfigMan.THREAD_IN_USE = 1;
		ConfigMan.SRR_SAMPLING_RATE = 0.1;
		ConfigMan.RECORD_INDEX_DIR = "C:\\备份信息\\index\\baike\\record";
		ConfigMan.URL_INDEX_DIR = "C:\\备份信息\\index\\baike\\url";

		ConfigMan.CANDIDATE_SET_SIZE = 1024;
		ConfigMan.TRANING_SET_SIZE = 512;
		ConfigMan.SAVE_HTML_RECORD = true;
		ConfigMan.SAVE_HTML_PATH = "C:\\备份信息\\records\\baike";
		ConfigMan.SAVE_CANDIDATE_SET_PATH =  "C:\\备份信息\\records\\baikecs";

		ConfigMan.CRAWLER_SLEEP_TIME = 1000;
		ConfigMan.WRAPPER_ENTRY_REXS = "(/view/.+\\.html)";
		ConfigMan.WRAPPER_SRR_SCRIPT = new String[] {"(?s)<div class=\"bpctrl\"></div><h2 class=\"first\">(.*)<dt>开放分类",
													 "(?s)<dt>开放分类：</dt>(.*?)</dl>"};
		ConfigMan.RECORD_BUFFER_SIZE = 512;
		ConfigMan.MULTI_LANUAGE = false;
		
		ConfigMan.FEATURE_WEIGHT = new double[]{0,0,8,2,0,0,2,0};
		ConfigMan.LIMIT_RESULT = true;
		
		protocal = ConfigMan.PROTOCAL;
		host = ConfigMan.HOSTURL;
		port = ConfigMan.PORTNUM;
		path = ConfigMan.SEARCH_PAGE_PATH;
		
		ConfigMan.K_NEARST_NEIGHBOR = 3;
		ConfigMan.USE_STEM = false;
		ConfigMan.EXPERIMENTAL_MODE = true;
		ConfigMan.MULTI_LANUAGE = true;
		//temp
		pageElement = "offset";
		pageOffset = 10;
	}
	
	/**
	 * DF
	 * @throws Exception
	 */
	public void crawlerTask3() throws Exception {
		EP[] epset = new EP[1];
		IElement element1 = new IElement("tag");
		ArrayList<Element> elements = new ArrayList<Element>(1);
		elements.add(element1);
		
		try {
			epset[0] = new EP(elements);
		} catch (DWIMException e) {
			e.printStackTrace();
		}
		config();
		
	
		
		RegulaExperssionWrapper wrapper = new RegulaExperssionWrapper(ConfigMan.WRAPPER_ENTRY_REXS,ConfigMan.WRAPPER_SRR_SCRIPT);
		ISemanticMapping mapping = new UnaryMaping();
		wrapper.setSemanticMapping(mapping);
		
		NextRecorder recorder = new NextRecorder();
		
		PageRetrievalManager prm = new PageRetrievalManager(protocal,host,port,wrapper,recorder);
		SRRBufferPool.clear();
	
		IndexManager im = new IndexManager(RecordIndexer.getInstance());
	

		IEstimator re = new GenericDFEstimator();
		IEvaluator gke = new GenericKeywordEvaluator(re);	
		//recorder.markDocNumbers();
	
		Keyword firstWord = new Keyword(epset[0].getIthElement(0),"音乐");
		//initialize the first instance
		firstWord.setDf(750);
		firstWord.setNdfrank(0.8);
		Query query = new Query(epset[0],firstWord);
		
		
		prm.start();
		im.start();
		String nextUrl = query.next();
		while(queryCounter < 1000) {
			//begin a new query session
			NextPager task = new NextPager(path + nextUrl);
			task.initialize(pageElement, pageOffset);	
			prm.nextTask(task);
		
			//wait until the retrieval process is completed
			while(true) {
				if(!prm.hasTask() && !SRRBufferPool.hasMore()) {
					
					im.commitAndOptimizeIndex();
					break;
				} else {
					System.out.println("more record to index " + SRRBufferPool.hasMore());
					System.out.println("indexer " + im.isAlive());
					if(!im.isAlive())
						im.start();
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);				//wait
				}
			}
			if(ConfigMan.DEBUG)		System.out.println("● index completed...");	
			
			while(query.hasNext()) {
				System.err.println("err");
			}
			
			if(ConfigMan.DEBUG)		
				System.out.println("● Add this instance"+
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
			
			if(ConfigMan.DEBUG)		System.out.println("● generating keyword Candidate Set...");	
			ArrayList<Keyword[]> cadidateSets = RecordIndexer.getInstance().dfCandidateSets();
			Keyword[] set = cadidateSets.get(0);
			
		
			if(ConfigMan.DEBUG)		System.out.println("● estimating rewards for keywords in Candiate Set");	
			query = gke.nextOptimalKeyword(epset[0],set);

			
			
	
			nextUrl = query.next();
			
			
			
			if(ConfigMan.DEBUG)	{
				System.out.println("● next query to issue " + nextUrl);
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
	
	
	public void crawlerTask2() throws Exception {
		EP[] epset = new EP[1];
		IElement element1 = new IElement("tag");
		ArrayList<Element> elements = new ArrayList<Element>(1);
		elements.add(element1);
		
		try {
			epset[0] = new EP(elements);
		} catch (DWIMException e) {
			e.printStackTrace();
		}
		config();
		
	
		FeatureExtractor fe = new FeatureExtractor();
		RegulaExperssionWrapper wrapper = new RegulaExperssionWrapper(ConfigMan.WRAPPER_ENTRY_REXS,ConfigMan.WRAPPER_SRR_SCRIPT);
		ISemanticMapping mapping = new UnaryMaping();
		wrapper.setSemanticMapping(mapping);
		
		NextRecorder recorder = new NextRecorder();
		
		PageRetrievalManager prm = new PageRetrievalManager(protocal,host,port,wrapper,recorder);
		SRRBufferPool.clear();
	
		IndexManager im = new IndexManager(RecordIndexer.getInstance());
	

		IEstimator re = new ZipfRewardEstimator();
		IEvaluator gke = new GenericKeywordEvaluator(re);	
		//recorder.markDocNumbers();
	
		Keyword firstWord = new Keyword(epset[0].getIthElement(0),"音乐");
		//initialize the first instance
		firstWord.setDf(750);
		firstWord.setNdfrank(0.8);
		Query query = new Query(epset[0],firstWord);
		
		
		prm.start();
		im.start();
		String nextUrl = query.next();
		while(queryCounter < 1000) {
			//begin a new query session
			NextPager task = new NextPager(path + nextUrl);
			task.initialize(pageElement, pageOffset);	
			prm.nextTask(task);
		
			//wait until the retrieval process is completed
			while(true) {
				if(!prm.hasTask() && !SRRBufferPool.hasMore()) {
					
					im.commitAndOptimizeIndex();
					break;
				} else {
					System.out.println("more record to index " + SRRBufferPool.hasMore());
					System.out.println("indexer " + im.isAlive());
					if(!im.isAlive())
						im.start();
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);				//wait
				}
			}
			if(ConfigMan.DEBUG)		System.out.println("● index completed...");	
			
			while(query.hasNext()) {
				System.err.println("err");
			}
			
			if(ConfigMan.DEBUG)		
				System.out.println("● Add this instance"+
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
			
			if(ConfigMan.DEBUG)		System.out.println("● generating keyword Candidate Set...");	
			ArrayList<Keyword[]> cadidateSets = RecordIndexer.getInstance().dfCandidateSets();
			Keyword[] set = cadidateSets.get(0);
			
		
			if(ConfigMan.DEBUG)		System.out.println("● estimating rewards for keywords in Candiate Set");	
			query = gke.nextOptimalKeyword(epset[0],set);

			
			
	
			nextUrl = query.next();
			
			
			
			if(ConfigMan.DEBUG)	{
				System.out.println("● next query to issue " + nextUrl);
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
	
	
	public void crawlerTask1() throws Exception {
		EP[] epset = new EP[1];
		IElement element1 = new IElement("tag");
		ArrayList<Element> elements = new ArrayList<Element>(1);
		elements.add(element1);
		
		try {
			epset[0] = new EP(elements);
		} catch (DWIMException e) {
			e.printStackTrace();
		}
		config();
		
	
		FeatureExtractor fe = new FeatureExtractor();
		RegulaExperssionWrapper wrapper = new RegulaExperssionWrapper(ConfigMan.WRAPPER_ENTRY_REXS,ConfigMan.WRAPPER_SRR_SCRIPT);
		ISemanticMapping mapping = new UnaryMaping();
		wrapper.setSemanticMapping(mapping);
		
		NextRecorder recorder = new NextRecorder();
		
		PageRetrievalManager prm = new PageRetrievalManager(protocal,host,port,wrapper,recorder);
		SRRBufferPool.clear();
	
		IndexManager im = new IndexManager(RecordIndexer.getInstance());
	

		IEstimator re = new KNNRewardEstimator();
		IEvaluator gke = new GenericKeywordEvaluator(re);	
		//recorder.markDocNumbers();
	
		Keyword firstWord = new Keyword(epset[0].getIthElement(0),"音乐");
		//initialize the first instance
		firstWord.addTagInDom("e0-1");
		firstWord.setDf(750);
		firstWord.setTf(750);
		firstWord.setNdfrank(0.8);
		firstWord = fe.extractFeatures(firstWord);
		Query query = new Query(epset[0],firstWord);
		
		
		prm.start();
		im.start();
		String nextUrl = query.next();
		while(queryCounter < 1000) {
			//begin a new query session
			NextPager task = new NextPager(path + nextUrl);
			task.initialize(pageElement, pageOffset);	
			prm.nextTask(task);
		
			//wait until the retrieval process is completed
			while(true) {
				if(!prm.hasTask() && !SRRBufferPool.hasMore()) {
					
					im.commitAndOptimizeIndex();
					break;
				} else {
					System.out.println("more record to index " + SRRBufferPool.hasMore());
					System.out.println("indexer " + im.isAlive());
					if(!im.isAlive())
						im.start();
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);				//wait
				}
			}
			if(ConfigMan.DEBUG)		System.out.println("● index completed...");	
			
			while(query.hasNext()) {
				System.err.println("err");
			}
			
			if(ConfigMan.DEBUG)		
				System.out.println("● Add this instance"+
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
			
			if(ConfigMan.DEBUG)		System.out.println("● generating keyword Candidate Set...");	
			ArrayList<Keyword[]> cadidateSets = RecordIndexer.getInstance().dfCandidateSets();
			Keyword[] set = cadidateSets.get(0);
			
		
			
			if(ConfigMan.DEBUG)		System.out.println("   ● extarcting features...");	
			set = fe.extractFeatures(set);
			if(ConfigMan.DEBUG)		System.out.println("● estimating rewards for keywords in Candiate Set");	
			query = gke.nextOptimalKeyword(epset[0],set);

			
			
	
			nextUrl = query.next();
			
			
			
			if(ConfigMan.DEBUG)	{
				System.out.println("● next query to issue " + nextUrl);
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
		Baike crawler = new Baike();
		crawler.crawlerTask3();
		

	}
}

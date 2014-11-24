package com.dwim.index;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.index.CorruptIndexException;

import com.dwim.util.ConfigMan;

public class IndexManager extends Thread{
	private RecordIndexer rindexer;
	//private boolean terminate;

	public IndexManager(RecordIndexer in) {
		rindexer = in;
	}
	
	public void run() {
		while(true) {
			ArrayList<SearchResultRecord> srrs =  SRRBufferPool.getSSRs();
			System.out.println("Indexer is alive");
			try {
				if(srrs ==null || srrs.size() == 0) {
					rindexer.commitIndex();
					try {
						Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);
				
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
				
					rindexer.index(srrs);
					if(ConfigMan.DEBUG)	
						System.out.println("¡ú Record Indexer indexed " + srrs.size() + " records");
					Thread.sleep(ConfigMan.CRAWLER_SLEEP_TIME * 5);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			} catch (CorruptIndexException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public void commitAndOptimizeIndex() throws CorruptIndexException, IOException {
		rindexer.commitIndex();
		rindexer.optimizeIndex();
	}
	
	public void collapse() throws CorruptIndexException, IOException {
		rindexer.closeIndex();
		IDIndexer.getInstance().closeIndex();
	}
	
	public void addIssuedKeyword(int patternid, String keyword) {
		rindexer.issued(patternid, keyword);
	}
	
}

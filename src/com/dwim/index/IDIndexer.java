package com.dwim.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NoLockFactory;

import com.dwim.util.ConfigMan;

public class IDIndexer {
	
	private Analyzer analyzer;
	private IndexWriter iwriter;
	private IndexReader ireader;
	private File indexDir;
	public int numOfIDs;							//all unique documents retrieved
	
	private static IDIndexer instance;

	
	private IDIndexer() throws IOException {
		this(new File(ConfigMan.URL_INDEX_DIR),ConfigMan.RECORD_BUFFER_SIZE);

	}


	private IDIndexer(File indexdir, int buffer) throws IOException {
		numOfIDs = 0;
		indexDir = indexdir;
		analyzer = new WhitespaceAnalyzer();
		IndexWriter.MaxFieldLength mfl = new IndexWriter.MaxFieldLength(512);
		//create a memory-mapped IO index

		iwriter = new IndexWriter(new MMapDirectory(indexdir,new NoLockFactory()),analyzer,true,mfl);
		iwriter.setMaxBufferedDocs(buffer);
		iwriter.setMergeFactor(buffer);	
		
		instance = null;
		//initialize a hash map for response record id
	}
	
	public static IDIndexer getInstance() throws IOException {
		if(instance == null)
			instance = new IDIndexer();
		return instance;
		
	}
	
	public static IDIndexer getInstance(File indexdir, int buffer) throws IOException {
		if(instance == null)
			instance = new IDIndexer(indexdir, buffer);
		return instance;
		
	}
	
	public void indexWCF(String url) throws CorruptIndexException, IOException {
		url = format(url);
		if(this.exists(url))	return;
		Document doc = new Document();
		doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		iwriter.addDocument(doc);
		numOfIDs++;
	}
	
	public void indexWFormat(String url) throws CorruptIndexException, IOException {
		url = format(url);
		Document doc = new Document();
		doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		iwriter.addDocument(doc);
		numOfIDs++;
	}
	
	public void indexWCheck(String url) throws CorruptIndexException, IOException {
		url = format(url);
		if(this.exists(url))	return;
		Document doc = new Document();
		doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		iwriter.addDocument(doc);
		numOfIDs++;
	}
	
	/**
	 * @expert As there is no replication within records brought by a query session.
	 * To accelerate the speed the indexer does not have to commit after each index
	 * @param url
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void index(String url) throws CorruptIndexException, IOException {
		
		Document doc = new Document();
		doc.add(new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED));
		iwriter.addDocument(doc);
		numOfIDs++;
	}
	
	public void closeIndex() throws CorruptIndexException, IOException {
		//when the index is stable optimize the index then close the index
		iwriter.commit();
		iwriter.optimize();
		iwriter.close();
	}
	
	public void commitIndex() throws CorruptIndexException, IOException {
		iwriter.commit();
		
	}
	
	public void optimize() throws CorruptIndexException, IOException {
		iwriter.optimize();
	}
	
	/**
	 * Note of the case. the result is Case sensitive 
	 * @param url
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public boolean exists(String url) throws CorruptIndexException, IOException {	
		ireader = IndexReader.open(FSDirectory.open(indexDir),true);
		Term term = new Term("url",url);
		TermDocs docs = ireader.termDocs(term);
		boolean result = docs.next();
		ireader.close();
		return result;
	}
	
	/**
	 * exist check and format the input
	 * @param url
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public boolean existsWFormat(String url) throws CorruptIndexException, IOException {
		ireader = IndexReader.open(FSDirectory.open(indexDir),true);
		Term term = new Term("url",url);
		TermDocs docs = ireader.termDocs(term);
		boolean result = docs.next();
		ireader.close();
		return result;
	}
	
	public String format(String url) {
		return url.toLowerCase();
	}
	
	public void clear() throws IOException {
		iwriter.deleteAll();
		iwriter.commit();
		numOfIDs = 0;
	}
	
	public static void main(String args[]) {
		
	}
	
	
}

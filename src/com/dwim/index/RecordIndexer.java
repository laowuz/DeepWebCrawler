package com.dwim.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.store.NoLockFactory;
import org.ictclas4j.bean.POSWord;
import org.ictclas4j.segment.SegTag;

import com.dwim.form.analysis.Element;
import com.dwim.form.analysis.IElement;

import com.dwim.kv.policy.Keyword;
import com.dwim.util.ConfigMan;
import com.dwim.util.Stemmer;

public class RecordIndexer {
	
	private Analyzer analyzer;
	private IndexWriter iwriter;
	private File indexDir;
	private SegTag cnSegTag;
	private Stemmer stemmer;
	private HashMap<String,String> issuedWordIndex;

	private int[] dfList;
	private static RecordIndexer instance;
	
	

	
	private RecordIndexer() throws IOException {
		this(new File(ConfigMan.RECORD_INDEX_DIR),ConfigMan.RECORD_BUFFER_SIZE);
		
	}

	private RecordIndexer(File indexdir, int buffer) throws IOException {
		indexDir = indexdir;
		analyzer = new WhitespaceAnalyzer();
		IndexWriter.MaxFieldLength mfl = new IndexWriter.MaxFieldLength(25000);
		//create a memory-mapped IO index
		iwriter = new IndexWriter(new MMapDirectory(indexdir,new NoLockFactory()),analyzer,true,mfl);
		iwriter.setMaxBufferedDocs(buffer);
		iwriter.setMergeFactor(buffer);	
		stemmer = new Stemmer();
		//issued word index
		issuedWordIndex = new HashMap<String,String>(512*8);
		//intialize POS Tagger
		if(ConfigMan.MULTI_LANUAGE == true)
			cnSegTag = new SegTag(1);
		else
			cnSegTag = null;
		//df rank array
		dfList = new int [ConfigMan.DF_RANK_BUFFER_SIZE];
		instance = null;
	}
	
	public static RecordIndexer getInstance() throws IOException {
		if(instance == null)
			instance = new RecordIndexer();
		return instance;
		
	}
	
	public static RecordIndexer getInstance(File indexdir, int buffer) throws IOException {
		if(instance == null)
			instance = new RecordIndexer(indexdir, buffer);
		return instance;
		
	}
	
	

	public void index(ArrayList<SearchResultRecord> srrs) throws CorruptIndexException, IOException {
		for(int i = 0 ; i < srrs.size() ; i++) {
			if(srrs.get(i) != null)
				this.index(srrs.get(i));
		}
	}
	
	/**
	 * Purely for test
	 * @param srr
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	//private modify
	protected void indexTest(String testin) throws CorruptIndexException, IOException {
	
		Document doc = new Document();
			doc.add(new Field("e"+0+"ot", testin, Field.Store.YES, Field.Index.NO));
			//index parsed text
			doc.add(new Field("e"+0+"pt", parse(testin), Field.Store.YES, Field.Index.ANALYZED));
			iwriter.addDocument(doc);
	}
	
	/**
	 * Note the index method does NOT perform the replication test.
	 * @param srr
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public void index(SearchResultRecord srr) throws CorruptIndexException, IOException {
	
		Document doc = new Document();
		//divide to IEP and FEP and conqure
		if(ConfigMan.USE_STEM) {
			//parse with stem
			for(int i = 0 ; i < srr.numOfElements(); i++) {
				ArrayList<String> contents = srr.getValues(i);
				for(int j = 0 ; j < contents.size() ; j++) {
					//index original text
					//doc.add(new Field("e"+i+"ot"+j, contents.get(j), Field.Store.YES, Field.Index.NO));
					//index parsed text
					doc.add(new Field("e"+i+"pt"+j, parse(contents.get(j)), Field.Store.YES, Field.Index.ANALYZED));
				}
			}
		} else {
			//parse without stem
			for(int i = 0 ; i < srr.numOfElements(); i++) {
				ArrayList<String> contents = srr.getValues(i);
				for(int j = 0 ; j < contents.size() ; j++) {
					//index original text
					//doc.add(new Field("e"+i+"ot"+j, contents.get(j), Field.Store.YES, Field.Index.NO));
					//index parsed text
					doc.add(new Field("e"+i+"pt"+j, this.parseWithoutStem(contents.get(j)), Field.Store.YES, Field.Index.ANALYZED));
				}
			}
		}
		iwriter.addDocument(doc);	
	}
	
	public void closeIndex() throws CorruptIndexException, IOException {
		//when the index is stable optimize the index then close the index
		iwriter.commit();
		iwriter.optimize();
		iwriter.close(true);
	}
	
	public void commitIndex() throws CorruptIndexException, IOException {
		iwriter.commit();
		
	}
	
	public void optimizeIndex() throws CorruptIndexException, IOException {
		iwriter.optimize();
		
	}
	

	
	/**
	 * The first phase is the format and pos tager result 
	 * followed by to be indexed words
	 * @critical:Due to the method will be invoked with significant times, some tricks are hired to
	 * accelerate the speed of the method.
	 * @param in
	 * @return
	 */
	private String parse(String in) {
		ArrayList<String> tokens = null;
		if(ConfigMan.MULTI_LANUAGE)
			tokens = formatCn(in);
		else
			tokens = formatEn(in);
		
		StringBuffer sb = new StringBuffer(128);
		for(int i = 0 ; i < tokens.size() ; i++) {
			//Instead of using Language Detector, a rough detector is hired to accelerate the speed.
			if(tokens.get(i).matches("[\\u0021-\\u007f]+")) {
				//English and Latin Token
				//POS tag of English token is postponed to the candidate set analysis phase.
				stemmer.add(tokens.get(i).toCharArray(),tokens.get(i).length());
				stemmer.stem();
				sb.append(stemmer.toString());
			}
			else if(ConfigMan.MULTI_LANUAGE&&tokens.get(i).matches(".*[\u4E00-\u9FA5]+.*")) {
				//Chinese Token
				//Split words
				//Also Japanese
				ArrayList<POSWord> words;
				words = cnSegTag.split(tokens.get(i)).toPOSWord();
				for(int j = 0 ; j < words.size() ; j++) {
					if(sb.length() > 0) sb.append(" ");
					sb.append(words.get(j).getContent());
				}
			}// else if(ConfigMan.MULTI_LANUAGE&&tokens.get(i).matches(".*[\u0800-\u4e00]+.*"))
			else {
				//Unsupported char set
				//Left alone
				sb.append(tokens.get(i));
			}
			 sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);		//delete the last white space
		return sb.toString();
	}
	

	private String parseWithoutStem(String in) {
		
		ArrayList<String> tokens = null;
		if(ConfigMan.MULTI_LANUAGE)
			tokens = formatCn(in);
		else
			tokens = formatEn(in);
		
		StringBuffer sb = new StringBuffer(128);
		for(int i = 0 ; i < tokens.size() ; i++) {
			//Instead of using Language Detector, a rough detector is hired to accelerate the speed.
			if(tokens.get(i).matches("[\\u0021-\\u007f]+")) {
				//English and Latin Token
				sb.append(tokens.get(i));
			}
			else if(ConfigMan.MULTI_LANUAGE&&tokens.get(i).matches(".*[\u4E00-\u9FA5]+.*")) {
				//Chinese Token
				//Split words
				//System.out.println(tokens.get(i));
				ArrayList<POSWord> words;
				words = cnSegTag.split(tokens.get(i)).toPOSWord();
				for(int j = 0 ; j < words.size() ; j++) {
					if(sb.length() > 0) sb.append(" ");
					sb.append(words.get(j).getContent());
				}
			}
			else {
				//Unsupported char set
				//Left alone
				sb.append(tokens.get(i));
			}
			 sb.append(" ");
		}
		if(sb.length() == 0) return " ";
		sb.deleteCharAt(sb.length()-1);		//delete the last white space
		return sb.toString();
	}


	
	protected ArrayList<String> formatEn(String in) {

		int pos = 0;
		ArrayList<String> result = new ArrayList<String>(16);
		char[] ins = in.toCharArray();
		for(int i = 0 ; i < ins.length ; i++) {
			if(ins[i] >= 'A' && ins[i] <= 'Z') {
				ins[i] = (char) (ins[i] + 32);
			}
			else if(ins[i] == ' ' || ins[i] == '　') {
				if(pos != i) {
					result.add(new String(ins,pos,i-pos));
					pos = i+1;
				}
				else {
					pos = i+1;
				}
			}
			else if(ins[i] == ',' || ins[i] == '.' || ins[i] == ';' ||
					ins[i] == ':' || ins[i] == '(' || ins[i] == ')' ||
					ins[i] == '"') {
				result.add(new String(ins,pos,i-pos));
				pos = i+1;
			}
			else if(i < ins.length -7 && ins[i] == '&' && ins[i+1] == 'n') {
				if(ins[i+2] == 'b' && ins[i+3] == 's' && ins[i+4] == 'p'&& ins[i+5] == ';') {
					result.add(new String(ins,pos,i-pos));
					i += 5;
					pos = i+1;
				}
			}
		}
		//retrive the last token
		if(pos != ins.length) {
			result.add(new String(ins,pos,ins.length-pos));
		}

		return result;
	}
	
	protected ArrayList<String> formatCn(String in) {

		int pos = 0;
		ArrayList<String> result = new ArrayList<String>(16);
		char[] ins = in.toCharArray();
		for(int i = 0 ; i < ins.length ; i++) {
			if(ins[i] >= 'A' && ins[i] <= 'Z') {
				ins[i] = (char) (ins[i] + 32);
			}
			else if(ins[i] == ' ' || ins[i] == '　') {
				if(pos != i) {
					result.add(new String(ins,pos,i-pos));
					pos = i+1;
				}
				else {
					pos = i+1;
				}
			}
			else if(ins[i] == ',' || ins[i] == '.' || ins[i] == ';' ||
					ins[i] == ':' || ins[i] == '(' || ins[i] == ')' ||
					ins[i] == '"' || ins[i] == '，' || ins[i] == '；' ||
					ins[i] == '：' || ins[i] == '（' || ins[i] == '）' ||
					ins[i] == '“' || ins[i] == '“'|| ins[i] == '-') {
				result.add(new String(ins,pos,i-pos));
				pos = i+1;
			}
			else if(i < ins.length -7 && ins[i] == '&' && ins[i+1] == 'n') {
				if(ins[i+2] == 'b' && ins[i+3] == 's' && ins[i+4] == 'p'&& ins[i+5] == ';') {
					result.add(new String(ins,pos,i-pos));
					i += 5;
					pos = i+1;
				}
			}
		}
		//retrive the last token
		if(pos != ins.length) {
			result.add(new String(ins,pos,ins.length-pos));
		}

		return result;
	}
	
	public void issued(int patternid, String word) {
		String pid = "e" + patternid + "pt";
		issuedWordIndex.put(word, pid);
	}
	
	
	/**
	 * 
	 * @return DF buffer The candidate sets corresponding to the infinite elements.
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public ArrayList<Keyword[]> dfCandidateSets() throws CorruptIndexException, IOException {
		//calculate the number of candidate sets to be returned.
		ArrayList<IElement> elements = IElement.getALLIElements();
		//As the elements name usually will be checked for thousands of times here we use String array instead of ArrayList<String>
		int iel = IElement.ielementsNum();					//infinite elements length
	
		String[] eleNames = new String[iel];
		int[] eleIndexs = new int[iel];
		
		for(int i = 0, j = 0; i < elements.size() ; i++) {
			if(elements.get(i) instanceof IElement) {
				eleNames[j] = "e" + elements.get(i).getId() + "pt";
				eleIndexs[j++] = i;
			}
		}
		//load index
		IndexReader reader= IndexReader.open(FSDirectory.open(indexDir, new NativeFSLockFactory()),true);
		TermEnum enumerator = reader.terms();
		long d = ConfigMan.ALL_DOCUMENT_IN_DB;
		
		ArrayList<Keyword[]> results = null;
		if(iel == 1) {			
			//one infinite element (accelerate the speed of process by assigning the single field name)
			//create two set of buffers to accelerate the speed of process
			String elementName = eleNames[0];
			Element thisElement = elements.get(0);
			
			String[] kMaxBuffer = new String[ConfigMan.CANDIDATE_SET_SIZE];
			int[] tfMaxBuffer = new int[ConfigMan.CANDIDATE_SET_SIZE];
			int[] dfMaxBuffer = new int[ConfigMan.CANDIDATE_SET_SIZE];
			int[] tagMaxBuffer = new int[ConfigMan.CANDIDATE_SET_SIZE];											//record from which tag text was retrieved
	
			

			//initialize the buffer
			for(int k = 0 ; k < kMaxBuffer.length ; k++) {
				dfMaxBuffer[k] = -1*Integer.MAX_VALUE;

			}
					
			while(enumerator.next()) {
				Term term = enumerator.term();
				if(!term.field().startsWith(elementName))	continue;							//decide the element of the term
				//document frequency
				int df = enumerator.docFreq();
				TermDocs termedocEnumerator = reader.termDocs(term);
				//term frequency
				int tf = 0;
				while(termedocEnumerator.next()) {
					tf += termedocEnumerator.freq();
				}
				//tag information
				int tag = Integer.parseInt(term.field().substring(elementName.length(),term.field().length()));
				
				
				//double ridf = Math.log(1-Math.pow(Math.E, -1*(double)tf/d)) - Math.log((double)df/d);
				
				

				//generate buffer -> tries to insert in the buffer
				if(df > dfMaxBuffer[dfMaxBuffer.length-1] ) {
					String patternid = issuedWordIndex.get(term.text());						//check whether the keyword has been issued
					if(patternid == null ) { //here no need to check !patternid.equals(elementName) since there is only one infinite element
						int j = dfMaxBuffer.length-1;
						for( ; j > 0 ; j--) {
							if(df >= dfMaxBuffer[j] && df < dfMaxBuffer[j-1]) {
								kMaxBuffer[j] = term.text();
								dfMaxBuffer[j] = df;
								tfMaxBuffer[j] = tf;
								tagMaxBuffer[j] = tag;
								break;			
							}
							else {
								//swap
								kMaxBuffer[j] = kMaxBuffer[j-1];
								dfMaxBuffer[j] = dfMaxBuffer[j-1];
								tfMaxBuffer[j] = tfMaxBuffer[j-1];	
								tagMaxBuffer[j] = tagMaxBuffer[j-1];
		
							}
						}
						if(j == 0) {
							//Maximum
							kMaxBuffer[0] = term.text();
							dfMaxBuffer[0] = df;
							tfMaxBuffer[0] = tf;	
							tagMaxBuffer[0] = tag;

						}
					}
				}
			}//end while
			
			results = new ArrayList<Keyword[]>(ConfigMan.CANDIDATE_SET_SIZE);
			Keyword[] ks = pack2KeywordSetDF(thisElement,kMaxBuffer,dfMaxBuffer,tfMaxBuffer,tagMaxBuffer);
			if(ConfigMan.MERGE_SAME_TAG_WORD)	
				ks = mergeSameKeyword(ks,reader);
			results.add(ks);
		}
		
		else if(iel > 1) {
			//multi-infinite elements running time is definitely slow than single element
			//create 2* eni set of buffers to accelerate the speed of process
			//not complete
			
			//results = pack2KeywordSets(elements,eleIndexs,kMaxBuffer,tfMaxBuffer,dfMaxBuffer,kMinBuffer,dfMinBuffer,tfMinBuffer);
		}
	
		
		return results;
	}

	
	
	/**
	 * 
	 * @return RIDF buffer The candidate sets corresponding to the infinite elements.
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public ArrayList<Keyword[]> generateCandidateSets() throws CorruptIndexException, IOException {
		//calculate the number of candidate sets to be returned.
		ArrayList<IElement> elements = IElement.getALLIElements();
		//As the elements name usually will be checked for thousands of times here we use String array instead of ArrayList<String>
		int iel = IElement.ielementsNum();					//infinite elements length
	
		String[] eleNames = new String[iel];
		int[] eleIndexs = new int[iel];
		
		for(int i = 0, j = 0; i < elements.size() ; i++) {
			if(elements.get(i) instanceof IElement) {
				eleNames[j] = "e" + elements.get(i).getId() + "pt";
				eleIndexs[j++] = i;
			}
		}
		//load index
		IndexReader reader= IndexReader.open(FSDirectory.open(indexDir, new NativeFSLockFactory()),true);
		TermEnum enumerator = reader.terms();
		long d = ConfigMan.ALL_DOCUMENT_IN_DB;
		
		ArrayList<Keyword[]> results = null;
		if(iel == 1) {			
			//one infinite element (accelerate the speed of process by assigning the single field name)
			//create two set of buffers to accelerate the speed of process
			String elementName = eleNames[0];
			Element thisElement = elements.get(0);
			
			String[] kMaxBuffer = new String[ConfigMan.CANDIDATE_SET_SIZE];
			double[] ridfMaxBuffer = new double[ConfigMan.CANDIDATE_SET_SIZE];
			int[] tfMaxBuffer = new int[ConfigMan.CANDIDATE_SET_SIZE];
			int[] dfMaxBuffer = new int[ConfigMan.CANDIDATE_SET_SIZE];
			int[] tagMaxBuffer = new int[ConfigMan.CANDIDATE_SET_SIZE];											//record from which tag text was retrieved
	
			

			//initialize the buffer
			for(int k = 0 ; k < kMaxBuffer.length ; k++) {
				ridfMaxBuffer[k] = -1*Double.MAX_VALUE;

			}
					
			while(enumerator.next()) {
				Term term = enumerator.term();
				if(!term.field().startsWith(elementName))	continue;							//decide the element of the term
				//document frequency
				int df = enumerator.docFreq();
				TermDocs termedocEnumerator = reader.termDocs(term);
				//term frequency
				int tf = 0;
				while(termedocEnumerator.next()) {
					tf += termedocEnumerator.freq();
				}
				//tag information
				int tag = Integer.parseInt(term.field().substring(elementName.length(),term.field().length()));
				
				double ridf = Math.log(1-Math.pow(Math.E, -1*(double)tf/d)) - Math.log((double)df/d);
				
			
				//calculate the keyword df ranks of all keywords (including ones outside the candidate buffer)
				int eob = dfList.length-1;					//end of buffer -> slightly accelerate the speed
				if(df > dfList[eob]) {
					//insertion (to be improved)
					int j = eob;
					for(; j > 0 ; j--) {
						if(df >= dfList[j] && df < dfList[j-1]) {
							break;
						}
					}
					if(dfList[j] != df) {
						for(int k = eob ; k > j ; k--) {
							dfList[k] = dfList[k-1];
						}
						dfList[j] = df;
					}
				}
				

				//generate buffer -> tries to insert in the buffer
				if(ridf > ridfMaxBuffer[ridfMaxBuffer.length-1] ) {
					String patternid = issuedWordIndex.get(term.text());						//check whether the keyword has been issued
					if(patternid == null ) { //here no need to check !patternid.equals(elementName) since there is only one infinite element
					
						int j = ridfMaxBuffer.length-1;
						for( ; j > 0 ; j--) {
							if(ridf >= ridfMaxBuffer[j] && ridf < ridfMaxBuffer[j-1]) {
								ridfMaxBuffer[j] = ridf;
								kMaxBuffer[j] = term.text();
								dfMaxBuffer[j] = df;
								tfMaxBuffer[j] = tf;
								tagMaxBuffer[j] = tag;
								break;			
							}
							else {
								//swap
								ridfMaxBuffer[j] = ridfMaxBuffer[j-1];
								kMaxBuffer[j] = kMaxBuffer[j-1];
								dfMaxBuffer[j] = dfMaxBuffer[j-1];
								tfMaxBuffer[j] = tfMaxBuffer[j-1];	
								tagMaxBuffer[j] = tagMaxBuffer[j-1];
		
							}
						}
						if(j == 0) {
							//Maximum
							ridfMaxBuffer[0] = ridf;
							kMaxBuffer[0] = term.text();
							dfMaxBuffer[0] = df;
							tfMaxBuffer[0] = tf;	
							tagMaxBuffer[0] = tag;

						}
					}
				}
			}//end while
			
			results = new ArrayList<Keyword[]>(ConfigMan.CANDIDATE_SET_SIZE);
			Keyword[] ks = pack2KeywordSet(thisElement,kMaxBuffer,ridfMaxBuffer,dfMaxBuffer,tfMaxBuffer,tagMaxBuffer);
			if(ConfigMan.MERGE_SAME_TAG_WORD)	
				ks = mergeSameKeyword(ks,reader);
			results.add(ks);
		}
		
		else if(iel > 1) {
			//multi-infinite elements running time is definitely slow than single element
			//create 2* eni set of buffers to accelerate the speed of process
			//not complete
			

			//end while
			//results = pack2KeywordSets(elements,eleIndexs,kMaxBuffer,tfMaxBuffer,dfMaxBuffer,kMinBuffer,dfMinBuffer,tfMinBuffer);
		}
	
		
		return results;
	}
	


	

	private void calculateDFRank(Keyword[] ks) {
		int eob = dfList.length-1;

		for(int i = 0 ; i < ks.length ; i++) {
			int dfrank = eob;
			int df = ks[i].getDf();
			
			if(df > dfList[eob]) {
				int left=0, right=eob;
				//binary search
				while(left <= right) {
					int middle = (left + right) / 2;
					if(df == dfList[middle]) {
						dfrank = middle;
						break;
					}
					if(df > dfList[middle]) {
						right=middle-1;
					} else {
						left=middle+1;
					}
				}
				dfrank += 1;
			} else {
				dfrank += dfList[eob] - df + 1;		//estimate a df rank (accelerate the speed)
			}
			ks[i].setDfrank(dfrank);
		}
	
		//estimate the DF rank
		
		
	}
	
	
	private Keyword[] pack2KeywordSet(Element e,String[] kMax, double[] ridfMax,int[] dfMax, int[] tfMax,int[] tagMax) {
		
		double maxRidf = Double.MIN_VALUE, minRidf = Double.MAX_VALUE;
		double maxTfidf = Double.MIN_VALUE, minTfidf = Double.MAX_VALUE;
	
		
		double[] tfidfMax = new double[kMax.length];
		for(int i = 0 ; i < kMax.length ; i++) {
			if(kMax[i] == null)	break;
			tfidfMax[i] = ((double)tfMax[i])*Math.log(ConfigMan.ALL_DOCUMENT_IN_DB/(double)dfMax[i]);
			if(tfidfMax[i] > maxTfidf) maxTfidf = tfidfMax[i];
			else if(tfidfMax[i] < minTfidf) minTfidf = tfidfMax[i];
			if(ridfMax[i] > maxRidf) maxRidf = ridfMax[i];
			else if(ridfMax[i] < minRidf) minRidf = ridfMax[i];
			
	
		}
		
		double ridfScale = maxRidf - minRidf;
		double tfidfScale = maxTfidf - minTfidf;
		ArrayList<Keyword> buffer = new ArrayList<Keyword>(kMax.length);
		for(int i = 0 ; i < kMax.length ; i++) {
			if(kMax[i] == null)	break;
			Keyword tk = new Keyword(e, kMax[i], dfMax[i], tfMax[i]);
			tk.addTagInDom("e"+e.getId()+"pt"+tagMax[i]);
			tk.setRidf((ridfMax[i]- minRidf)/ridfScale);
			tk.setTfidf((tfidfMax[i]-minTfidf)/tfidfScale);
			buffer.add(tk);
		}
		Keyword[] ks = new Keyword[buffer.size()];
		buffer.toArray(ks);
		//calculate the df rank of these candidate keywords
		this.calculateDFRank(ks);
		return ks;
	}
	
	private Keyword[] pack2KeywordSetDF(Element e,String[] kMax,int[] dfMax, int[] tfMax,int[] tagMax) {
		
		double maxRidf = Double.MIN_VALUE, minRidf = Double.MAX_VALUE;
		double maxTfidf = Double.MIN_VALUE, minTfidf = Double.MAX_VALUE;

	
		double[] ridfMax = new double[kMax.length];
		double[] tfidfMax = new double[kMax.length];
		
		for(int i = 0 ; i < kMax.length ; i++) {
			if(kMax[i] == null)	break;
			ridfMax[i] = Math.log(1-Math.pow(Math.E, -1*(double)tfMax[i]/ConfigMan.ALL_DOCUMENT_IN_DB)) - Math.log((double)dfMax[i]/ConfigMan.ALL_DOCUMENT_IN_DB);
			tfidfMax[i] = ((double)tfMax[i])*Math.log(ConfigMan.ALL_DOCUMENT_IN_DB/(double)dfMax[i]);
			if(tfidfMax[i] > maxTfidf) maxTfidf = tfidfMax[i];
			else if(tfidfMax[i] < minTfidf) minTfidf = tfidfMax[i];
			if(ridfMax[i] > maxRidf) maxRidf = ridfMax[i];
			else if(ridfMax[i] < minRidf) minRidf = ridfMax[i];
			
	
		}
		
		double ridfScale = maxRidf - minRidf;
		double tfidfScale = maxTfidf - minTfidf;
		ArrayList<Keyword> buffer = new ArrayList<Keyword>(kMax.length);
		for(int i = 0 ; i < kMax.length ; i++) {
			if(kMax[i] == null)	break;
			Keyword tk = new Keyword(e, kMax[i], dfMax[i], tfMax[i]);
			tk.addTagInDom("e"+e.getId()+"pt"+tagMax[i]);
			tk.setRidf((ridfMax[i]- minRidf)/ridfScale);
			tk.setTfidf((tfidfMax[i]-minTfidf)/tfidfScale);
			
			//df rank
			tk.setDfrank(i+1);
			tk.setNdfrank(1-i/(double)kMax.length);
			buffer.add(tk);
		}
		Keyword[] ks = new Keyword[buffer.size()];
		buffer.toArray(ks);
		return ks;
	}
	
	

	
	private Keyword[] pack2KeywordSet(Element e,String[] kMax, String[] kMin, int[] dfMax, int[] dfMin, int[] tfMax, int[] tfMin,
			int[] hlMax, int[] hlMin) {
		int counter = 0;
		Keyword[] ks = new Keyword[kMax.length+kMin.length];
		for(int i = 0 ; i < kMax.length ; i++) {
			if(kMax[i] == null)	continue;
			Keyword tk = new Keyword(e, kMax[i], dfMax[i], tfMax[i]);
			tk.addTagInDom("e"+e.getId()+"-"+hlMax[i]);
			//tk.computeStatistic();
			ks[counter++] = tk;
		}
		for(int i = 0 ; i < kMin.length ; i++) {
			if(kMin[i] == null)	continue;
			Keyword tk = new Keyword(e, kMin[i], dfMin[i], tfMin[i]);
			tk.addTagInDom("e"+e.getId()+"-"+hlMin[i]);
			//tk.computeStatistic();
			ks[counter++] = tk;
		}
		return ks;
	}
	
	/**
	 * This method is to match the same keyword that appears in more than one tag in HTML page
	 * Note the method is only available in the EP consisting of a single infinite domain element
	 * in which the element name is better indicator than tag in DOM tree.
	 * @param keywords
	 * @param index
	 * @param elementName
	 * @return
	 * @throws IOException
	 */
	protected Keyword[] mergeSameKeyword(Keyword[] keywords, IndexReader index) throws IOException {
		for(int i = 0 ; i < keywords.length ; i++) {
			if(keywords[i] == null)	break;
			Term term = new Term(keywords[i].getWord());
			TermEnum tenum = index.terms(term);
			while(tenum.next()) {
				//found - merge the two terms
				String field = tenum.term().field();
				ArrayList<String> seenTags = keywords[i].getTagInDom();
				boolean in = false;
				for(int j = 0 ; j < seenTags.size() ; j++) {
					if(field.equals(seenTags.get(j))) {
						in = true;
						break;
					}
				}
				if(!in) {
					//append the new tag
					keywords[i].addTagInDom(field);
				}
			}
			
		}
		
		return keywords;
	}

	

	
	public void print(ArrayList<POSWord> words) {
		System.out.println("---------------POS WORDS------------");
		for(int i = 0 ; i < words.size() ; i++) {
			System.out.println(words.get(i).getContent() + "-" + words.get(i).getPos());
		}
	}




	public static void main(String args[]) {
		RecordIndexer im;
		try {
			im = new RecordIndexer();
			//System.out.println(im.format("就大事记.321 i AM     a tEacher倒萨接口,加咖&nbsp;啡较为u."));
			//System.out.println("asfsaftrDASze1".matches("[\\u0021-\\u007f]+"));
			//SegTag cs = new SegTag(1);;
			//ArrayList<POSWord> sentence;
			//sentence = cs.split("我是一个中国人1 i am a chinese").toPOSWord();
			System.out.println(im.parse("就大事记来说.321 i AM     a tEacher 中国国庆60周年"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}


	}


}

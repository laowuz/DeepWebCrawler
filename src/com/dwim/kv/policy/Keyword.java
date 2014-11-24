package com.dwim.kv.policy;


import java.util.ArrayList;

import com.dwim.form.analysis.Element;
import com.dwim.util.ConfigMan;
import com.dwim.util.FormatPrinter;

/**
 * 
 * 
 * 
 * Here TF is not against the strict definition of term frequency which equals term occurrence 
 * dividing the number of words in the document. In contrast, TF in DWIM equals the term occurrence in all documents.
 * @author JL
 *
 */
public class Keyword {
	private String word;
	private Element element;
	private int wordLength;
	private ArrayList<String> tagInDom;
	private int pos;
	private int df;
	private int tf;
	private int lanuageType;
	private double ridf;			//normalized score
	private double tfidf;			//normalized score
	private double highlight;		//not used
	private int dfrank;	
	private double ndfrank;			//normalized score
	private int numOfAccessDocs;
	private int numOfAccessNewDocus;
	
	//user defined features
	
	
	

	
	//Types of POS
	public static final int NUMBER = 0, 							//cardinal number
							NOUN = 1,  
							ADJ = 2,								//
							VERB_OR_MODAL = 3, 
							ADV = 4,
							PNOUN_OR_PREDETERMINER = 5, 			//personal noun or perdeterminer  all both
							W_WORD = 6,
							CONJ_OR_PREP_OR_TO_OR_DT= 7;			//conjunction or preposition
	
	public static int NUM_OF_POS = 8;
						
	
	public Keyword(Element element, String keyword) {
		this(element,keyword,0,0);
	}
	
	public Keyword(Element element,String keyword, int documentFrequency, int termFrequency) {
		this.element = element;
		word = keyword;
		df = documentFrequency;
		tf = termFrequency;
		pos = -1;
		lanuageType = -1;
		highlight = -1;
		dfrank = -1;
		numOfAccessDocs = -1;
		numOfAccessNewDocus = -1;
		tagInDom = new ArrayList<String>(1);
		
	}

	public int getPos() {
		return pos;
	}

	protected void setPos(int pos) {
		this.pos = pos;
	}

	public int getDf() {
		return df;
	}

	public void setDf(int df) {
		this.df = df;
	}

	public int getTf() {
		return tf;
	}

	public void setTf(int tf) {
		this.tf = tf;
	}

	public int getLanuageType() {
		return lanuageType;
	}
	
	protected void setLanuageType(int lanuageType) {
		this.lanuageType = lanuageType;
	}


	public double getRidf() {
		return ridf;
	}

	public void setRidf(double ridf) {
		this.ridf = ridf;
	}

	public double getTfidf() {
		return tfidf;
	}

	public void setTfidf(double tfidf) {
		this.tfidf = tfidf;
	}

	public String getWord() {
		return word;
	}

	public int getWordLength() {
		return wordLength;
	}

	public double getHighlight() {
		return highlight;
	}

	public void setHighlight(double highlight) {
		this.highlight = highlight;
	}

	public Element getElement() {
		return element;
	}
	
	public void computeStatistic() {
		//impose a discount on tf ????
		ridf = Math.log(1-Math.pow(Math.E, -1*(double)tf/ConfigMan.ALL_DOCUMENT_IN_DB)) - Math.log((double)df/ConfigMan.ALL_DOCUMENT_IN_DB);
		tfidf = ((double)tf)*Math.log(ConfigMan.ALL_DOCUMENT_IN_DB/(double)df);
	}

	public int getDfrank() {
		return dfrank;
	}

	public void setDfrank(int dfrank) {
		this.dfrank = dfrank;
	}

	public ArrayList<String> getTagInDom() {
		return tagInDom;
	}

	public void addTagInDom(String tagInDom) {
		this.tagInDom.add(tagInDom);
	}
	
	public void addTagInDom(ArrayList<String> tagInDoms) {
		for(int i = 0 ; i < tagInDoms.size() ; i++)
			this.tagInDom.add(tagInDoms.get(i));
	}
	
	protected void setWordLength(int wordLength) {
		this.wordLength = wordLength;
	}
	
	public int getNumOfAccessDocs() {
		return numOfAccessDocs;
	}

	public void setNumOfAccessDocs(int numOfAccessDocs) {
		this.numOfAccessDocs = numOfAccessDocs;
	}

	public int getNumOfAccessNewDocus() {
		return numOfAccessNewDocus;
	}

	public void setNumOfAccessNewDocus(int numOfAccessNewDocus) {
		this.numOfAccessNewDocus = numOfAccessNewDocus;
	}

	public static void printTitle() {
		StringBuffer result = new StringBuffer(256);
		result.append(FormatPrinter.spaces("word", 10));
		result.append(FormatPrinter.spaces("elem", 10));
		result.append(FormatPrinter.spaces("len", 3));
		result.append(FormatPrinter.spaces("lt", 5));
		//result.append(FormatPrinter.spaces("hl", 2));
		result.append(FormatPrinter.spaces("tag", 6));
		result.append(FormatPrinter.spaces("pos", 3));
		result.append(FormatPrinter.spaces("df", 5));
		result.append(FormatPrinter.spaces("tf", 5));
		result.append(FormatPrinter.spaces("tfidf", 16));
		result.append(FormatPrinter.spaces("ridf", 16));
		System.out.println(result.toString());
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(256);
		result.append(FormatPrinter.spaces(word, 10));
		result.append(FormatPrinter.spaces(element.getElementName(), 10));
		result.append(FormatPrinter.spaces(wordLength, 3));
		result.append(FormatPrinter.spaces(lanuageType, 5));
		//result.append(FormatPrinter.spaces("highlight", 2));
		result.append(FormatPrinter.spaces(pos, 3));
		result.append(FormatPrinter.spaces(df, 5));
		result.append(FormatPrinter.spaces(tf, 5));
		result.append(FormatPrinter.spaces(tfidf, 16));
		result.append(FormatPrinter.spaces(ridf, 16));
		return result.toString();
		
	}

	public double getNdfrank() {
		return ndfrank;
	}

	public void setNdfrank(double ndfrank) {
		this.ndfrank = ndfrank;
	}


	



	
	
	
	
}

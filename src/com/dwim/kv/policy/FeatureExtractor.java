package com.dwim.kv.policy;

import java.util.ArrayList;

import org.ictclas4j.bean.POSWord;
import org.ictclas4j.segment.SegTag;

import com.dwim.util.ConfigMan;
import com.dwim.util.LanguageDetector;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class FeatureExtractor {
	private SegTag cnSegTag;

	public FeatureExtractor() {
		
		if(ConfigMan.MULTI_LANUAGE == true)
			cnSegTag = new SegTag(1);
		else
			cnSegTag = null;

	}
	
	public Keyword[] extractFeatures(Keyword[] ins) throws Exception {
		for(int i = 0 ; i < ins.length ; i++) {
			if(ins[i] != null)
				ins[i] = extractFeatures(ins[i]);
		}
		return ins;
	}
	
	public Keyword extractFeatures(Keyword in) throws Exception {
		
		String word = in.getWord();

		int lanuageType = LanguageDetector.getLaguageType(word);
		in.setLanuageType(lanuageType);


		int wordLength = word.length();
		int pos = -1;
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_POS] != 0) {
			String posTag;
			if(lanuageType == LanguageDetector.CHINESE) {
				//pos tag English and Latin
				ArrayList<POSWord> s;
				//System.out.println(word);
				s = cnSegTag.split(word).toPOSWord();
				if(s == null || s.size() <= 0) 
					posTag = null;
				else 
					posTag = s.get(0).getPos();
			} else if(lanuageType == LanguageDetector.ENLGLISH) {
				//pos tag Chinese
				Sentence<TaggedWord> s = MaxentTagger.tagStringTokenized(word);
				posTag = s.get(0).tag();
			} else {
				//otherwise
				posTag = null;
			}
		
			
			if(posTag != null) {
				posTag = posTag.toLowerCase();
				if(posTag.equals("ls") || posTag.equals("cd")) {
					pos = Keyword.NUMBER;
				} else if(posTag.startsWith("n")) {
					pos = Keyword.NOUN;
				} else if(posTag.startsWith("j")) {
					pos = Keyword.ADJ;
				} else if(posTag.equals("md") || posTag.startsWith("v")) {
					pos = Keyword.VERB_OR_MODAL;
				} else if(posTag.startsWith("r")) {
					pos = Keyword.ADV;
				} else if(posTag.startsWith("p")) {
					pos = Keyword.PNOUN_OR_PREDETERMINER;
				} else if(posTag.startsWith("w")) {
					pos = Keyword.W_WORD;
				} else if(posTag.equals("cc") || posTag.equals("in") || posTag.equals("to") || posTag.equals("dt")) {
					pos = Keyword.CONJ_OR_PREP_OR_TO_OR_DT;
				} else {
					//unknown POS 
					pos = -1;
				}
			} else {
				//unknown POS 
				pos = -1;
			}
			in.setPos(pos);
		}
		
		//assign the extracted features
		in.setWordLength(wordLength);
		
		
		
		return in;	
	}
	

	
	
}



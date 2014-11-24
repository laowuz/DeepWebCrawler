package com.dwim.kv.policy;


import java.util.ArrayList;

import com.dwim.form.analysis.IElement;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.util.HTMLDocument;
import com.dwim.util.LanguageDetector;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class KNNRewardEstimator implements IEstimator {

	private Instances traningSet;
	private ArrayList<String> keywordsBuffer;			//store the corresponding keywords of the one in training set
	private ArrayList<Integer> accessNewDocBuffer;		//store the corresponding number of new doc accessed of the one in training set
	private Classifier classifer;
	private String tsHead;
	private String csHead;
	
	
	//tags in DOM tree
	private String[] seenDomTags;
	private int numOfTagsInDom;
	

	
	
	public KNNRewardEstimator() {
		FastVector vector = new FastVector(32);
		
		FastVector nominalValues = new FastVector(3);
		nominalValues.addElement("-1");
		nominalValues.addElement("0");
		nominalValues.addElement("1");
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_HIGHLIGHT] != 0) {
			Attribute highlight = new Attribute("hightlight");
			highlight.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_HIGHLIGHT]);
			vector.addElement(highlight);
		}
	
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_RIDF] != 0) {
			Attribute ridf = new Attribute("ridf");
			ridf.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_RIDF]);
			vector.addElement(ridf);
		}
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_DFRANK] != 0) {
			Attribute dfrank = new Attribute("dfrank");
			dfrank.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_DFRANK]);
			vector.addElement(dfrank);
		}
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LENGTH] != 0) {
			Attribute length1 = new Attribute("length1",nominalValues);
			length1.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LENGTH]);
			Attribute length2 = new Attribute("length2",nominalValues);
			length2.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LENGTH]);
			Attribute length3 = new Attribute("length3",nominalValues);
			length3.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LENGTH]);
			Attribute length4 = new Attribute("length4",nominalValues);
			length4.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LENGTH]);
			Attribute lengthm4 = new Attribute("length>4",nominalValues);
			lengthm4.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LENGTH]);
			
			vector.addElement(length1);
			vector.addElement(length2);
			vector.addElement(length3);
			vector.addElement(length4);
			vector.addElement(lengthm4);
		}
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_ELEMENT] != 0) {
			for(int i = 0 ; i < IElement.ielementsNum() ; i++) {
				Attribute element = new Attribute("element"+i,nominalValues);
				element.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_ELEMENT]);
				vector.addElement(element);
			}
		}
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_DOMTAG] != 0) {
			numOfTagsInDom = ConfigMan.WRAPPER_SRR_SCRIPT.length; 
			if(numOfTagsInDom <= 0)
				numOfTagsInDom = 8;
			
			for(int i = 0 ; i < numOfTagsInDom ; i++) {
				Attribute tagInDom = new Attribute("tagindom"+i,nominalValues);
				tagInDom.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_DOMTAG]);
				vector.addElement(tagInDom);
			}
		}
		
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_POS] != 0) {
			for(int i = 0 ; i < Keyword.NUM_OF_POS ; i++) {
				Attribute pos = new Attribute("pos"+i,nominalValues);
				pos.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_POS]);
				vector.addElement(pos);
			}
		}
			
		
		if(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LANGUAGE] != 0) {
			for(int i = 0 ; i < LanguageDetector.SUPPORT_TYPES_OF_LANUAGE +1; i++) {
				Attribute language = new Attribute("language"+i,nominalValues);
				language.setWeight(ConfigMan.FEATURE_WEIGHT[ConfigMan.F_LANGUAGE]);
				vector.addElement(language);
			}
		}
		
		
		vector.addElement(new Attribute("accessrecords"));
		
		traningSet = new Instances("Candidate Set",vector,ConfigMan.TRANING_SET_SIZE);
		keywordsBuffer = new ArrayList<String>(ConfigMan.TRANING_SET_SIZE);
		accessNewDocBuffer = new ArrayList<Integer>(ConfigMan.TRANING_SET_SIZE);
			
		traningSet.setClassIndex(traningSet.numAttributes() - 1);
		
		csHead = traningSet.toString();
		traningSet.setRelationName("Training Set");
		tsHead = traningSet.toString();
		
		classifer = new IBk();
	
		seenDomTags = new String[numOfTagsInDom];
	
		
	}
	
	
	public void addTrainingInstance(Keyword k) throws Exception {
		Instance ins = new Instance(traningSet.numAttributes());
		if(traningSet.attribute("highlight") != null)
			ins.setValue(traningSet.attribute("highlight").index(), k.getHighlight());
		if(traningSet.attribute("ridf") != null)
		ins.setValue(traningSet.attribute("ridf").index(), k.getRidf());
		if(traningSet.attribute("dfrank") != null)
		ins.setValue(traningSet.attribute("dfrank").index(), k.getNdfrank());
		ins = length(k,ins);
		ins = element(k,ins);
		ins = tagInDom(k, ins);
		ins = pos(k,ins);
		ins = lanuage(k,ins);
		ins.setValue(traningSet.attribute("accessrecords").index(), k.getNumOfAccessDocs());
		traningSet.add(ins);
		keywordsBuffer.add(k.getWord());			//store the keyword string
		accessNewDocBuffer.add(k.getNumOfAccessNewDocus());
		
		//rebuild model
		if(ConfigMan.K_NEARST_NEIGHBOR > 0)
			((IBk) classifer).setKNN(ConfigMan.K_NEARST_NEIGHBOR);
		else
			((IBk) classifer).setKNN(traningSet.numInstances()/12+1);
		
		 classifer.buildClassifier(traningSet);
		 
		//save the training set file 
		StringBuffer fileContent = null;
		if(ConfigMan.DEBUG)  {
			fileContent = new StringBuffer(traningSet.numInstances() * 32);
			fileContent.append(tsHead);
			for(int i = 0 ; i < traningSet.numInstances() ; i++) {
				fileContent.append(traningSet.instance(i));
				fileContent.append(" % " + keywordsBuffer.get(i) + "," + accessNewDocBuffer.get(i));
				fileContent.append("\r\n");
			}
			
			if(ConfigMan.DEBUG)
				HTMLDocument.write(ConfigMan.SAVE_CANDIDATE_SET_PATH,"ts_"+traningSet.numInstances(),"utf-8",fileContent.toString(),"arff");
		}
		
	}
	
	public double getReward(Object obj) throws Exception {
		if(obj == null)	return -1;
		if(!(obj instanceof Keyword))
			throw new DWIMException ("Does not match the input format of" + this.getClass());
		if(traningSet.numInstances() == 0) {
			//there is no instance (The first round loop)
		}
		Keyword k = (Keyword)obj;
		Instance ins = new Instance(traningSet.numAttributes());
		if(traningSet.attribute("highlight") != null)
			ins.setValue(traningSet.attribute("highlight").index(), k.getHighlight());
		if(traningSet.attribute("ridf") != null)
			ins.setValue(traningSet.attribute("ridf").index(), k.getRidf());
		if(traningSet.attribute("dfrank") != null)
			ins.setValue(traningSet.attribute("dfrank").index(), k.getNdfrank());
		ins = length(k,ins);
		ins = element(k,ins);
		ins = tagInDom(k,ins);
		ins = pos(k,ins);
		ins = lanuage(k,ins);
		ins.setDataset(traningSet);
		
		double result = classifer.classifyInstance(ins);
		if(!ConfigMan.LIMIT_RESULT)
			result = (result-k.getDf())/ConfigMan.ALL_DOCUMENT_IN_DB;
		else
			result = (result)/ConfigMan.ALL_DOCUMENT_IN_DB;
		return result;
	}
	
	public double[] getRewards(Object[] objs) throws Exception {
		if(objs == null || objs.length == 0)	return null;
		if(!(objs[0] instanceof Keyword)) 
			throw new DWIMException("Does not match the input format of" + this.getClass());
		
		StringBuffer fileContent = null;
		if(ConfigMan.DEBUG)  {
			fileContent = new StringBuffer(1024);
			fileContent.append(csHead);
		}
		
		double[] results = new double[objs.length];
		
		//find the maximum and minimum df rank in the buffer set
	
		
		for(int i = 0 ; i < objs.length ; i++) {
			Keyword k = (Keyword) objs[i];
			Instance ins = new Instance(traningSet.numAttributes());
			if(traningSet.attribute("highlight") != null)
				ins.setValue(traningSet.attribute("highlight").index(), k.getHighlight());
			if(traningSet.attribute("ridf") != null)
				ins.setValue(traningSet.attribute("ridf").index(), k.getRidf());
			if(traningSet.attribute("dfrank") != null)
				ins.setValue(traningSet.attribute("dfrank").index(), k.getNdfrank());
			ins = length(k,ins);
			ins = element(k,ins);
			ins = tagInDom(k,ins);
			ins = pos(k,ins);
			ins = lanuage(k,ins);
			ins.setDataset(traningSet);
			double result = classifer.classifyInstance(ins);
			if(!ConfigMan.LIMIT_RESULT)
				results[i] = (result-k.getDf())/ConfigMan.ALL_DOCUMENT_IN_DB;
			else
				results[i] = (result)/ConfigMan.ALL_DOCUMENT_IN_DB;
			if(ConfigMan.DEBUG) {
				fileContent.append(ins);
				fileContent.append(" % " + k.getWord() + "," + result + "," + results[i]);
				fileContent.append("\r\n");
			}
			
		}
		if(ConfigMan.DEBUG) {
			HTMLDocument.write(ConfigMan.SAVE_CANDIDATE_SET_PATH,"cs_"+traningSet.numInstances(),"utf-8",fileContent.toString(),"arff");
		}
		return results;
	}
	
	
	

	
	
	protected Instance element(Keyword k, Instance ins) {
		if(traningSet.attribute("element0") == null)
			return ins;
		int patternype = k.getElement().getId();
	
		int startIndex = traningSet.attribute("element0").index();
		for(int i = 0 ; i < IElement.ielementsNum() ; i++) {
			if(i == patternype)
				ins.setValue(traningSet.attribute(startIndex+i), "1");
			else
				ins.setValue(traningSet.attribute(startIndex+i), "-1");
		}
		return ins;
	}
	
	protected Instance pos(Keyword k, Instance ins) {
		if(traningSet.attribute("pos0") == null)
			return ins;
		int postype = k.getPos();
		int startIndex = traningSet.attribute("pos0").index();
		for(int i = 0 ; i < Keyword.NUM_OF_POS ; i++) {
			if(i == postype)
				ins.setValue(traningSet.attribute(startIndex+i), "1");
			else
				ins.setValue(traningSet.attribute(startIndex+i), "-1");
		}
		return ins;
	}
	/**
	 * note language type index 0 is unknown
	 * @param k
	 * @param ins
	 */
	protected Instance lanuage(Keyword k, Instance ins) {
		if(traningSet.attribute("language0") == null)
			return ins;
		int lanuagetype = k.getLanuageType()/LanguageDetector.MULTIPLE_FACTOR;
		int startIndex = traningSet.attribute("language0").index();
		for(int i = 0 ; i < LanguageDetector.SUPPORT_TYPES_OF_LANUAGE+1; i++) {
			if(i == lanuagetype)
				ins.setValue(traningSet.attribute(startIndex+i), "1");
			else
				ins.setValue(traningSet.attribute(startIndex+i), "-1");
		}
		return ins;
	}
	
	protected Instance tagInDom(Keyword k, Instance ins) {
		if(k.getTagInDom() == null || traningSet.attribute("tagindom0") ==null
		  ||k.getTagInDom().size() <= 0)	
			return ins;
		
		//initialize the attribute
		int startIndex = traningSet.attribute("tagindom0").index();
		for(int j = 0 ; j < numOfTagsInDom ; j++) {
			ins.setValue(traningSet.attribute(startIndex+j), "-1");
		}
		
		for(int p = 0 ; p < k.getTagInDom().size() ; p++) {
			String tag = k.getTagInDom().get(p);
			int findex = -1;		//index in seenDomTags
			for(int i = 0 ; i < seenDomTags.length ; i++) {
				if(seenDomTags[i] == null)  break;
				if(seenDomTags[i].equals(tag)) {
					findex = i;
					break;
				}
			}
			if(findex != -1) {
				//found
				
				for(int j = 0 ; j < numOfTagsInDom ; j++) {
					if(j == findex) {
						ins.setValue(traningSet.attribute(startIndex+j), "1");
						break;
					}
				}
			} else if (seenDomTags[numOfTagsInDom-1] == null) {
				//the seenDomTags array has spaces
				int i = 0;
				for( ; i < seenDomTags.length ; i++) {
					if(seenDomTags[i] == null) {
						seenDomTags[i] = tag;
						break;
					}
				}

				for(int j = 0 ; j < numOfTagsInDom ; j++) {
					if(j == i) {
						ins.setValue(traningSet.attribute(startIndex+j), "1");
						break;
					}
				}
			} else {
				//ignore the unseen tag in DOM (features are exhausted)
			}
		}
		
		return ins;
		
	}
	
	
	
	protected Instance length(Keyword k, Instance ins) {
		if(traningSet.attribute("length1") == null) 
			return ins;
		int length = k.getWordLength();
		if(length >= 0 && length <2) {
			ins.setValue(traningSet.attribute("length1"), "1");
			ins.setValue(traningSet.attribute("length2"), "-1");
			ins.setValue(traningSet.attribute("length3"), "-1");
			ins.setValue(traningSet.attribute("length4"), "-1");
			ins.setValue(traningSet.attribute("length>4"), "-1");
		} else if(length >=2 && length <3) {
			ins.setValue(traningSet.attribute("length1"), "-1");
			ins.setValue(traningSet.attribute("length2"), "1");
			ins.setValue(traningSet.attribute("length3"), "-1");
			ins.setValue(traningSet.attribute("length4"), "-1");
			ins.setValue(traningSet.attribute("length>4"), "-1");
		} else if(length >=3 && length <4) {
			ins.setValue(traningSet.attribute("length1"), "-1");
			ins.setValue(traningSet.attribute("length2"), "-1");
			ins.setValue(traningSet.attribute("length3"), "1");
			ins.setValue(traningSet.attribute("length4"),"-1");
			ins.setValue(traningSet.attribute("length>4"),"-1");
		} else if(length >=4 && length <5) {
			ins.setValue(traningSet.attribute("length1"),"-1");
			ins.setValue(traningSet.attribute("length2"),"-1");
			ins.setValue(traningSet.attribute("length3"), "-1");
			ins.setValue(traningSet.attribute("length4"), "1");
			ins.setValue(traningSet.attribute("length>4"), "-1");
		} else if(length >=5) {
			ins.setValue(traningSet.attribute("length1"), "-1");
			ins.setValue(traningSet.attribute("length2"), "-1");
			ins.setValue(traningSet.attribute("length3"), "-1");
			ins.setValue(traningSet.attribute("length4"), "-1");
			ins.setValue(traningSet.attribute("length>4"), "1");
		}
		return ins;
	}



	

	
}

package com.dwim.kv.policy;


import java.util.ArrayList;

import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.util.HTMLDocument;

public class GenericDFEstimator implements IEstimator {
	private ArrayList<Keyword> traningSet;			//dummy
	private String tsHead;
	private String csHead;
	
	
	public GenericDFEstimator() {
		if(ConfigMan.DEBUG) {
			traningSet = new ArrayList<Keyword>(ConfigMan.TRANING_SET_SIZE);
			tsHead = "@relation 'Training Set'\r\n" +
					 "@attribute df numeric\r\n" +
					 "@attribute accessrecords numeric \r\n\r\n" +
					 "@data\r\n";
			csHead = "@relation 'Candidate Set'\r\n" +
					 "@attribute df numeric\r\n" +
					 "@attribute accessrecords numeric\r\n\r\n" +
					 "@data\r\n";
		}
	}
	
	
	
	
	public void addTrainingInstance(Keyword key) {
		if(ConfigMan.DEBUG)  {
			traningSet.add(key);
			StringBuffer fileContent = new StringBuffer(traningSet.size() * 16);
			fileContent.append(tsHead);
			for(int i = 0 ; i < traningSet.size() ; i++) {
				fileContent.append(traningSet.get(i).getDf() + "," + traningSet.get(i).getNumOfAccessDocs());
				fileContent.append(" % " + traningSet.get(i).getWord() + "," + traningSet.get(i).getNumOfAccessNewDocus());
				fileContent.append("\r\n");
			}
			HTMLDocument.write(ConfigMan.SAVE_CANDIDATE_SET_PATH,"ts_"+traningSet.size(),"utf-8",fileContent.toString(),"arff");
		}
		
	}
	

	public double getReward(Object obj) throws Exception {
		if(obj == null)	return -1;
		if(!(obj instanceof Keyword))
			throw new DWIMException ("Does not match the input format of" + this.getClass());
		
		double result = ((double)ConfigMan.CANDIDATE_SET_SIZE - ((Keyword)obj).getDfrank()) / (double)ConfigMan.CANDIDATE_SET_SIZE;
		return result;
	}

	public double[] getRewards(Object[] objs) throws Exception {
		if(objs == null || objs.length == 0)	return null;
		if(!(objs[0] instanceof Keyword)) 
			throw new DWIMException("Does not match the input format of" + this.getClass());
		
		double maxDf = 0.0d;
		for(int i = 0 ; i < objs.length ; i++) {
			if(((Keyword)objs[i]).getDf() > maxDf) maxDf = ((Keyword)objs[i]).getDf();
		}
		
		StringBuffer fileContent = null;
		if(ConfigMan.DEBUG)  {
			fileContent = new StringBuffer(1024);
			fileContent.append(csHead);
		}
		
		double[] results = new double[objs.length];
		
		for(int i = 0 ; i < results.length ; i++) {
			Keyword key = (Keyword)objs[i];
			results[i] = 1-((maxDf -key.getDf()) / maxDf) ;
			if(ConfigMan.DEBUG) {
				fileContent.append(key.getDf() + ",?");
				fileContent.append(" %" + key.getWord() + "," + results[i]);
				fileContent.append("\r\n");
			}
		}
		if(ConfigMan.DEBUG) {
			HTMLDocument.write(ConfigMan.SAVE_CANDIDATE_SET_PATH,"cs_"+traningSet.size(),"utf-8",fileContent.toString(),"arff");
		}
		
		return results;
	}

}

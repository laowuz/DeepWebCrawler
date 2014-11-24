package com.dwim.kv.policy;

import java.util.ArrayList;

import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.util.HTMLDocument;

public class RandomRewardEstimator implements IEstimator {
	private ArrayList<Keyword> traningSet;			//dummy
	private String tsHead;
	private String csHead;
	
	public RandomRewardEstimator() {
		if(ConfigMan.DEBUG) {
			traningSet = new ArrayList<Keyword>(ConfigMan.TRANING_SET_SIZE);
			tsHead = "@relation 'Training Set'\r\n" +
			 "@attribute accessrecords numeric \r\n\r\n" +
			 "@data\r\n";
			csHead = "@relation 'Candidate Set'\r\n" +
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
				fileContent.append(traningSet.get(i).getNumOfAccessDocs());
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
		return Math.random();
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
		
		for(int i = 0 ; i < results.length ; i++) {
			results[i] = Math.random();
			
			if(ConfigMan.DEBUG) {
				fileContent.append("?");
				fileContent.append(" %" + ((Keyword)objs[i]).getWord() + "," + results[i]);
				fileContent.append("\r\n");
			}
		}
		
		if(ConfigMan.DEBUG) {
			HTMLDocument.write(ConfigMan.SAVE_CANDIDATE_SET_PATH,"cs_"+traningSet.size(),"utf-8",fileContent.toString(),"arff");
		}
		
		return results;
	}



}

package com.dwim.kv.policy;


import java.util.ArrayList;

import com.dwim.form.analysis.EP;
import com.dwim.form.analysis.Element;
import com.dwim.form.analysis.IElement;
import com.dwim.form.parser.Query;
import com.dwim.util.DWIMException;

/**
 * each pattern is assumed to include no more than single infinite element
 * @author JL
 *
 */
public class PatternKeywordEstimator {
	/*
	private PatternRewardEstimator preest;
	private IEstimator kreest;

	public PatternKeywordEstimator(PatternRewardEstimator patternEst, IEstimator keywordEst) {
		preest = patternEst;
		kreest =  keywordEst;
	}

	

	public void setPatternEstimator(PatternRewardEstimator est) {
		preest = est;
	}
	
	
	public void setKeywordRewards(IEstimator est) {
		kreest = est;
	}
	

	 * Assume that the pattern contains more than one infinite element are simply discarded

	public Query nextOptimalKeyword(Keyword[][] ks) throws Exception {
		ArrayList<EP> eps = preest.getAllPatterns();
		if(eps == null || ks == null || eps.size() == 0 || ks.length ==0)	
			return null;
		
		//computer pattern and keyword rewards of given keywords
		double[] prewards = preest.getRewards(eps);
		double[][] krewards = new double[ks.length][1];
		for(int i = 0 ; i < krewards.length ; i++) {
			krewards[i] = kreest.getRewards(ks[i]); 
		}

		//initialize the max record of keywords
		double[] maxValue = new double[krewards.length];
		int[] maxIndex = new int[krewards.length];
		for(int i = 0 ; i < krewards.length ; i++) {
			maxValue[i] = 0.0d;
			maxIndex[i] = 0;
		}
		
		//find the keyword with maximum reward of each infinite elements
		for(int i = 0 ; i < krewards.length ; i++) {
			for(int j = 0 ; j < krewards[i].length ; j++) {
				if(krewards[i][j] > maxValue[i]) {
					maxValue[i] = krewards[i][j];
					maxIndex[i] = j;
				} else if(krewards[i][j] == maxValue[i]) {
					if(ks[i][j].getDf() > ks[i][maxIndex[i]].getDf()) {
						maxValue[i] = krewards[i][j];
						maxIndex[i] = j;
					}
				}
			}
		}
		
		//initialize the max record of the optimal query
		double maxReward = 0.0d;
		int opi = 0,			//the index of the optimal pattern
			okr = 0,			//the row of the optimal keyword 
			okc = 0;			//the column of the optimal keyword 
	
		//find the query with maximum reward
		for(int i = 0 ; i < eps.length ; i++) {
			for(int j = 0 ; j < eps[i].size() ; j++) {
				Element e = eps[i].getElement(j);		
				if(e instanceof IElement) {
					int k = ((IElement)e).getId();
					double reward = prewards[i] * maxValue[k];
					if(reward > maxReward) {
						maxReward = reward;
						opi = i;
						okr = k;
						okc = maxIndex[k];
					} else if(reward == maxReward) {
						//assign priority to pattern containing finite elements
						if(eps[i].containFElement() && !eps[i].containFElement()) {
							maxReward = reward;
							opi = i;
							okr = k;
							okc = maxIndex[k];
						}
					}
				}
			}
		}
		
		//pack the next optimal query
		Query query = new Query(eps[opi],ks[okr][okc]);
		return query;
	}
	
	
	public double getReward(Object obj) throws Exception {
		if(obj == null)	return -1;
		if(!(obj instanceof Query))
			throw new DWIMException ("Does not match the input format of" + this.getClass());
		Query query  = (Query) obj;
		EP ep = query.getPattern();
		Keyword k = query.getKeyword();
		double result = -1.0d;
			result = preest.getReward(ep) * kreest.getReward(k);
		return result;
	}


	public double[] getRewards(Object[] objs) throws Exception {
		if(objs == null || objs.length == 0)	return null;
		if(!(objs[0] instanceof Query)) 
			throw new DWIMException("Does not match the input format of" + this.getClass());
		double[] results = new double[objs.length];
		for(int i = 0 ; i < objs.length ; i++) {
			Query query = (Query) objs[i];
			results[i] = this.getReward(query);
		}
		return results;
	}
	
	public static void main(String args[]) {
		double[][] krewards = new double[20][1];
		krewards[0] = new double[200];
		System.out.println(krewards.length);
		System.out.println(krewards[0].length);
		System.out.println(krewards[1].length);
	
	}
	*/
}

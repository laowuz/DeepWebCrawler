package com.dwim.kv.policy;

import com.dwim.form.analysis.EP;
import com.dwim.form.analysis.IElement;
import com.dwim.form.parser.Query;

public class GenericKeywordEvaluator implements IEvaluator {
	private IEstimator kreest;

	public GenericKeywordEvaluator(IEstimator keywordEst) {
		kreest =  keywordEst;
	}

	


	public Query nextOptimalKeyword(EP[] eps, Keyword[][] ks) throws Exception {
		if(eps == null || eps.length == 0 || ks == null ||  ks.length ==0)	
			return null;
		
		//compute  keyword rewards of given keywords
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
				int k = ((IElement)eps[i].getIthElement(0)).getId();
					double reward =  maxValue[k];
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
		
		//delete the used keyword from record index

		//pack the next optimal query
		Query query = new Query(eps[opi],ks[okr][okc]);
		return query;
	}


	public Query nextOptimalKeyword(EP ep, Keyword[] k) throws Exception {
		EP[] eps = new EP[1];
		eps[0] = ep;
		Keyword[][] ks = new Keyword[1][k.length];
		ks[0] = k;
		return nextOptimalKeyword(eps,ks);
	}
}

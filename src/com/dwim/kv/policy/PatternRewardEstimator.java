package com.dwim.kv.policy;

import java.util.ArrayList;

import com.dwim.form.analysis.EP;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;

public class PatternRewardEstimator {
	
/*	private EP[] patterns;
	private double weight;
	private double[] reward;
	
	
	public PatternRewardEstimator(EP[] patterns) {
		this.patterns = patterns;
		this.reward = new double[patterns.length];
		weight = ConfigMan.PATTERN_REWARD_FADING_FACTOR;
		
		for(int i = 0 ; i < patterns.length ; i++) {
			reward[i] = 1.0d/patterns.length;
		}
	}
	
	public void setReward(int index, int accessRecord, int newRecord)  throws DWIMException {
		this.setReward(patterns[index], accessRecord, newRecord);
	}
	
	public void setReward(EP ep, int accessRecord, int newRecord) throws DWIMException {
		if(ep == null)	throw new DWIMException("corresponding pattern not found");
		boolean found = false;
		for(int i = 0 ; i < patterns.length ; i++) {
			if(patterns.get(i).equals(ep)) {
				reward[i] = reward[i]*weight + (double)newRecord/(double)accessRecord;
				found = true;
			}
		}
		if(!found)	throw new DWIMException("corresponding pattern not found");
		this.normalize();
	}
	
	public ArrayList<EP> getAllPatterns() {
		return patterns;
	}
	

	public double getReward(int id) {
		if(id < 0 || id >= patterns.size()) return -1;
		return reward[id];
	}
	
	private void normalize() {
		double total = 0.0d;
		for(int i = 0 ; i < patterns.size() ; i++) {
			total += reward[i];
		}
		for(int i = 0 ; i < patterns.size() ; i++) {
			reward[i] = reward[i]/total;
		}
	}

	public double getReward(Object obj) throws Exception {
		if(obj == null)	return -1;
		if(!(obj instanceof EP))
			throw new DWIMException ("Does not match the input format of" + this.getClass());
		EP ep  = (EP) obj;
		for(int i = 0 ; i < patterns.size() ; i++) {
			if(patterns.get(i).equals(ep)) {
				return reward[i];
			}
		}
		return -1;
	}


	public double[] getRewards(Object[] s) throws Exception {
		if(s =objs= null || s.objsgth == 0)	return null;
		if(!(s[0]objsstanceof EP)) 
			throw new DWIMException("Does not match the input format of" + this.getClass());
		double[] results = new double[s.lengobjs;
		for(int i = 0 ; i < s.lengthobjsi++) {
			EP ep = (EP) s[i];
			objsults[i] = this.getReward(ep);
		}
		return results;
		
	}

*/
	
	
}

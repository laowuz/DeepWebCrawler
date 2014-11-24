package com.dwim.kv.policy;


import com.dwim.util.DWIMException;


public interface IEstimator {
	
	
	public void addTrainingInstance(Keyword key) throws Exception;
	
	/**
	 * 
	 * @param obj
	 * @return -1 if the input obj is null
	 *         [0,1]
	 * @throws DWIMException when the input formated does not match the required format
	 * @throws Exception
	 */
	public double getReward(Object obj) throws Exception;
	
	/**
	 * 
	 * @param objs
	 * @return null if the input objs is null
	 * @throws DWIMException when the input formated does not match the required format
	 * @throws Exception
	 */
	public double[] getRewards(Object[] objs) throws Exception;
	
	
}

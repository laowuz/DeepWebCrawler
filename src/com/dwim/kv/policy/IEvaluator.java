package com.dwim.kv.policy;


import com.dwim.form.analysis.EP;
import com.dwim.form.parser.Query;
import com.dwim.util.DWIMException;

public interface IEvaluator {
	/**
	 * Assume that the pattern contains more than one infinite element are simply discarded
	 * @return
	 * @throws DWIMException when the input formated does not match the required format
	 * @throws Exception
	 */
	public Query nextOptimalKeyword(EP[] eps, Keyword[][] ks) throws Exception;
	
	
	/**
	 * Assume that the pattern contains more than one infinite element are simply discarded
	 * @return
	 * @throws DWIMException when the input formated does not match the required format
	 * @throws Exception
	 */
	public Query nextOptimalKeyword(EP ep, Keyword[] ks) throws Exception;
}

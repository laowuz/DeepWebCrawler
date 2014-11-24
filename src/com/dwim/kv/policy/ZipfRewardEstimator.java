package com.dwim.kv.policy;

import java.util.ArrayList;

import com.dwim.form.analysis.Element;
import com.dwim.form.analysis.IElement;
import com.dwim.util.ConfigMan;
import com.dwim.util.DWIMException;
import com.dwim.util.HTMLDocument;

import flanagan.analysis.Regression;

public class ZipfRewardEstimator implements IEstimator {

	private ArrayList<Keyword> traningSet;
	private double[] starts;
	private double[] steps;
	private double[] optimalParas;
	private Regression regression;
	private ZipfMandelbrotFunction function;
	private String tsHead;
	private String csHead;
	
	
	public ZipfRewardEstimator() throws DWIMException {
		this(new double[]{0.0d,0.0d,0.0d},new double[]{0.5d,0.5d,0.5d});
	}
	
	
	public ZipfRewardEstimator(double[] starts, double[] steps) throws DWIMException {
	
		
		if((starts == null || starts.length != 3)||(steps == null || steps.length !=3))
			throw new DWIMException ("starts and steps array must be double array with exact three elements.");
		
		this.starts = starts;
		this.steps = steps;
		
		traningSet = new ArrayList<Keyword>(ConfigMan.TRANING_SET_SIZE);
		function = new ZipfMandelbrotFunction();
	
		tsHead = "@relation 'Training Set'\r\n" +
		 "@attribute dfrank numeric\r\n" +
		 "@attribute accessrecords numeric \r\n\r\n" +
		 "@data\r\n";
		csHead = "@relation 'Candidate Set'\r\n" +
		 "@attribute dfrank numeric\r\n" +
		 "@attribute accessrecords numeric\r\n\r\n" +
		 "@data\r\n";
	}
	
	
	public void addTrainingInstance(Keyword k) {
		traningSet.add(k);		
		StringBuffer fileContent = null;
		if(ConfigMan.DEBUG)  {
			fileContent = new StringBuffer(traningSet.size() * 16);
			fileContent.append(tsHead);
		}

		
		//rebuild model
		optimalParas = null;
		double[] x = new double[traningSet.size()];			//ranks
		double[] y = new double[traningSet.size()];			//fvalues
		
		for(int i = 0 ; i < traningSet.size() ; i++) {
			Keyword key = (Keyword)traningSet.get(i);
			x[i] = key.getDfrank();
			y[i] = key.getNumOfAccessDocs();
			if(ConfigMan.DEBUG)  {
				fileContent.append(key.getDfrank() + "," + key.getNumOfAccessDocs());
				fileContent.append(" % " + key.getWord() + "," + key.getNumOfAccessNewDocus());
				fileContent.append("\r\n");
			}
		}
		//call non-linear regression using default tolerance and maximum iterations
	    regression = new Regression(x, y);
	    regression.setNmax(10000);
	    regression.supressErrorMessages();
	    regression.supressPrint();
	    regression.supressStats();

	    regression.simplex(function, starts, steps);
	    
	    optimalParas = regression.getBestEstimates();
	    fileContent.append("%alaph = " + optimalParas[0]);
	    fileContent.append("\r\n");
	    fileContent.append("%beta = " + optimalParas[1]);
	    fileContent.append("\r\n");
	    fileContent.append("%gama = " + optimalParas[2]);

	    //save the training set file
	    if(ConfigMan.DEBUG) {
	    	HTMLDocument.write(ConfigMan.SAVE_CANDIDATE_SET_PATH,"ts_"+traningSet.size(),"utf-8",fileContent.toString(),"arff");
	    }
		
	}
	

	
	/**
	 * Please 
	 */
	public double getReward(Object obj) throws Exception {
		if(obj == null)	return -1;
		if(!(obj instanceof Keyword))
			throw new DWIMException ("Does not match the input format of" + this.getClass());
		Keyword k = (Keyword)obj;
		double result = function.function(optimalParas, k.getDfrank());
		result = (result-k.getDf())/ConfigMan.ALL_DOCUMENT_IN_DB;
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
		
		for(int i = 0 ; i < objs.length ; i++) {
			Keyword key = (Keyword) objs[i];
			double result = function.function(optimalParas, key.getDfrank());
			results[i] = (result-key.getDf())/ConfigMan.ALL_DOCUMENT_IN_DB;
			
			if(ConfigMan.DEBUG) {
				fileContent.append(key.getDfrank() + ",?");
				fileContent.append(" %" + key.getWord() + "," + key.getDf() + "," + result + "," + results[i]);
				fileContent.append("\r\n");
			}
		}
		
		if(ConfigMan.DEBUG) {
			HTMLDocument.write(ConfigMan.SAVE_CANDIDATE_SET_PATH,"cs_"+traningSet.size(),"utf-8",fileContent.toString(),"arff");
		}
		return results;
	}
	
	public static void main(String args[]) throws DWIMException {
		ZipfRewardEstimator z = new ZipfRewardEstimator();

		Keyword firstWord = new Keyword(new IElement("test"),"com");
		//initialize the first instance
		firstWord.setDf(8);
		firstWord.setDfrank(50);
		firstWord.setNumOfAccessDocs(8);
		
		z.addTrainingInstance(firstWord);
		
	}




}

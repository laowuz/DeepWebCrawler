package com.dwim.kv.policy;


import flanagan.analysis.RegressionFunction;

public class ZipfMandelbrotFunction implements RegressionFunction {
	
	
	
	 public double function(double[] p, double[] x){
		 // y = a * (x + b) ^ -r	 
		 double y = 0;
		 
         if((x[0]+p[1] < 0) && p[2] > -1 && p[2] <0 )
        	 y = 1;
         else if ((x[0]+p[1] <= 0) && p[2] >0 && p[2]< 1 ) 
        	 y = Double.MAX_VALUE;
         else 
        	 y=Math.pow((x[0] + p[1]),-1*p[2]);
		 
          y = p[0] * y;
         return y;
	 }
	 
	 public double function(double[] p, double x){
		 // y = a * (x + b) ^ -r
		 double y = 0;
         if((x+p[1] < 0) && p[2] > -1 && p[2] <1 )
        	 y = 1;
         else if ((x+p[1] <= 0) && p[2] >0 && p[2]< 1 ) 
        	 y = Double.MAX_VALUE;
         else 
        	 y=Math.pow((x + p[1]),-1*p[2]);
         
         y = p[0] * y;
         
         return y;
	 }
}

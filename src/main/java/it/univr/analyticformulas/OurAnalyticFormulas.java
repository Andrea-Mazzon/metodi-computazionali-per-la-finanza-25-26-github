package it.univr.analyticformulas;

import net.finmath.functions.AnalyticFormulas;

public class OurAnalyticFormulas {


	public static double blackScholesDownAndOut(double initialValue, double riskFreeRate, double sigma, double maturity, double strike,
			double lowerBarrier) {
		return AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, sigma, maturity, strike) 
				- Math.pow(initialValue/lowerBarrier,-(2*riskFreeRate/(sigma*sigma) - 1)) 
				* AnalyticFormulas.blackScholesOptionValue(lowerBarrier*lowerBarrier/initialValue, riskFreeRate, sigma, maturity, strike);
	}

}

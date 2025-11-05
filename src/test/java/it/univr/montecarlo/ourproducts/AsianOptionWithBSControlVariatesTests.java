package it.univr.montecarlo.ourproducts;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.AsianOption;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class tests the implementation of an Asian option with and without control variates,
 * for the Black-Scholes model.
 */
public class AsianOptionWithBSControlVariatesTests {
		
	
	public static void main(String[] args) throws CalculationException {
	
		// Model parameters
		final double initialValue = 1.0;
		final double riskFreeRate = 0.05;
		final double volatility = 0.30;

		// Process discretization parameters
		final int numberOfPaths= 200000;
		
		double initialTime = 0.0;
		double maturity = 2.0;
		double timeStep = 0.1;
		int numberOfTimeSteps = (int) (maturity/timeStep);
		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);

		final int seed = 1897;
		
		//option parameters
		double	strike = 1.0;
		TimeDiscretization timesForAveraging = new TimeDiscretizationFromArray(0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, maturity);
		
		// We construct the two objects
		AbstractAssetMonteCarloProduct asianOption = new AsianOption(maturity, strike, timesForAveraging);
		
		AbstractAssetMonteCarloProduct asianOptionWithCV = new AsianOptionWithBSControlVariate(maturity, strike, timesForAveraging);
		
		BrownianMotion ourDriver = new BrownianMotionFromMersenneRandomNumbers(times, 1 /* numberOfFactors */, numberOfPaths, seed);

		//we construct an object of type MonteCarloBlackScholesModel: it represents the simulation of a Black-Scholes process
		MonteCarloBlackScholesModel blackScholesProcess = new MonteCarloBlackScholesModel(initialValue, riskFreeRate, volatility, ourDriver);
		
		// here we want the RandomVariable representing all payoffs: we want to compute its standard error
		RandomVariable asianOptionPayoff = asianOption.getValue(0.0, blackScholesProcess);
		RandomVariable asianOptionWithCVPayoff = asianOptionWithCV.getValue(0.0, blackScholesProcess);
		
		//these are the usual prices
		System.out.println("Expected value of Asian option: " + asianOptionPayoff.getAverage());
		System.out.println("Expected value of asian option with control variate: " + asianOptionWithCVPayoff.getAverage());
		System.out.println();
		
		//these are the standard deviations
		System.out.println("Standard deviation of Asian option: " + asianOptionPayoff.getStandardDeviation());
		System.out.println("Standard deviation of Asian option with control variate: " + asianOptionWithCVPayoff.getStandardDeviation());
		System.out.println("Ratio: " + asianOptionPayoff.getStandardDeviation() / asianOptionWithCVPayoff.getStandardDeviation());

	}			
}

package it.univr.montecarlo.ourproducts;

import it.univr.analyticformulas.OurAnalyticFormulas;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * This class tests the implementation of BarrierOption. In particular, we construct an object of type MonteCarloBlackScholesModel
 * and we give it as underlying to the getValue method of BarrierOption. We test the option when there is only the lower barrier, and
 * compare the value we get against the analytic value.
 * 
 * @author Andrea Mazzon
 *
 */
public class BarrierOptionTest {
	
	public static void main(String[] args) throws CalculationException {

		//option parameters
		double upperBarrier = Long.MAX_VALUE;
		double lowerBarrier = 90;
		double maturity = 2.0;		
		double strike = 100;
		
		AbstractAssetMonteCarloProduct optionValueCalculator = new BarrierOption(maturity, strike, lowerBarrier, upperBarrier);
		
		
		//time discretization parameters
		double initialTime = 0.0;
		double timeStep = 0.1;
		int numberOfTimeSteps = (int) (maturity/timeStep);
		
		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);
		
		//simulation parameters
		int numberOfPaths = 10000;
		int seed = 1897;
		

		BrownianMotion ourDriver = new BrownianMotionFromMersenneRandomNumbers(times, 1 /* numberOfFactors */, numberOfPaths, seed);
		
		//model (i.e., underlying) parameters
		double initialValue = 100;
		double riskFreeRate = 0.0;
		double volatility = 0.2;
		
		//we construct an object of type MonteCarloBlackScholesModel: it represents the simulation of a Black-Scholes process
		MonteCarloBlackScholesModel blackScholesProcess = new MonteCarloBlackScholesModel(initialValue, riskFreeRate, volatility, ourDriver);
		
		double monteCarloPrice = optionValueCalculator.getValue(blackScholesProcess);

		double analyticPrice = OurAnalyticFormulas.blackScholesDownAndOut(initialValue, riskFreeRate, volatility, maturity, strike, lowerBarrier);

		
		System.out.println("The Monte Carlo price is: " + monteCarloPrice);
		System.out.println("The analytic price is: " + analyticPrice);

	}

}


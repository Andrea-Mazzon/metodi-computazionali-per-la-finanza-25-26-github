package it.univr.montecarlo.finmathlibraryproducts;

import java.util.Arrays;

import it.univr.montecarlo.ourproducts.BermudanPutOption;
import it.univr.montecarlo.ourproducts.BermudanPutOption.ExerciseMethod;
import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * In this class we test the Finmath library implementation of a Bermudan option
 */
public class AmericanOptionTest {

	public static void main(String[] args) throws CalculationException {

		//model parameters
		double spotPrice = 1.0;
		double riskFreeRate = 0.0;
		double volatility = 0.4;
		
		//option parameters
		double maturity = 3.5;		
		double strike = 1.0;
		
		//time discretization parameters
		double initialTime = 0.0;
		double timeStep = 0.1;
		int numberOfTimeSteps = (int) (maturity/timeStep);
				
		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);
		
		//In this way, we specify that it is actually (an approximation of) an American option: it can be exercised at every tiscretized time
		double[] exerciseDatesForAmericanOption = times.getAsDoubleArray();
		
		double[] notionals = new double[exerciseDatesForAmericanOption.length];
		Arrays.fill(notionals, 1.0);
		
		double[] strikes = new double[exerciseDatesForAmericanOption.length];
		Arrays.fill(strikes, strike);
		
		AbstractAssetMonteCarloProduct optionValueCalculator = new BermudanPutOption(exerciseDatesForAmericanOption, notionals, strikes, ExerciseMethod.UPPER_BOUND_METHOD);
		
		
		//simulation parameters
		int numberOfPaths = 100000;
		int seed = 1897;
		

		BrownianMotion ourDriver = new BrownianMotionFromMersenneRandomNumbers(times, 1 /* numberOfFactors */, numberOfPaths, seed);
		
		//we construct an object of type MonteCarloBlackScholesModel: it represents the simulation of a Black-Scholes process
		MonteCarloBlackScholesModel blackScholesProcess = new MonteCarloBlackScholesModel(spotPrice, riskFreeRate, volatility, ourDriver);
		
		double monteCarloPrice = optionValueCalculator.getValue(blackScholesProcess);

		
		System.out.println("The Monte Carlo price is: " + monteCarloPrice);

	}

}

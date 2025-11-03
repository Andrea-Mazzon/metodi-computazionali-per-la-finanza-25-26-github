package it.univr.montecarlo.finmathlibraryproducts;


import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.IndependentIncrements;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloAssetModel;
import net.finmath.montecarlo.assetderivativevaluation.models.BachelierModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;
import net.finmath.montecarlo.model.ProcessModel;
import net.finmath.stochastic.RandomVariable;


/**
 * In this class we test the Finmath library implementation of the Monte Carlo method and discretization of a stochastic
 * process for the evaluation of an European call option. We want the underlying to be a Bachelier model and use a classic
 * Euler-Maruyama scheme.
 *
 * @author Andrea Mazzon
 *
 */
public class EuropeanOptionTest {

	public static void main(String[] args) throws CalculationException {

		//parameters for the option

		double maturity = 1.0;
		double strike = 2.0;

		//so we construct the option object
		AbstractAssetMonteCarloProduct ourOption = new EuropeanOption (maturity, strike);
		
		//parameters for the model (i.e., for the SDE)
		double initialValue = 2.0;
		double riskFreeRate = 0.0;
		double volatility = 0.2;

		//based on this, we can already compute and print the analytic value
		double analyticValue = AnalyticFormulas.bachelierOptionValue(initialValue, volatility, maturity, strike, 1.0);
		System.out.println("The analytic value of the option is " + analyticValue);

		//here we start the construction of the object of type AssetModelMonteCarloSimulationModel
		
		//what we simulate
		ProcessModel ourModel = new BachelierModel(initialValue, riskFreeRate, volatility);
		
		//how we simulate it 
		
		//simulation parameters
		int seed = 1897;
		int numberOfPaths = 100000;
		int numberOfFactors = 1;//one-dimensional
		
		double timeStep = 0.1;
		int numberOfTimeSteps = (int) (maturity/timeStep);
		
		TimeDiscretization  times = new TimeDiscretizationFromArray(0.0, numberOfTimeSteps, timeStep);
		
		IndependentIncrements ourDriver =
				new BrownianMotionFromMersenneRandomNumbers(times,numberOfFactors,numberOfPaths,seed);
		
		//we put togetehr what we simulate and how we simulate it
		AssetModelMonteCarloSimulationModel ourSimulation = new MonteCarloAssetModel(ourModel, ourDriver);
		
		
		RandomVariable simulatedPayoff = ourOption.getValue(0.0, ourSimulation);
		double monteCarloPrice = simulatedPayoff.getAverage();
		
		//alternative (used more ofter)
		//double monteCarloPrice = ourOption.getValue(ourSimulation);
		
		System.out.println("Monte Carlo price: " + monteCarloPrice);
		
	}
}

package it.univr.montecarlo.finmathlibraryproducts;

import java.util.Random;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.IndependentIncrements;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloAssetModel;
import net.finmath.montecarlo.assetderivativevaluation.models.BachelierModel;
import net.finmath.montecarlo.assetderivativevaluation.products.EuropeanOption;
import net.finmath.montecarlo.model.ProcessModel;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;


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

		//parameters for the model (i.e., for the SDE)

		double initialValue = 2.0;
		double riskFreeRate = 0.0;
		double volatility = 0.2;

		//we compute and print the analytic value

		double analyticValue = AnalyticFormulas.bachelierOptionValue(initialValue, volatility, maturity, strike, 1.0);
		System.out.println("The analytic value of the option is " + analyticValue);

		//from here, we will proceed together
		
	}
}

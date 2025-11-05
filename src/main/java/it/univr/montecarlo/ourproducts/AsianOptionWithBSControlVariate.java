package it.univr.montecarlo.ourproducts;

import net.finmath.exception.CalculationException;
import net.finmath.functions.AnalyticFormulas;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloAssetModel;
import net.finmath.montecarlo.assetderivativevaluation.models.BlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.montecarlo.assetderivativevaluation.products.AsianOption;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;


/**
 * Implements the valuation of an Asian option with a control variate.
 *
 * The product represents an Asian option written on an underlying asset S, with
 * strike K, maturity T, and averaging dates T_1, ..., T_n (where T_n = T). The payoff
 * at maturity is given by
 *
 *     max(A(T) - K, 0)
 *
 * where A(T) is the arithmetic average of the underlying asset prices at the
 * averaging dates:
 *
 *     A(T) = (1/n) * (S(T_1) + S(T_2) + ... + S(T_n))
 *
 * The valuation is performed by Monte Carlo simulation under a given asset model.
 * If the model used is a BlackScholesModel, a control variate method is applied to
 * reduce the variance of the Monte Carlo estimator.
 *
 * The control variate is constructed as the average of the discounted payoffs of
 * standard European call options with the same strike, and maturities equal to the
 * averaging times. The expected value of this control variate is known in closed
 * form from the Black–Scholes formula. Since the avergae of the payoffs of these calls
 * is highly correlated with the payoff of the Asian option, this allows us to build a
 * variance-reduced estimator:
 *
 *     Z - beta * (Y - E[Y]),
 *
 * where Z is the Monte Carlo estimate of the Asian option payoff, Y is the simulated
 * value of the control variate, E[Y] is its analytic expectation under Black–Scholes,
 * and beta is the optimal control variate coefficient estimated as
 * Cov(Z, Y) / Var(Y).
 *
 * If the model is not a Black–Scholes one, the valuation falls back to a standard
 * Monte Carlo computation of the Asian option without any variance reduction.
 *
 * @author Andrea Mazzon
 */
public class AsianOptionWithBSControlVariate extends AbstractAssetMonteCarloProduct {

	private final double maturity;
	private final double strike;
	private final TimeDiscretization timesForAveraging;
	private final Integer underlyingIndex;


	/**
	 * Construct a product representing an Asian option on an asset S (where S the asset with index 0 from the model - single asset case).
	 * A(T) = 1/n sum_{i=1,...,n} S(T_i), where T_i are given averaging times, with T=T_n.
	 *
	 * @param strike The strike K in the option payoff max( sign * (A(T)-K), 0)
	 * @param maturity The maturity T in the option payoff max( sign * (A(T)-K), 0)
	 * @param timesForAveraging The times t_i used in the calculation of A(T) = 1/n sum_{i=1,...,n} S(t_i).
	 * @param underlyingIndex The index of the asset S to be fetched from the model
	 * @param callOrPutSign The parameter sign in the payoff max( sign * (A(T)-K), 0) (either +1 or -1).
	 */
	public AsianOptionWithBSControlVariate(final double maturity, final double strike, final TimeDiscretization timesForAveraging, final int underlyingIndex) {
		super();
		this.maturity = maturity;
		this.strike = strike;
		this.timesForAveraging = timesForAveraging;
		this.underlyingIndex = underlyingIndex;
	}

	/**
	 * Construct a product representing an Asian option on an asset S (where S the asset with index 0 from the model - single asset case).
	 * A(T) = 1/n sum_{i=1,...,n} S(T_i), where T_i are given averaging times, with T=T_n.
	 *
	 * @param strike The strike K in the option payoff max( sign * (A(T)-K), 0)
	 * @param maturity The maturity T in the option payoff max( sign * (A(T)-K), 0)
	 * @param timesForAveraging The times t_i used in the calculation of A(T) = 1/n sum_{i=1,...,n} S(t_i).
	 * @param callOrPutSign The parameter sign in the payoff max( sign * (A(T)-K), 0) (either +1 or -1).
	 */
	public AsianOptionWithBSControlVariate(final double maturity, final double strike, final TimeDiscretization timesForAveraging) {
		this(maturity, strike, timesForAveraging, 0);
	}


	// Computing the mean of Call options
	private double computeAnalyticValue(double initialValue,	double riskFreeRate, double volatility, double evaluationTime) {

		int numberOfTimes = timesForAveraging.getNumberOfTimes();
		double callSum = 0.0;

		for (double timeToMaturity : timesForAveraging.getAsDoubleArray()) {

			callSum += AnalyticFormulas.blackScholesOptionValue(initialValue, riskFreeRate, volatility, timeToMaturity-evaluationTime, strike, true);

		}
		return 1.0 / numberOfTimes * callSum;

	}


	/**
	 * This method returns the value random variable of the product within the specified model, evaluated at a given evalutationTime.
	 * Note: For a lattice this is often the value conditional to evalutationTime, for a Monte-Carlo simulation this is the (sum of) value discounted to evaluation time.
	 * Cashflows prior evaluationTime are not considered.
	 *
	 * @param evaluationTime The time on which this products value should be observed.
	 * @return The random variable representing the value of the product discounted to evaluation time
	 * @throws net.finmath.exception.CalculationException Thrown if the valuation fails, specific cause may be available via the <code>cause()</code> method.
	 */
	@Override
	public RandomVariable getValue(final double evaluationTime, final AssetModelMonteCarloSimulationModel model) throws CalculationException {

		
		//
		/*
		 * We first compute the "standard" discounted Monte-Carlo payoff via the Finmath class:
		 * at this point, the RandomVariable values contains the path-wise discounted values
		 * (the payoff multiplied with the numeraire ratio N(t)/N(T)), such that taking the expectation
		 * would give us the value of the option.
		 */
		AsianOption standardAsianOption = new AsianOption(maturity, strike, timesForAveraging, underlyingIndex);

		RandomVariable standardPayoff = standardAsianOption.getValue(evaluationTime, model);
		
		//if the model is not Black-Scholes, we return it and print a "warning" message
		if(!(((MonteCarloAssetModel) model).getModel() instanceof BlackScholesModel)) {
			System.out.println("The model is not Black-Scholes: we are going to perform the standard Monte Carlo");
			return standardPayoff;
		}

		/*
		 * If the model is Black-Scholes, we add a control variate for the Asian option. We take the average of the
		 * call options having maturities equal to any averaging time T_1, ..., T_n. 
		 */
		// We know that the underlying follows BS dynamics
		BlackScholesModel processModel = (BlackScholesModel) ((MonteCarloAssetModel) model).getModel();

		// We compute the sum of the simulated discounted payoffs
		RandomVariable simulationSum = model.getRandomVariableForConstant(0.0);//initialization

		// Computation via for loop
		for(double time : timesForAveraging) {
			double riskFreeRate = processModel.getRiskFreeRate().doubleValue();
			RandomVariable underlying	= model.getAssetValue(time, underlyingIndex);
			RandomVariable callPayoff = underlying.sub(strike).floor(0.0).mult(Math.exp(-riskFreeRate * (time-evaluationTime)));
			simulationSum = simulationSum.add(callPayoff);
		}

		// The average
		RandomVariable simulationAverage = simulationSum.div(timesForAveraging.getNumberOfTimes());

		// We compute the optimal beta
		double covariance = standardPayoff.covariance(simulationAverage).doubleValue();
		double varianceOfControlVariate = simulationAverage.getVariance();
		double optimalBeta = covariance/varianceOfControlVariate;

		// We compute the analytic value of our estimator
		double initialValueOfStock = model.getAssetValue(0, 0).doubleValue();
		double riskFreeRate = processModel.getRiskFreeRate().doubleValue();
		double volatility = processModel.getVolatility().doubleValue();

		double analyticValueControlVariate = computeAnalyticValue(initialValueOfStock, riskFreeRate,volatility, evaluationTime);

		//and have therefore the term to subtract
		RandomVariable termToSubtract = simulationAverage.sub(analyticValueControlVariate).mult(optimalBeta);

		RandomVariable controlledValues = standardPayoff.sub(termToSubtract);

		return controlledValues;
	}
}
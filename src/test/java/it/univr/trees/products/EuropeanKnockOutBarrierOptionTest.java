package it.univr.trees.products;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import it.univr.trees.models.CoxRossRubinsteinModel;
import it.univr.trees.models.JarrowRuddModel;
import it.univr.trees.models.LeisenReimerModel;
import net.finmath.plots.Named;
import net.finmath.plots.Plot2D;
import it.univr.analyticformulas.OurAnalyticFormulas;


/**
 * In this class we test the implementation of Knock Out Options options via Binomial models: we plot the prices for the three
 * approximating methods and for increasing number of times.
 * 
 * @author Andrea Mazzon
 *
 */
public class EuropeanKnockOutBarrierOptionTest {
	
	public static void main(String[] strings) throws Exception {

		double spotPrice = 2;
		double riskFreeRate = 0.2;
		double volatility = 0.7;
		double lastTime = 3;

		double strike = 2;

		double lowerBarrier = 1.7;
		
		
		DoubleUnaryOperator payoffFunction = (x) -> Math.max(x - strike, 0);
		
		EuropeanKnockOutBarrierOption ourOption = new EuropeanKnockOutBarrierOption(lastTime, payoffFunction, lowerBarrier, Double.MAX_VALUE);
		
		
		/*
		 * We want to plot the results we get for our approximating models when we increase the number of times.
		 * In the same plot, we want to show also the analytic price of the option, as a benchmark.
		 * We use the Plot2D class of finmath-lib-plot-extensions. In order to do that, we have to define the
		 * functions to plot as objects of type DoubleUnaryOperator.
		 * In our case, we want these functions to take the number of times and return the prices approximated
		 * with this number of times. For us numberOfTimesForFunction should be an int, but in order to define
		 * a DoubleUnaryOperator one should take a double. So we first treat it as a double and then we downcast
		 * it when passing it to the getValue of EuropeanNonPathDependentOption.
		 */		
		DoubleUnaryOperator numberOfTimesToPriceCoxRossRubinsteinModel = (numberOfTimesForFunction) -> {
			CoxRossRubinsteinModel ourModelForFunction = new CoxRossRubinsteinModel(spotPrice, riskFreeRate, volatility, lastTime, (int) numberOfTimesForFunction);		
			return ourOption.getValue(ourModelForFunction);
		};
		
		DoubleUnaryOperator numberOfTimesToPriceJarrowRuddModel = (numberOfTimesForFunction) -> {
			JarrowRuddModel ourModelForFunction = new JarrowRuddModel(spotPrice, riskFreeRate, volatility, lastTime, (int) numberOfTimesForFunction);		
			return ourOption.getValue(ourModelForFunction);
		};
		
		
		DoubleUnaryOperator numberOfTimesToPriceLeisenReimer = (numberOfTimesForFunction) -> {
			LeisenReimerModel ourModelForFunction = new LeisenReimerModel(spotPrice, riskFreeRate, volatility, lastTime, (int) numberOfTimesForFunction, strike);		
			return ourOption.getValue(ourModelForFunction);
		};
		
		/*
		 * This is the DoubleUnaryOperator to plot the analytic price. "Dummy" in the sense that it is a function
		 * that always gives the same value.
		 */
		DoubleUnaryOperator dummyFunctionBlackScholesPrice = (numberOfTimesForFunction) -> {
			return OurAnalyticFormulas.blackScholesDownAndOut(spotPrice, riskFreeRate, volatility, lastTime, strike, lowerBarrier);
		};
		
	
		int maxNumberOfTimes = 1500;
		int minNumberOfTimes = 10;
		
		//plots
		
		final Plot2D plotCRR = new Plot2D(minNumberOfTimes, maxNumberOfTimes, maxNumberOfTimes-minNumberOfTimes+1, Arrays.asList(
				new Named<DoubleUnaryOperator>("Cox Ross Rubinstein", numberOfTimesToPriceCoxRossRubinsteinModel),
				new Named<DoubleUnaryOperator>("Analytic price", dummyFunctionBlackScholesPrice)));
		
		plotCRR.setXAxisLabel("Number of discretized times");
		plotCRR.setYAxisLabel("Price");
		plotCRR.setIsLegendVisible(true);
		plotCRR.show();
		
		final Plot2D plotJR = new Plot2D(minNumberOfTimes, maxNumberOfTimes, maxNumberOfTimes-minNumberOfTimes+1, Arrays.asList(
				new Named<DoubleUnaryOperator>("Jarrow Rudd", numberOfTimesToPriceJarrowRuddModel),
				new Named<DoubleUnaryOperator>("Analytic price", dummyFunctionBlackScholesPrice)));
		plotJR.setXAxisLabel("Number of discretized times");
		plotJR.setYAxisLabel("Price");
		plotJR.setIsLegendVisible(true);
		plotJR.show();
		
		final Plot2D plotLR = new Plot2D(minNumberOfTimes, maxNumberOfTimes,  maxNumberOfTimes-minNumberOfTimes+1, Arrays.asList(
				new Named<DoubleUnaryOperator>("Leisen Reimer", numberOfTimesToPriceLeisenReimer),
				new Named<DoubleUnaryOperator>("Analytic price", dummyFunctionBlackScholesPrice)));
		plotLR.setXAxisLabel("Number of discretized times");
		plotLR.setYAxisLabel("Price");
		plotLR.setIsLegendVisible(true);
		plotLR.show();
	}
}

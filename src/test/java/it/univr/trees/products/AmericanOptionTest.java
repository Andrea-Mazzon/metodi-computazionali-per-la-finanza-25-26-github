package it.univr.trees.products;


import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import it.univr.trees.models.CoxRossRubinsteinModel;
import it.univr.trees.models.JarrowRuddModel;
import it.univr.trees.models.LeisenReimerModel;
import net.finmath.plots.Named;
import net.finmath.plots.Plot2D;


/**
 * In this class we test the implementation of American options via Binomial models: we plot the prices for the three
 * approximating methods and for increasing number of times.
 * 
 * @author Andrea Mazzon
 *
 */
public class AmericanOptionTest {
	public static void main(String[] strings) throws Exception {


		//model parameters
		double spotPrice = 1.0;
		double riskFreeRate = 0.06;
		double volatility = 0.2;

		//option parameters
		double strike = 1.0;
		DoubleUnaryOperator payoffFunction = (x) -> Math.max(strike-x, 0);

		double maturity = 1.0;

		AmericanOption ourOption = new AmericanOption(maturity, payoffFunction);

		/*
		 * We want to plot the results we get for our approximating models when we increase the number of times.
		 * We use the Plot2D class of finmath-lib-plot-extensions. In order to do that, we have to define the
		 * functions to plot as objects of type DoubleUnaryOperator.
		 * In our case, we want these functions to take the number of times and return the prices approximated
		 * with this number of times. For us numberOfTimesForFunction should be an int, but in order to define
		 * a DoubleUnaryOperator one should take a double. So we first treat it as a double and then we downcast
		 * it when passing it to the getValue of EuropeanBarrierOption.
		 */		
		DoubleUnaryOperator numberOfTimesToPriceCoxRossRubinsteinModel = (numberOfTimesForFunction) -> {
			CoxRossRubinsteinModel ourModelForFunction = new CoxRossRubinsteinModel(spotPrice, riskFreeRate, volatility, maturity, (int) numberOfTimesForFunction);		
			return ourOption.getValue(ourModelForFunction);
		};

		DoubleUnaryOperator numberOfTimesToPriceJarrowRuddModel = (numberOfTimesForFunction) -> {
			JarrowRuddModel ourModelForFunction = new JarrowRuddModel(spotPrice, riskFreeRate, volatility, maturity, (int) numberOfTimesForFunction);		
			return ourOption.getValue(ourModelForFunction);
		};


		DoubleUnaryOperator numberOfTimesToPriceLeisenReimerModel = (numberOfTimesForFunction) -> {
			LeisenReimerModel ourModelForFunction = new LeisenReimerModel(spotPrice, riskFreeRate, volatility, maturity, (int) numberOfTimesForFunction, strike);		
			return ourOption.getValue(ourModelForFunction);
		};

		int maxNumberOfTimes = 300;
		int minNumberOfTimes = 10;

		//plots
		final Plot2D plot = new Plot2D(minNumberOfTimes, maxNumberOfTimes, maxNumberOfTimes-minNumberOfTimes+1, Arrays.asList(
				new Named<DoubleUnaryOperator>("Cox Ross Rubinstein", numberOfTimesToPriceCoxRossRubinsteinModel),
				new Named<DoubleUnaryOperator>("Jarrow-Rudd", numberOfTimesToPriceJarrowRuddModel),
				new Named<DoubleUnaryOperator>("Leisen-Reimer", numberOfTimesToPriceLeisenReimerModel)
				));

		plot.setTitle("American option pricing");
		plot.setXAxisLabel("Number of discretized times");
		plot.setYAxisLabel("Price");
		plot.setIsLegendVisible(true);
		
		plot.show();

	}
}
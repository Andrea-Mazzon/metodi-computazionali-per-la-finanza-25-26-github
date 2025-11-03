package it.univr.finitedifferences.finmathlibraryimplementation;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

import net.finmath.finitedifference.models.FDMBlackScholesModel;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
import net.finmath.finitedifference.products.FDMEuropeanCallOption;
import net.finmath.finitedifference.products.FDMEuropeanPutOption;
import net.finmath.finitedifference.products.FiniteDifference1DProduct;
import net.finmath.interpolation.RationalFunctionInterpolation;
import net.finmath.plots.Named;
import net.finmath.plots.Plot2D;
import net.finmath.plots.Plotable2D;
import net.finmath.plots.PlotableFunction2D;


/**
 * In this class we plot the prices of call and put options with Black-Scholes underlying, computed by solving
 * the associated PDE. In order to do that, we use the implementation of the Finmath library.
 * 
 * @author Andrea Mazzon
 *
 */
public class CallAndPutPlot {

	
	public static void main(String[] strings) throws Exception {
		//this is used to print only the first digit of the maturity
		final DecimalFormat formatterMaturity = new DecimalFormat("0.0");
		formatterMaturity.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		
		//model parameters
		final double riskFreeRate = 0.06;
		final double volatility = 0.4;
		final double initialValue = 50;

		//discretization parameters
		final int numberSpaceSteps = 120;
		final int numberTimeSteps = 120;
		
		final int numberStandardDeviations = 5;
		final double theta = 0.5;

		//option parameters
		final double optionStrike = 50;
		double maturity = 0.1;//this is just the first maturity for which we do the plot
		


		final FiniteDifference1DModel model = new FDMBlackScholesModel(
				numberTimeSteps,//for the discretization of the time interval
				numberSpaceSteps,//for the discretization of the space domain
				numberStandardDeviations,//this enters in the computation of the right and left end of the space domain
				optionStrike,//in the finmath library, there is written that this is the center of the space discretization, but this is not true: it has no effect
				theta,
				initialValue,//the center of the discretization in the class FDMThetaMethod is initialValue*exp(r*timeHorizon)
				riskFreeRate,
				volatility);

		
		//the object representing the option (like the classes implementing AbstractMonteCarloProduct)
		FiniteDifference1DProduct callOption = new FDMEuropeanCallOption(maturity, optionStrike);

		
		/* 
		 * We don't really have to know that to call the method, but anyway: this method calls a method with the same name
		 * (but different argument list) defined in FiniteDifference1DModel. There, the boundary conditions are given according to
		 * the option: note that  FDMEuropeanCallOption has itw own implementation of the methods giving the boundary
		 * conditions. 
		 */
		double[][] returnedValues = callOption.getValue(0.0, model);

		//there are the space variables (i.e., the initial values) for which the prices are computed
		double[] initialStockPricesForCall = returnedValues[0];

		//these are the corresponding prices
		double[] callOptionValues = returnedValues[1];

		/*
		 * Since we want to plot a function, and not only the array with respect to the other array, we have to interpolate.
		 * That is, we want to find a continuous function f such that f(x_i)=y_i for all i, where x_i are the initial values
		 * and y_i the prices. We do that with the help of the Finmath library itself.
		 */
		final RationalFunctionInterpolation callInterpolation = new RationalFunctionInterpolation(initialStockPricesForCall, callOptionValues);

		//in this way, we can define the DoubleUnaryOperator we plot
		DoubleUnaryOperator interpolatedCallFunction = x -> callInterpolation.getValue(x);

		int numberOfPlottedValues = 100;

		final Plot2D plotCallAndPut = new Plot2D(initialStockPricesForCall[0], initialStockPricesForCall[initialStockPricesForCall.length-1],
				numberOfPlottedValues, Arrays.asList(
						new Named<DoubleUnaryOperator>("Call and put plot", interpolatedCallFunction)));

		plotCallAndPut.setXAxisLabel("Initial value");
		plotCallAndPut.setYAxisLabel("Call price for maturity " + formatterMaturity.format(maturity));
		plotCallAndPut.setYRange(0, 200);
		plotCallAndPut.show();
		Thread.sleep(500);


		//now we want to plot the prices also for other maturities, one after the other
		while (maturity < 2.1) {

			//we just update the following objects
			callOption = new FDMEuropeanCallOption(maturity, optionStrike);
			returnedValues = callOption.getValue(0.0, model);
			initialStockPricesForCall = returnedValues[0];
			callOptionValues = returnedValues[1];

			final RationalFunctionInterpolation callInterpolationInLoop =
					new RationalFunctionInterpolation(initialStockPricesForCall, callOptionValues);

			interpolatedCallFunction = x -> callInterpolationInLoop.getValue(x);

			//this is what we want to plot, at every iteration of the while loop
			final List<Plotable2D> plotables = Arrays.asList(
					new PlotableFunction2D(initialStockPricesForCall[0], initialStockPricesForCall[initialStockPricesForCall.length-1],
							numberOfPlottedValues, new Named<DoubleUnaryOperator>("Call function", interpolatedCallFunction), null));

			plotCallAndPut.update(plotables);//in this way, all the plots are in the same figure
			plotCallAndPut.setYAxisLabel("Call price for maturity " + formatterMaturity.format(maturity));
			Thread.sleep(500);
			maturity += 0.1;
		}
		
		
		//NOW THE PUT: we want to plot the put price in the same figure as for the call
		
		
		plotCallAndPut.setYRange(0, 60);

		maturity = 0.1;

		while (maturity < 2.1) {

			//we just update the following objects
			FiniteDifference1DProduct putOption = new FDMEuropeanPutOption(maturity, optionStrike);
			/* 
			 * We don't really have to know that to call the method, but anyway: this method calls a method with the same name
			 * (but different argument list) defined in FiniteDifference1DModel. There, the boundary conditions are given according to
			 * the option: note that  FDMEuropeanCallOption has itw own implementation of the methods giving the boundary
			 * conditions. 
			 */
			double[][] returnedValuesForPut = putOption.getValue(0.0, model);

			//there are the space variables (i.e., the initial values) for which the prices are computed
			double[] initialStockPricesForPut = returnedValuesForPut[0];

			//these are the corresponding prices
			double[] putOptionValues = returnedValuesForPut[1];

			/*
			 * Since we want to plot a function, and not only the array with respect to the other array, we have to interpolate.
			 * That is, we want to find a continuous function f such that f(x_i)=y_i for all i, where x_i are the initial values
			 * and y_i the prices. We do that with the help of the Finmath library itself.
			 */
			final RationalFunctionInterpolation putInterpolation = new RationalFunctionInterpolation(initialStockPricesForPut, putOptionValues);

			//in this way, we can define the DoubleUnaryOperator we plot
			DoubleUnaryOperator interpolatedPutFunction = x -> putInterpolation.getValue(x);


			//this is what we want to plot, at every iteration of the while loop
			final List<Plotable2D> plotables = Arrays.asList(
					new PlotableFunction2D(initialStockPricesForPut[0], initialStockPricesForPut[initialStockPricesForPut.length-1],
							numberOfPlottedValues, new Named<DoubleUnaryOperator>("Put function", interpolatedPutFunction), null));

			plotCallAndPut.update(plotables);//in this way, all the plots are in the same figure
			plotCallAndPut.setYAxisLabel("Put price for maturity " + formatterMaturity.format(maturity));
			Thread.sleep(500);
			maturity += 0.1;
		}


	}

}

package it.univr.finitedifferences.ourproducts;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

import it.univr.usefulmethodsarrays.UsefulMethodsArrays;
import net.finmath.finitedifference.models.FDMBlackScholesModel;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
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
public class BarrierCallPlot {
	
	public static void main(String[] strings) throws Exception {
		//this is used to print only the first digit of the maturity
		DecimalFormat formatterMaturity = new DecimalFormat("0.0");
		formatterMaturity.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		
		//model parameters
		double riskFreeRate = 0.06;
		double volatility = 0.4;
		double initialValue = 50;

		//discretization parameters
		int numTimeSteps = 35;
		int numSpaceSteps = 120;
		int numStandardDeviations = 5;
		double theta = 0.5;

		//option parameters
		double optionStrike = 50;
		double lowerBarrier = 30;
		double upperBarrier = 80;

		FiniteDifference1DModel model = new FDMBlackScholesModel(
				numTimeSteps,//for the discretization of the time interval
				numSpaceSteps,//for the discretization of the space domain
				numStandardDeviations,//this enters in the computation of the right and left end of the space domain
				optionStrike,//in the finmath library, there is written that this is the center of the space discretization, but this is not true: it has no effect
				theta,
				initialValue,//the center of the discretization in the class FDMThetaMethod is initialValue*exp(r*timeHorizon)
				riskFreeRate,
				volatility);

		double maturity = 0.1;

		//the object representing the option (like the classes implementing AbstractMonteCarloProduct)
		FiniteDifference1DProduct optionValueFDCalculator = new FDMBarrierCallOption(maturity, optionStrike, lowerBarrier, upperBarrier, theta);

		/* 
		 * This method calls a method with the same name (but different argument list) defined in FiniteDifference1DModel.
		 * There, the boundary conditions are given according to the option: note that  FDMEuropeanCallOption has its own
		 * implementation of the methods giving the boundary conditions. 
		 */
		double[][] valueCallFDM = optionValueFDCalculator.getValue(0.0, model);

		//these are the space variables (i.e., the initial values) for which the prices are computed
		double[] initialStockPriceForCall = valueCallFDM[0];

		//these are the corresponding prices
		double[] callOptionValue = valueCallFDM[1];

		/*
		 * Since we want to plot a function, and not only the array with respect to the other array, we have to interpolate.
		 * That is, we want to find a continuous function f such that f(x_i)=y_i for all i, where x_i are the initial values
		 * and y_i the prices. We do that with the help of the Finmath library itself.
		 */
		RationalFunctionInterpolation callInterpolation = new RationalFunctionInterpolation(initialStockPriceForCall, callOptionValue);

		//in this way, we can define the DoubleUnaryOperator we plot
		DoubleUnaryOperator interpolatedCallFunction = x -> callInterpolation.getValue(x);

		int numberOfPlottedValues = 100;

		Plot2D plotCallAndPut = new Plot2D(initialStockPriceForCall[0], initialStockPriceForCall[initialStockPriceForCall.length-1],
				numberOfPlottedValues, Arrays.asList(
						new Named<DoubleUnaryOperator>("Call and put plot", interpolatedCallFunction)));

		plotCallAndPut.setXAxisLabel("Initial value");
		plotCallAndPut.setYAxisLabel("Call price for maturity " + formatterMaturity.format(maturity));
		plotCallAndPut.setXRange(lowerBarrier, upperBarrier-1);
		plotCallAndPut.setYRange(0, UsefulMethodsArrays.getMax(callOptionValue));
		plotCallAndPut.show();
		Thread.sleep(1000);


		//now we want to plot the prices also for other maturities, one after the other
		while (maturity < 2.1) {

			//we just update the following objects
			optionValueFDCalculator = new FDMBarrierCallOption(maturity, optionStrike, lowerBarrier, upperBarrier, theta);
			valueCallFDM = optionValueFDCalculator.getValue(0.0, model);
			initialStockPriceForCall = valueCallFDM[0];
			callOptionValue = valueCallFDM[1];

			RationalFunctionInterpolation callInterpolationInLoop = new RationalFunctionInterpolation(initialStockPriceForCall, callOptionValue);

			interpolatedCallFunction = x -> callInterpolationInLoop.getValue(x);

			//this is what we want to plot, at every iteration of the while loop
			List<Plotable2D> plotables = Arrays.asList(
					new PlotableFunction2D(initialStockPriceForCall[0], initialStockPriceForCall[initialStockPriceForCall.length-1],
							numberOfPlottedValues, new Named<DoubleUnaryOperator>("Call function", interpolatedCallFunction), null));

			plotCallAndPut.update(plotables);//in this way, all the plots are in the same figure
			plotCallAndPut.setYAxisLabel("Call price for maturity " + formatterMaturity.format(maturity));
			Thread.sleep(1000);
			maturity += 0.1;
		}

	}

}

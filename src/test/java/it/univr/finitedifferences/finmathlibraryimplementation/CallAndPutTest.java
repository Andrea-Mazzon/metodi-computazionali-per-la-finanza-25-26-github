package it.univr.finitedifferences.finmathlibraryimplementation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

import net.finmath.finitedifference.models.FDMBlackScholesModel;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
import net.finmath.finitedifference.products.FDMEuropeanCallOption;
import net.finmath.finitedifference.products.FDMEuropeanPutOption;
import net.finmath.finitedifference.products.FiniteDifference1DProduct;
import net.finmath.functions.AnalyticFormulas;

/**
 * In this class we test the Finmath library implementation of finite difference methods to price call and put options.
 * The underlying is a Black-Scholes process. We print the prices for different initial values of the underlying and check,
 * via assertArrayEquals, if these are close to the analytic one.
 * 
 * @author Andrea Mazzon
 *
 */
public class CallAndPutTest {

	
	//option parameters
	final double optionMaturity = 1;
	final double optionStrike = 50;
	
	//model parameters
	final double initialValue = 50;
	final double riskFreeRate = 0.06;
	final double volatility = 0.4;
	
	//discretization parameters
	final int numTimesteps = 120;
	final int numSpacesteps = 120;
	final int numStandardDeviations = 5;
	final double theta = 0.5;

	final FiniteDifference1DModel model = new FDMBlackScholesModel(
			numTimesteps,//for the discretization of the time interval
			numSpacesteps,//for the discretization of the space domain
			numStandardDeviations,//this enters in the computation of the right and left end of the space domain
			optionStrike,//in the finmath library, there is written that this is the center of the space discretization, but this is not true: it has no effect
			theta,
			initialValue,//the center of the discretization in the class FDMThetaMethod is initialValue*exp(r*timeHorizon)
			riskFreeRate,
			volatility);
	
	final double tolerance = 0.01;
	
	/**
	 * This method prints the prices of a call option for different initial values of the underlying, and checks
	 * if these are close to the analytic ones. This is done via assertArrayEquals, which causes a failure if, at least
	 * for an index i, there is a difference between the i-th elements of the array of analytic prices and of the prices we
	 * compute higher than tolerance.
	 */
	private void testCall() {
		//this is used to print only the first digits of the values
		DecimalFormat formatterValue = new DecimalFormat("#.##");
		formatterValue.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		//the object representing the option (like the classes implementing AbstractMonteCarloProduct)
		final FiniteDifference1DProduct callOption = new FDMEuropeanCallOption(optionMaturity, optionStrike);
		/* 
		 * We don't really have to know that to call the method, but anyway: this method calls a method with the same name
		 * (but different argument list) defined in FiniteDifference1DModel. There, the boundary conditions are given according to
		 * the option: note that  FDMEuropeanCallOption has its own implementation of the methods giving the boundary
		 * conditions. 
		 */
		final double[][] returnedValuesForCallOption = callOption.getValue(0.0, model);
		
		//these are the space variables (i.e., the initial values) for which the prices are computed
		final double[] initialStockPricesForCall = returnedValuesForCallOption[0];
		
		//these are the corresponding prices
		final double[] callOptionValues = returnedValuesForCallOption[1];
		
		//calculation of the analytic prices
		final double[] analyticCallOptionValues = new double[callOptionValues.length];
		for (int i =0; i < analyticCallOptionValues.length; i++) {
			analyticCallOptionValues[i] = AnalyticFormulas.blackScholesOptionValue(initialStockPricesForCall[i], riskFreeRate,
					volatility, optionMaturity, optionStrike, true);
		}
		System.out.println("Initial values for call options:");
		System.out.println();
		//we use the Stream implementation to print all the elements of an array, stating how many digits after comma we want to print
		Arrays.stream(initialStockPricesForCall).forEach(element -> System.out.print(formatterValue.format(element) + " " ));
		System.out.println();
		
		System.out.println();
		System.out.println("Call option values:");
		System.out.println();
		//we use the Stream implementation to print all the elements of an array, stating how many digits after comma we want to print
		Arrays.stream(callOptionValues).forEach(element -> System.out.print(formatterValue.format(element) + " " ));
		System.out.println();
		
		System.out.println();
		System.out.println("Analytic call option values:");
		System.out.println();
		//we use the Stream implementation to print all the elements of an array, stating how many digits after comma we want to print
		Arrays.stream(analyticCallOptionValues).forEach(element -> System.out.print(formatterValue.format(element) + " " ));
		System.out.println();
	}
	
	/**
	 * This method prints the prices of a put option for different initial values of the underlying, and checks
	 * if these are close to the analytic ones. This is done via assertArrayEquals, which causes a failure if, at least
	 * for an index i, there is a difference between the i-th elements of the array of analytic prices and of the prices we
	 * compute higher than tolerance.
	 */
	private void testPut() {
		//this is used to print only the first digits of the values
		DecimalFormat formatterValue = new DecimalFormat("#.###");
		formatterValue.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		
		//the object representing the option (like the classes implementing AbstractMonteCarloProduct)
		final FiniteDifference1DProduct putOption = new FDMEuropeanPutOption(optionMaturity, optionStrike);
		
		/* 
		 * We don't really have to know that to call the method, but anyway: this method calls a method with the same name
		 * (but different argument list) defined in FiniteDifference1DModel. There, the boundary conditions are given according to
		 * the option: note that  FDMEuropeanCallOption has its own implementation of the methods giving the boundary
		 * conditions. 
		 */
		final double[][] returnedValuesForPutOption = putOption.getValue(0.0, model);
		
		//there are the space variables (i.e., the initial values) for which the prices are computed
		final double[] initialStockPricesForPut = returnedValuesForPutOption[0];
		
		//these are the corresponding prices
		final double[] putOptionValues = returnedValuesForPutOption[1];
		
		//calculation of the analytic prices
		final double[] analyticPutOptionValues = new double[putOptionValues.length];
		for (int i =0; i < analyticPutOptionValues.length; i++) {
			analyticPutOptionValues[i] = AnalyticFormulas.blackScholesOptionValue(initialStockPricesForPut[i], riskFreeRate,
					volatility, optionMaturity, optionStrike, false);
		}
		System.out.println("Initial values for put options:");
		System.out.println();
		//we use the Stream implementation to print all the elements of an array, stating how many digits after comma we want to print
		Arrays.stream(initialStockPricesForPut).forEach(element -> System.out.print(formatterValue.format(element) + " " ));
		System.out.println();
		System.out.println();
		System.out.println("Put option values:");
		System.out.println();
		
		//we use the Stream implementation to print all the elements of an array, stating how many digits after comma we want to print
		Arrays.stream(putOptionValues).forEach(element -> System.out.print(formatterValue.format(element) + " " ));	
		System.out.println();
		System.out.println();
		System.out.println("Analytic put option values:");
		System.out.println();
		
		//we use the Stream implementation to print all the elements of an array, stating how many digits after comma we want to print
		Arrays.stream(analyticPutOptionValues).forEach(element -> System.out.print(formatterValue.format(element) + " " ));	
	}
	
	public static void main(String[] strings) throws Exception {
		CallAndPutTest tester = new CallAndPutTest();
		tester.testCall();
		System.out.println();
		System.out.println();
		tester.testPut();
	}

}

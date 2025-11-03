package it.univr.finitedifferences.ourproducts;

import java.util.function.DoubleUnaryOperator;

import it.univr.analyticformulas.OurAnalyticFormulas;
import it.univr.montecarlo.ourproducts.BarrierOption;
import net.finmath.exception.CalculationException;
import net.finmath.finitedifference.models.FDMBlackScholesModel;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
import net.finmath.finitedifference.products.FiniteDifference1DProduct;
import net.finmath.interpolation.RationalFunctionInterpolation;
import net.finmath.interpolation.RationalFunctionInterpolation.ExtrapolationMethod;
import net.finmath.interpolation.RationalFunctionInterpolation.InterpolationMethod;
import net.finmath.montecarlo.BrownianMotion;
import net.finmath.montecarlo.BrownianMotionFromMersenneRandomNumbers;
import net.finmath.montecarlo.assetderivativevaluation.MonteCarloBlackScholesModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;

/**
 * In this class we test the Finmath library implementation of finite difference methods to price call and put options.
 * The underlying is a Black-Scholes process. We print the prices for different initial values of the underlying and check,
 * via assertArrayEquals, if these are close to the analytic one.
 * 
 * @author Andrea Mazzon
 *
 */
public class BarrierCallTest {


	public static void main(String[] args) throws CalculationException {

		//option parameters
		double upperBarrier = Long.MAX_VALUE;
		double lowerBarrier = 80;
		double maturity = 3.0;		
		double strike = 100;
		
		
		//model (i.e., underlying) parameters
		double initialValue = 100;
		double riskFreeRate = 0.0;
		double volatility = 0.3;
		
		double analyticalOptionValue = OurAnalyticFormulas.blackScholesDownAndOut(initialValue, riskFreeRate, volatility, maturity, strike, lowerBarrier);
		System.out.println("The analytic price is: " + analyticalOptionValue);
		
		
		// MONTE CARLO
		
		AbstractAssetMonteCarloProduct optionValueMCCalculator = new BarrierOption(maturity, strike, lowerBarrier, upperBarrier);
		
		//Monte Carlo time discretization parameters
		double initialTime = 0.0;
		double timeStep = 0.1;
		int numberOfTimeSteps = (int) (maturity/timeStep);

		TimeDiscretization times = new TimeDiscretizationFromArray(initialTime, numberOfTimeSteps, timeStep);

		//simulation parameters
		int numberOfPaths = 100000;
		int seed = 1897;

		
		BrownianMotion ourDriver = new BrownianMotionFromMersenneRandomNumbers(times, 1 /* numberOfFactors */, numberOfPaths, seed);
		

		//we construct an object of type MonteCarloBlackScholesModel: it represents the simulation of a Black-Scholes process
		MonteCarloBlackScholesModel blackScholesProcess = new MonteCarloBlackScholesModel(initialValue, riskFreeRate, volatility, ourDriver);

		double monteCarloValueOfTheOption = optionValueMCCalculator.getValue(blackScholesProcess);

		System.out.println("The Monte Carlo price is: " + monteCarloValueOfTheOption);
		
		
		// FINITE DIFFERENCES
		
		double theta = 0.5;

		//the object representing the option (like the classes implementing AbstractMonteCarloProduct)
		FiniteDifference1DProduct optionValueFDCalculator = new FDMBarrierCallOption(maturity, strike, lowerBarrier, upperBarrier, theta);
		
		//Finite difference discretization parameters
		int numTimesteps = 70;
		int numSpacesteps = 300;
		int numStandardDeviations = 15;
		
		FiniteDifference1DModel model = new FDMBlackScholesModel(
				numTimesteps,//for the discretization of the time interval
				numSpacesteps,//for the discretization of the space domain
				numStandardDeviations,//this enters in the computation of the right and left end of the space domain
				strike,//in the finmath library, there is written that this is the center of the space discretization, but this is not true: it has no effect
				theta,
				strike,//the center of the discretization in the class FDMThetaMethod is initialValue*exp(r*timeHorizon)
				riskFreeRate,
				volatility);

		/* 
		 * This method calls a method with the same name (but different argument list) defined in FiniteDifference1DModel.
		 * There, the boundary conditions are given according to the option: note that  FDMEuropeanCallOption has its own
		 * implementation of the methods giving the boundary conditions. 
		 */
		double[][] returnedValues = optionValueFDCalculator.getValue(0.0, model);

		//these are the corresponding prices
		double[] initialValues = returnedValues[0];
		double[] optionValues = returnedValues[1];
		
		/*
		 * We interpolate because it can be that the initial value we care about is not part of the space discretization.
		 * That is, we want to find a continuous function f such that f(x_i)=y_i for all i, where x_i are the initial values
		 * and y_i the prices. We do that with the help of the Finmath library itself.
		 */
		RationalFunctionInterpolation callInterpolation = new RationalFunctionInterpolation(initialValues, optionValues,
				InterpolationMethod.CUBIC_SPLINE, ExtrapolationMethod.DEFAULT);

		//in this way, we can define the interpolating DoubleUnaryOperator
		DoubleUnaryOperator interpolatedCallFunction = x -> callInterpolation.getValue(x);

		double finiteDifferenceValueOfTheOption = interpolatedCallFunction.applyAsDouble(initialValue);		


		System.out.println("The Finite difference price is: " + finiteDifferenceValueOfTheOption);
	}

}

package it.univr.trees.products;


import java.util.function.DoubleUnaryOperator;

import it.univr.trees.models.BinomialModel;
import it.univr.usefulmethodsarrays.UsefulMethodsArrays;

/**
 * This class implements the valuation of an American option, with given payoff at maturity. This is a path
 * dependent option which can be exercised by the holder at any time. The option value is computed via an
 * approximation of a continuous time process by a Binomial model, represented by an object of type
 * BinomialModel. In this way, it is possible to conveniently apply a backward evaluation method.
 * 
 * @author Andrea Mazzon
 *
 */
public class AmericanOption {

	private double maturity;
	private DoubleUnaryOperator payoffFunction;

	/**
	 * It constructs an object which represents the implementation of the American option.
	 * @param maturity, the maturity of the option
	 * @param payoffFunction, the funtion which identifies the payoff. The payoff is f(S_T) for payoffFunction
	 * 			f and underlying value S_T at maturity. The payoffFunction is represented by a DoubleUnaryOperator
	 */
	public AmericanOption(double maturity, DoubleUnaryOperator payoffFunction) {
		this.maturity = maturity;
		this.payoffFunction = payoffFunction;
	}

	/**
	 * It returns the discounted value of the option written on the continuous time model approximated by
	 * the object of type BinomialModel given in input. At any node at time t_{i}, the value
	 * of the option is computed as the maximum between the payoff function evaluated at that node and the
	 * conditional expectation at that node of the values of the option at time t_{i+1}.
	 * This is done by going backward
	 * 
	 * @param approximatingBinomialModel, the underlying
	 * @return the value of the option written on the underlying
	 */
	public double getValue(BinomialModel approximatingBinomialModel) {

		//values of the option at maturity. Then we go backward. 
		double[] optionValues = approximatingBinomialModel.getTransformedValuesAtGivenTime(maturity, payoffFunction);

		int numberOfTimeSteps = (int) Math.round(maturity/approximatingBinomialModel.getTimeStep());

		/*
		 * We go backward. For any timeIndex and any node, first we compute the conditional expectation of the option value
		 * at timeIndex + 1, and then we compute the maximum between this value and the payoff function evaluated at the node
		 */
		for (int timeIndex = numberOfTimeSteps - 1; timeIndex >= 0; timeIndex--) {
			//delegation to approximatingTreeModel!
			double[] conditionalExpectation = approximatingBinomialModel.getConditionalExpectation(optionValues);
			double[] payoffAtCurrentTime = approximatingBinomialModel.getTransformedValuesAtGivenTimeIndex(timeIndex, payoffFunction);
			optionValues = UsefulMethodsArrays.getMaxValuesBetweenTwoArrays(conditionalExpectation,payoffAtCurrentTime);   
		}
		return optionValues[0];
	}

}

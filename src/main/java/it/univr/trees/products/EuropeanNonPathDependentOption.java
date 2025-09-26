package it.univr.trees.products;

import java.util.function.DoubleUnaryOperator;

import it.univr.trees.models.BinomialModel;
import it.univr.usefulmethodsarrays.UsefulMethodsArrays;



/**
 * This class implements the valuation of an European option non path dependent (that is, which pays only
 * according to the value of the underlying at maturity) via an approximation of a Black-Scholes process
 * with a Binomial model.
 * 
 * @author Andrea Mazzon
 *
 */
public class EuropeanNonPathDependentOption {

	private double maturity;
	private DoubleUnaryOperator payoffFunction;

	/**
	 * It constructs an object which represents the implementation of the European, non path dependent option.
	 * @param maturity, the maturity of the option
	 * @param payoffFunction, the funtion which identifies the payoff. The payoff is f(S_T) for payoffFunction
	 * f and underlying value S_T at maturity. The payoffFunction is represented by a DoubleUnaryOperator.
	 */
	public EuropeanNonPathDependentOption(double maturity, DoubleUnaryOperator payoffFunction) {
		this.maturity = maturity;
		this.payoffFunction = payoffFunction;
	}

	
	/**
	 * It returns the discounted value of the option written on the Black-Scholes model approximated by
	 * the object of type BinomialModel given in input. The value of the option is computed as the discounted
	 * expectation of the possible values at maturity. This expectation is computed as the scalar product of the vector
	 * of the possible payoff values and the one of their probabilities. 
	 * 
	 * @param approximatingBinomialModel, the underlying
	 * @return the value of the option written on the underlying
	 */
	public double getValue(BinomialModel approximatingBinomialModel) {
		//the values of the payoffs..
		double[] payoffValues = approximatingBinomialModel.getTransformedValuesAtGivenTime(maturity, payoffFunction);
		//..and the corresponding probabilities
		double[] valuesProbabailities = approximatingBinomialModel.getValuesProbabilitiesAtGivenTime(maturity);
		//then we compute the weighted sum..
		double nonDiscountedValue = UsefulMethodsArrays.getScalarProductTwoArrays(payoffValues, valuesProbabailities);
		//..and discount
		int numberOfTimeSteps = approximatingBinomialModel.getNumberOfTimes()-1; //it is n
		double discountFactor = Math.pow(approximatingBinomialModel.getRiskFreeFactor()+1,-numberOfTimeSteps);
		return discountFactor * nonDiscountedValue;
	}
}

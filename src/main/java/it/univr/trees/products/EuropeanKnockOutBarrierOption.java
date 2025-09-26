package it.univr.trees.products;

import java.util.function.DoubleUnaryOperator;

import it.univr.trees.models.BinomialModel;
import it.univr.usefulmethodsarrays.UsefulMethodsArrays;

/**
 * This class implements the valuation of an European option with double or single barrier. This is a path
 * dependent option which pays the payoff only if the value of the underlying stays in an interval
 * [lowerBarrier, upperBarrier] for the whole path. We have single barrier if we only have lowerBarrier
 * or only have upperBarrier. The value is computed via an approximation of a Black-Scholes process
 * with a Binomial model.
 * 
 * @author Andrea Mazzon
 *
 */
public class EuropeanKnockOutBarrierOption {

	private double maturity;
	private DoubleUnaryOperator payoffFunction;
	private DoubleUnaryOperator barrierFunction;//this is defined in the costructor.
	

	/**
	 * It constructs an object which represents the implementation of the European option with barriers.
	 * @param maturity, the maturity of the option
	 * @param payoffFunction, the funtion which identifies the payoff. The payoff is f(S_T) for payoffFunction
	 * f and underlying value S_T at maturity. The payoffFunction is represented by a DoubleUnaryOperator
	 * @param lowerBarrier, the lower barrier: if the underlying goes below this value in its path until maturity,
	 * we get no payoff
	 * * @param upperBarrier, the upper barrier: if the underlying goes above this value in its path until maturity,
	 * we get no payoff
	 */
	public EuropeanKnockOutBarrierOption(double maturity, DoubleUnaryOperator payoffFunction, double lowerBarrier,
			double upperBarrier) {
		this.maturity = maturity;
		this.payoffFunction = payoffFunction;
		//ternary operator!
		barrierFunction = (x) -> (x>lowerBarrier & x<upperBarrier ? 1 : 0);
	}
	
	/**
	 * It returns the discounted value of the option written on the Black-Scholes model approximated by
	 * the object of type BinomialModel given in input. The value of the option is computed as the discounted expectation
	 * of the possible values at maturity. This expectation is computed by going backward from maturity to initial time and
	 * computing the iterative conditional expectation, see slides. The conditional expectations are multiplied at every
	 * time with a vector whose elements are 1 if the value of the underlying approximating Binomial model is within the
	 * interval [lowerBarrier, upperBarrier] and 0 otherwise.
	 * 
	 * 
	 * @param approximatingBinomialModel, the underlying
	 * @return the value of the option written on the underlying
	 */
	public double getValue(BinomialModel approximatingBinomialModel) {
		
		//the values of the option at maturity if this is not a barrier option
		double[] optionValuesWithoutBarrier = approximatingBinomialModel.getTransformedValuesAtGivenTime(maturity, payoffFunction);
		
		//the values of the underlyings: we need them to check if they are inside the interval		
		double[] underlyingValues = approximatingBinomialModel.getValuesAtGivenTime(maturity);
		
		/*
		 * Vector whose elements are 1 if the value of the underlying approximating Binomial model is within the interval
		 * [lowerBarrier, upperBarrier] and 0 otherwise
		 */
		double[] areTheUnderlyingValuesInsideInterval = UsefulMethodsArrays.applyFunctionToArray(underlyingValues, barrierFunction);
		 
		//the values of the option at maturity, considering now the barrier
		double[] optionValues = UsefulMethodsArrays.multArrays(optionValuesWithoutBarrier, areTheUnderlyingValuesInsideInterval);

		int numberOfTimes = (int) Math.round(maturity/approximatingBinomialModel.getTimeStep());
		for (int timeIndex = numberOfTimes - 1; timeIndex >= 0; timeIndex--) {
			//now we repeat the same thing as above at any time.
			
			//the values of the option not considering the barrier
        	double[] conditionalExpectation = approximatingBinomialModel.getConditionalExpectation(optionValues);
    		//the values of the underlyings: we need them to check if they are inside the interval
        	underlyingValues = approximatingBinomialModel.getValuesAtGivenTimeIndex(timeIndex);
        	/*
    		 * vector whose elements are 1 if the value of the underlying approximating Binomial model is within the interval
    		 * [lowerBarrier, upperBarrier] and 0 otherwise
    		 */
        	areTheUnderlyingValuesInsideInterval = UsefulMethodsArrays.applyFunctionToArray(underlyingValues, barrierFunction);
        	
    		//the values of the option, considering now the barrier
        	double[] transformedConditionalExpectation = UsefulMethodsArrays.multArrays(conditionalExpectation, areTheUnderlyingValuesInsideInterval);
        	optionValues = transformedConditionalExpectation;  

        }
		return optionValues[0];
	}
}

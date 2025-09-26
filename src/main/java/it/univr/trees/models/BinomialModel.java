package it.univr.trees.models;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

import it.univr.usefulmethodsarrays.UsefulMethodsArrays;

/**
 * Abstract base class representing a general Binomial model used to approximate
 * the Black–Scholes model via discrete time steps.
 * 
 * The philosophy behind this class is to provide a reusable implementation of the
 * core structure of a Binomial model:
 * 
 *   - it initializes the parameters of the model (up/down factors, risk-free factor,
 *       risk-neutral probabilities) from the input data;
 *   - it generates the possible values of the process and their probabilities
 *       at each time step of the discretization;
 *   - it gives public methods to get values, probabilities and conditional
 *       expectations at every time index / time.
 *       
 * Concrete models (such as Cox–Ross–Rubinstein, Jarrow–Rudd, Leisen–Reimer)
 * extend this class and implement the computeUpDownFactors method.
 * In particular, the up and down factors are computed in the derived classes
 * according to the parameters of the underlying Black–Scholes process
 * (interest rate, volatility, maturity).
 * 
 * The public methods of this class will be used in classes for the valuation of derivatives,
 * especially European options and barrier options, where the ability to easily compute conditional
 * expectations is essential.
 */
public abstract class BinomialModel {
	
	//parameters describing the model
	
	//this is given in the constructor
	private double initialPrice;
	
	//the following parameters are not given in the constructor, but computed from volatility and risk free rate r
	private double upFactor;
	private double downFactor;
	private double riskFreeFactor;//this is rho_n in the notes, not to be confused with the risk free rate r 
	
	//these are determined from the last three values above, in order to have an arbitrage free market
	private double riskNeutralProbabilityUp;
	private double riskNeutralProbabilityDown;
	
	//parameters of the time discretization
	private double timeStep;
	private int numberOfTimes;

	//these fields will be initialized and set in private methods. For now their values is "null".
	private double[][] valuesProbabilities;
	private double[][] values;
	
	
	/*
	 * Below, we have an example of overloaded constructors: we two constructors, the first one accepts as
	 * last argument a double indicating the time step, and computed the number of times accordingly, whereas
	 * the second one does the inverse. In this way, the user can decide what to give.
	 */
	
	public BinomialModel(double initialPrice, double riskFreeRate, double volatility, 
			double lastTime, double timeStep) {
		this.initialPrice = initialPrice;
		this.timeStep = timeStep;
		
		//computed from timeStep
		numberOfTimes = (int) (Math.round(lastTime/timeStep) + 1);//the number of times comes from the time step
		/*
		 * Important: u_n and d_n are computed in a specific way according to the specific model. The specific model
		 * will be represented by a class inheriting from this one. In general, we assume they can be function of r,
		 * sigma and Delta_n=T/n. In cases when they depend on more parameters, these parameters will be given in the
		 * constructors of those classes, see for example LeisenReimerModel.
		 */
		upFactor = computeUpDownFactors(riskFreeRate, volatility, timeStep)[0];
		downFactor = computeUpDownFactors(riskFreeRate, volatility, timeStep)[1];
		riskFreeFactor = Math.exp(riskFreeRate*timeStep)-1;//it is rho_n
		
		//risk neutral probabilities 
		riskNeutralProbabilityUp = (1 + riskFreeFactor - downFactor) / (upFactor - downFactor);
		riskNeutralProbabilityDown = 1 - riskNeutralProbabilityUp;
	}
	
	
	
	public BinomialModel(double initialPrice, double riskFreeRate, double volatility, 
			double lastTime, int numberOfTimes) {
		this.initialPrice = initialPrice;
		this.numberOfTimes = numberOfTimes;
		timeStep = lastTime/(numberOfTimes-1);//the time step comes from the number of times
		/*
		 * Important: u_n and d_n are computed in a specific way according to the specific model. The specific model
		 * will be represented by a class inheriting from this one. In general, we assume they can be function of r,
		 * sigma and Delta_n=T/n. In cases when they depend on more parameters, these parameters will be given in the
		 * constructors of those classes, see for example LeisenReimerModel.
		 */
		upFactor = computeUpDownFactors(riskFreeRate, volatility, timeStep)[0];
		downFactor = computeUpDownFactors(riskFreeRate, volatility, timeStep)[1];
		riskFreeFactor = Math.exp(riskFreeRate*timeStep)-1;//it is rho_n
		
		
		//risk neutral probabilities 
		riskNeutralProbabilityUp = (1 + riskFreeFactor - downFactor) / (upFactor - downFactor);
		riskNeutralProbabilityDown = 1 - riskNeutralProbabilityUp;
	}
	
	
	protected abstract double[] computeUpDownFactors(double riskFreeRate, double volatility, double timeStep);

	
	/*
	 * This method is private! This is our inner implementation, behind the scenes. We don't want an user of our
	 * class to access it. The method sets values to be a matrix whose row k (starting from 0, as it happens in Java)
	 * represents the possible values of the binomial model at time t_k. However, the last values of the row
	 * are zero, because at time t_k the binomial model can only take k+1 values. Note that this could cause
	 * confusion if somebody is directly aware of this method.
	 */
	private void generateValues() {
		values = new double[numberOfTimes][numberOfTimes];
		values[0][0] = initialPrice;
		int numberOfDowns;//it will be updated in the for loop
		for (int numberOfMovements = 1; numberOfMovements < numberOfTimes; numberOfMovements++) {
			for (int numberOfUps = 0; numberOfUps <= numberOfMovements; numberOfUps++) {
				numberOfDowns=numberOfMovements-numberOfUps;
				/*
				 * Value of the binomial model when it went numberOfUps times up and numberOfDowns times down.
				 * Note that this is stored in position numberOfDowns! So the first position has all ups and so on
				 */
				values[numberOfMovements][numberOfDowns] = values[0][0] * Math.pow(upFactor, numberOfUps)*
						Math.pow(downFactor, numberOfDowns);
			}
		}
	}

	/*
	 * This method is private! This is our inner implementation, behind the scenes. We don't want an user of our
	 * class to access it. The method sets valuesProbabilities to be a matrix whose row k represents the probabilities
	 * of the corresponding values of the binomial model at time t_k. However, the last values of the row are
	 * zero, because at time t_k the binomial model can only take k+1 values. Note that this could cause confusion
	 * if somebody is directly aware of this method.
	 */
	private void generateValuesProbabilities() {
		valuesProbabilities = new double[numberOfTimes][numberOfTimes];
		valuesProbabilities[0][0]=1;//the first value is deterministic
		int numberOfDowns;//it will be updated in the for loop
		for (int numberOfMovements = 1; numberOfMovements < numberOfTimes; numberOfMovements++) {
			/*
			 * Here we have to take care of the computation of the binomial coefficients. 
			 * We are at time n and start the "internal" for loop with the case when we have k=0 ups.
			 * So we first have binomialCoefficient(n,k)=n!/(k!(n-k)!)=1 
			 */
			double binomialCoefficient = 1;
			for (int numberOfUps = 0; numberOfUps <= numberOfMovements; numberOfUps++) {
				numberOfDowns=numberOfMovements-numberOfUps;
				/*
				 * Probability of having B(0)u^numberOfUps*d^numberOfDowns.
				 * Note that this is stored in position numberOfDowns! So the first position has all ups and so on
				 */
				valuesProbabilities[numberOfMovements][numberOfDowns]
						= binomialCoefficient*Math.pow(riskNeutralProbabilityUp, numberOfUps)
						* Math.pow(riskNeutralProbabilityDown, numberOfDowns);
				/*
				 * Here we update the value of the binomial coeffeicient computing the one
				 * that we will use next, i.e., when we will have one more up: so, if j is
				 * the actual number of ups, we have to compute 
				 * binomialCoefficient(k,j+1)=k!/((j+1)!(k-j-1)!)=k!/(j!(k-j)!)*(k-j)/(j+1).
				 * Since k!/(j!(k-j)!) is the last computed value, we multiply by (k-j) 
				 * (so, by numberOfDowns) and divide by j+1, so, by the current number of ups plus 1.
				 */
				binomialCoefficient=binomialCoefficient * (numberOfDowns)/(numberOfUps+1);
			}
		}
	}
	
	/**
	 * It returns all the possible values of the binomial model at the given time index. The element in
	 * position i is the one where the underlying has gone down i times.
	 * @param timeIndex, the given time index
	 * @return an array of doubles representing all the possible values of the binomial model at timeIndex.
	 * 		   The value in position i is B(0)*u^(timeIndex-i)*d^i
	 */
	public double[] getValuesAtGivenTimeIndex(int timeIndex) {
		/*
		 * Pay attention: the method generateValues() initializes the array values and sets it. This is
		 * of course needed if we want to get those values. However, we want to do that only once!
		 * So we check if values is null (this means "not yet initialized") and call the method only
		 * in this case. 
		 */
		if (values == null) {
			generateValues();
		}	
		/*
		 * We only return the first timeIndex entries! The others are zero, because the process can take
		 * only timeIndex values at time index timeIndex.
		 */
		return Arrays.copyOfRange(values[timeIndex], 0, timeIndex+1);
	}
	
	
	/**
	 * It returns all the possible values of the binomial model at the given time. The element in
	 * position i is the one where the underlying has gone down i times.
	 * @param time, the given time 
	 * @return an array of doubles representing all the possible values of the binomial model at the given time.
	 * 		   The value in position i is B(0)*u^(Math.round(time/timeStep)-i)*d^i
	 */
	public double[] getValuesAtGivenTime(double time) {
		int timeIndex = (int) Math.round(time/timeStep);

		//we return directly what we get from getValuesAtGivenTimeIndex(timeIndex)
		return getValuesAtGivenTimeIndex(timeIndex);
	}
		
	
	/**
	 * It returns an array whose elements are a function of all the possible values of the binomial model at
	 * the given time index. The element in position i is the function of the value of the underlying in the
	 * case when it has gone down i times.
	 * @param timeIndex, the given time index
	 * @param DoubleUnaryOperator transformFunction, the function
	 * @return an array of doubles representing a function of all the possible values of the binomial model at timeIndex.
	 * 		   The value in position i is transformFunction(B(0)*u^(timeIndex-i)*d^i)
	 */
	public double[] getTransformedValuesAtGivenTimeIndex(int timeIndex, DoubleUnaryOperator transformFunction) {
		double[] valuesAtGivenTimeIndex = getValuesAtGivenTimeIndex(timeIndex);
		/*
		 * We return the function applied to this array, that is, what we get from
		 * UsefulMethodsArrays.applyFunctionToArray(valuesAtGivenTimeIndex, transformFunction)
		 */
		return UsefulMethodsArrays.applyFunctionToArray(valuesAtGivenTimeIndex, transformFunction);
	}

	
	/**
	 * It returns an array whose elements are a function of all the possible values of the binomial model at
	 * the given time. The element in position i is the function of the value of the underlying in the
	 * case when it has gone down i times.
	 * @param time, the given time 
	 * @param DoubleUnaryOperator transformFunction, the function
	 * @return an array of doubles representing a function all the possible values of the binomial model at the
	 * 		   given time. The value in position i is transformFunction(B(0)*u^(Math.round(time/timeStep)-i)*d^i)
	 */
	public double[] getTransformedValuesAtGivenTime(double time, DoubleUnaryOperator transformFunction) {
		int timeIndex = (int) Math.round(time/timeStep);
		return getTransformedValuesAtGivenTimeIndex(timeIndex, transformFunction);
	}

	
	
	/**
	 * It returns the probabilities of all the possible values of the binomial model at the given time index.
	 * The element in position i is the probability of the value where the underlying has gone down i times.
	 * @param timeIndex, the given time index
	 * @return an array of doubles representing the probabilities of all the possible values of the binomial
	 * 		   model at timeIndex. The value in position i is the probability of B(0)*u^(timeIndex-i)*d^i
	 */
	public double[] getValuesProbabilitiesAtGivenTimeIndex(int timeIndex) {
		/*
		 * Pay attention: the method generateValues() initializes the array valuesProbabilities and sets it.
		 * This is of course needed if we want to get those values. However, we want to do that only once!
		 * So we check if valuesProbabilities is null (this means "not yet initialized") and call the method only
		 * in this case. 
		 */
		if (valuesProbabilities == null) {
			generateValuesProbabilities();
		}
		/*
		 * We only return the first timeIndex entries! The others are zero, because the process can take
		 * only timeIndex values at time index timeIndex.
		 */
		return Arrays.copyOfRange(valuesProbabilities[timeIndex], 0, timeIndex + 1);
	}
	
	
	
	
	/**
	 * It returns the probabilities of all the possible values of the binomial model at the given time.
	 * The element in position i is the probability of the value where the underlying has gone down i times.
	 * @param timeIndex, the given time index
	 * @return an array of doubles representing the probabilities of all the possible values of the binomial
	 * 		   model at timeIndex. The value in position i is the probability of B(0)*u^(Math.round(time/timeStep)-i)*d^i
	 */
	public double[] getValuesProbabilitiesAtGivenTime(double time) {
		int timeIndex = (int) Math.round(time/timeStep);
		return getValuesProbabilitiesAtGivenTimeIndex(timeIndex);
	}

	
	/**
	 * It returns an array representing the discounted conditional expectations of the values of 
	 * (possibly a function of) a binomial model. Here, the values are given as an argument of the method.
	 * 
	 * @param binomialValues, values of (possibly a function of) a binomial model
	 * @return the array of the discounted conditional expectations of binomialValues at the previous time in the tree.  
	 * 			The i-th element is the conditional expectation computed in the case when the underlying
	 * 			has gone down i times.
	 */
	public double[] getConditionalExpectation(double[] binomialValues) {
		
		
		int numberOfConditionalExpectations = binomialValues.length-1;
		double[] conditionalExpectation = new double[numberOfConditionalExpectations];
		for (int i = 0; i < numberOfConditionalExpectations; i++) {
			/*
			 * computation of the conditional probability at the state with i down. Note that the i-th element
			 * of binomialValues has gone up, because the number of down is still i. 
			 */
			conditionalExpectation[i] = (binomialValues[i]*riskNeutralProbabilityUp + binomialValues[i + 1]*riskNeutralProbabilityDown)/(1+riskFreeFactor);
		}
		return conditionalExpectation;
	}
	
	
	
	/**
	 * It returns the array whose two elements are the probability of an up movement and the probability
	 * of a down movement, respectively.
	 * @return the array whose two elements are the probability of an up movement and the probability
	 * of a down movement, respectively.
	 */
	public double[] getUpAndDownProbabilities() {
		double[] probabilities = {riskNeutralProbabilityUp,riskNeutralProbabilityDown};
		return probabilities;
	}
	
	
	
	
	/*
	 * Getters for the parameters of the binomial model. Some of them are used in the derived classes:
	 * in this way, we can set them private here (we prefer, because in this way they cannot be modified,
	 * note that there are no setters indeed)
	 */
	/**
	 * It returns the initial price of the approximated Black-Scholes model
	 * @return the initial price of the approximated Black-Scholes model
	 */
	public double getInitialPrice() {
		return initialPrice;
	}

	/**
	 * It returns the time step of the time discretization with which we approximate Black-Scholes model
	 * @return the time step of the time discretization with which we approximate Black-Scholes model
	 */
	public double getTimeStep() {
		return timeStep;
	}

	/**
	 * It returns the number of times of the time discretization with which we approximate Black-Scholes model
	 * @return the number of times of the time discretization with which we approximate Black-Scholes model
	 */
	public int getNumberOfTimes() {
		return numberOfTimes;
	}	

	/**
	 * It returns the risk free rate of the approximated Black-Scholes model
	 * @return the risk free rate of the approximated Black-Scholes model
	 */
	public double getRiskFreeFactor() {
		return riskFreeFactor;
	}
	
	/**
	 * It returns the up factor u_n
	 * @return u_n
	 */
	public double getUpFactor() {
		return upFactor;
	}
	
	/**
	 * It returns the up factor d_n
	 * @return d_n
	 */
	public double getDownFactor() {
		return downFactor;
	}

}

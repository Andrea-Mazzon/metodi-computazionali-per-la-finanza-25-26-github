package it.univr.trees.models;

/**
 * This class represents the approximation of a Black-Scholes model via the Leisen-Reimer model.
 * It extends BinomialModel. The only method that is implemented here computes the values
 * of the up and down movements of the Binomial model.
 * 
 * @author Andrea Mazzon
 *
 */
public class LeisenReimerModel extends BinomialModel {
	
	private double strike;
	private double maturity;
	
	/**
	 * It constructs an object which represents the approximation of a Black-Scholes model via the Leisen-Reimer
	 * model.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param timeStep, the length t_k-t_{k-1} of the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 * @param strike, the strike of the option for which the model is used. Indeed, it is mostly used for valuing
	 * call and put options.
	 */
	public LeisenReimerModel(double spotPrice, double riskFreeRate, double volatility, 
			double lastTime, double timeStep, double strike) {
		super(spotPrice, riskFreeRate, volatility, lastTime, timeStep);
		this.strike = strike;
		maturity = lastTime;
	}

	/**
	 * It constructs an object which represents the approximation of a Black-Scholes model via the Leisen-Reimer
	 * model.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param numberOfTimes, the number of times in the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 * @param strike, the strike of the option for which the model is used. Indeed, it is mostly used for valuing
	 * call and put options.
	 */
	public LeisenReimerModel(double spotPrice, double riskFreeRate, double volatility, 
			double lastTime, int numberOfTimes, double strike) {
		super(spotPrice, riskFreeRate, volatility, lastTime, numberOfTimes);
		this.strike = strike;
		maturity = lastTime;
	}

	/**
	 * It computes and returns the up and down movements of the Binomial model for the Leisen-Reimer model
	 * @return an arrays of two elements: the first is the up movement for the Leisen-Reimer model,
	 * the second the down one.
	 */
	@Override
	protected double[] computeUpDownFactors(double riskFreeRate, double volatility, double timeStep) {
		
		/*
		 * We use the getters of the parent class here, because the fields are private: we prefer to let them
		 * be private and not protected first of all because it is always a good practice, and also because in this way
		 * is is not possible to manipulate them.
		 */
		double initialPrice = getInitialPrice();
		int numberOfTimes = getNumberOfTimes();
		
		//see slides. Pure computations here. 
		double d1 = (Math.log(initialPrice/strike)+(riskFreeRate+Math.pow(volatility, 2)/2)*maturity)/(volatility*Math.sqrt(maturity));
		double d2 = d1 - volatility*Math.sqrt(maturity);

		double bond = Math.exp(riskFreeRate * timeStep);
		int numberOfTimeSteps = numberOfTimes -1;
		double term1 = Math.pow((d1/(numberOfTimeSteps+1/3.0//note the 3.0 here and below! What would we get if we used 3?
				+0.1/(numberOfTimeSteps+1))),2)*(numberOfTimeSteps+1/6.0);
		double qprime = 0.5+Math.signum(d1)*0.5*Math.sqrt(1-Math.exp(-term1));
		double term2 = Math.pow((d2/(numberOfTimeSteps+1/3.0+0.1/(numberOfTimeSteps+1))),2)*(numberOfTimeSteps+1/6.0);
		double upProbability = 0.5+Math.signum(d2)*0.5*Math.sqrt(1-Math.exp(-term2));
		double upFactor = bond*qprime/upProbability;
		double downFactor = bond*((1-qprime)/(1-upProbability));
		double[] upAndDownFactors = {upFactor, downFactor};
		return upAndDownFactors;
	}
	//NOTE: this class contains a mistake, which makes the results we can see in the test classes not reliable: let's spot it together!
}

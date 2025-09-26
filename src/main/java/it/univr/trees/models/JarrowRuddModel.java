package it.univr.trees.models;

/**
 * This class represents the approximation of a Black-Scholes model via the Jarrow-Rudd model.
 * It extends BinomialModel. The only method that is implemented here computes the values
 * of the up and down movements of the Binomial model.
 * 
 * @author Andrea Mazzon
 *
 */
public class JarrowRuddModel extends BinomialModel {
	
	/**
	 * It constructs an object which represents the approximation of a Black-Scholes model via the Jarrow-Rudd model.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param timeStep, the length t_k-t_{k-1} of the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 */
	public JarrowRuddModel(double spotPrice, double riskFreeRate, double volatility, 
			double lastTime, double timeStep) {
		super(spotPrice, riskFreeRate, volatility, lastTime, timeStep);
	}
	
	/**
	 * It constructs an object which represents the approximation of a Black-Scholes model via the Jarrow-Rudd model.
	 * 
	 * @param initialPrice, the initial price of the asset modeled by the process
	 * @param riskFreeRate, the number r such that the value of a risk-free bond at time T is e^(rT)
	 * @param volatility, the log-volatility of the Black-Scholes model
	 * @param lastTime, the last time T in the time discretization 0=t_0<t_1<..<t_n=T
	 * @param numberOfTimes, the number of times in the equally spaced time steps that we take for the approximating
	 * time discretization 0=t_0<t_1<..<t_n=T
	 */
	public JarrowRuddModel(double spotPrice, double riskFreeRate, double volatility, 
			double lastTime, int numberOfTimes) {
		super(spotPrice, riskFreeRate, volatility, lastTime, numberOfTimes);
	}
	
	/**
	 * It computes and returns the up and down movements of the Binomial model for the Jarrow-Rudd model
	 * @return an arrays of two elements: the first is the up movement for the Jarrow-Rudd model,
	 * the second the down one.
	 */
	@Override
	protected double[] computeUpDownFactors(double riskFreeRate, double volatility, double timeStep) {
		//see slides
	    double upFactor = Math.exp((riskFreeRate-(Math.pow(volatility, 2)/2))*timeStep+volatility*Math.sqrt(timeStep));
	    double downFactor = Math.exp((riskFreeRate-(Math.pow(volatility, 2)/2))*timeStep-volatility*Math.sqrt(timeStep));
        double[] upAndDownFactors = {upFactor, downFactor};
        return upAndDownFactors;
	}
}

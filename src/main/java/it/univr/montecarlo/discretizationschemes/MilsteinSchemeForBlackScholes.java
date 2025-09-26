package it.univr.montecarlo.discretizationschemes;

import net.finmath.montecarlo.BrownianMotion;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;

/**
 * This class simulates the trajectories of a geometric Brownian motion (i.e.,
 * Black-Scholes model) by using an Milstein scheme. This class extends
 * AbstractSimulation by giving the implementation of getDrift and getDiffusion.
 *
 * @author Andrea Mazzon
 */
public class MilsteinSchemeForBlackScholes extends AbstractProcessSimulation {

	private final double muDrift;// mu
	private final double sigmaVolatility;// sigma

	/**
	 * It constructs an object of type MilsteinSchemeForBlackScholes, which provides the Milstein scheme for the
	 * Black-Scholes model.
	 * 
	 * @param sigmaVolatility: the volatility sigma in dX_t = mu X_t dt + sigma X_t dW_t
	 * @param muDrift: the drift mu in dX_t = mu X_t dt + sigma X_t dW_t
	 * @param initialValue: X_0
	 * @param numberOfSimulations: the number of simulated trajectories 
	 * @param seed: the seed to simulate the trajectories of the process
	 * @param times: the time discretization where we simulate the process
	 */
	public MilsteinSchemeForBlackScholes(double sigmaVolatility, double muDrift,
			double initialValue,  int numberOfSimulations, int seed, TimeDiscretization times) {
		super(initialValue, numberOfSimulations, seed, times);
		this.muDrift = muDrift;
		this.sigmaVolatility = sigmaVolatility;
		this.transform = (x -> x);
		this.inverseTransform = (x -> x);
	}

	/*
	 * It gets and returns the drift of a geometric Brownian motion computed with
	 * the Milstein scheme. That is, it returns mu*S_{t_{k-1}}*(T_k-t_{k-1}). Here
	 * S_{t_{k-1}} is given as an argument, called lastRealization.
	 */
	@Override
	protected RandomVariable getDrift(RandomVariable lastRealization, int timeIndex) {
		TimeDiscretization times = getTimeDiscretization();
		final double timeStep = times.getTimeStep(timeIndex - 1);
		return lastRealization.mult(muDrift).mult(timeStep);
	}

	
	// It gets and returns the diffusion of a geometric Brownian motion computed with the Milstein scheme.
	@Override
	protected RandomVariable getDiffusion(RandomVariable lastRealization, int timeIndex) {

		BrownianMotion brownianMotion = getStochasticDriver();

		TimeDiscretization times = getTimeDiscretization();
		final double timeStep = times.getTimeStep(timeIndex - 1);

		final RandomVariable brownianIncrement = brownianMotion.getBrownianIncrement(timeIndex - 1, 0);

		final RandomVariable linearTerm = lastRealization.mult(sigmaVolatility).mult(brownianIncrement);

		final RandomVariable adjustment = brownianIncrement.mult(brownianIncrement).sub(timeStep).mult(lastRealization)
				.mult(sigmaVolatility * sigmaVolatility * 0.5);

		return linearTerm.add(adjustment);
	}
}
package it.univr.montecarlo.ourproducts;

import java.util.function.DoubleUnaryOperator;

import net.finmath.exception.CalculationException;
import net.finmath.montecarlo.RandomVariableFromDoubleArray;
import net.finmath.montecarlo.assetderivativevaluation.AssetModelMonteCarloSimulationModel;
import net.finmath.montecarlo.assetderivativevaluation.products.AbstractAssetMonteCarloProduct;
import net.finmath.stochastic.RandomVariable;
import net.finmath.time.TimeDiscretization;

/**
 * This class implements the valuation of a European option on a single asset.
 * In particular, the option has payoff
 * (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]} for a given underlying X_t. 
 * Here K is the strike of the option, T its maturity, B_L and B_U the lower and upper barrier, respectively.
 * 
 * Note that this class extends AbstractAssetMonteCarloProduct. For this reason, we have to implement the method
 * getValue(double evaluationTime, AssetModelMonteCarloSimulationModel model).
 * 
 * We do that exploiting the methods of the interface AssetModelMonteCarloSimulationModel.
 *
 * @author Andrea Mazzon
 */
public class BarrierOption extends AbstractAssetMonteCarloProduct {

	private double maturity;
	private double strike;
	private double lowerBarrier;
	private double upperBarrier;
	private int underlyingIndex;

	/**
	 * It constructs an object representing a barrier, European call option on an underlying X.
	 * @param maturity The maturity T in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 * @param strike The strike K in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 * @param lowerBarrier the lower barrier B_L in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 * @param upperBarrier the upper barrier B_U in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 * @param underlyingIndex it identifies the underlying if model in getValue is multi-dimensional
	 */
	public BarrierOption(double maturity, double strike, double lowerBarrier, double upperBarrier, int underlyingIndex) {
		this.maturity = maturity;
		this.strike = strike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.underlyingIndex = underlyingIndex;
	}
	

	
	/**
	 * It constructs an object representing a barrier, European call option on an underlying X.
	 * @param maturity The maturity T in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 * @param strike The strike K in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 * @param lowerBarrier the lower barrier B_L in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 * @param upperBarrier the upper barrier B_U in the option payoff (X_T-K)1_{B_L <= X_t <= B_U for any t \in [0,T]}
	 */
	public BarrierOption(double maturity, double strike, double lowerBarrier, double upperBarrier) {
		this.maturity = maturity;
		this.strike = strike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.underlyingIndex = 0;
	}
	
	//the only method we have to implement

	@Override
	public RandomVariable getValue(double evaluationTime, AssetModelMonteCarloSimulationModel model)
			throws CalculationException {
		
		//we need it to check the path before maturity
		
		/*
		 * if we have the time distretization, t_0=0<t_1<t_2<...<t_n=T every "state of the world"
		 * omega_j will be associated to
		 * (X_{t_n}(omega_j)-K)^+ 1_{B_L <= X_{t_i}(omega_j)<= B_U for all i=0,1,...,n} 
		 */
		
		TimeDiscretization timeDiscretizationOfTheUnderlying = model.getTimeDiscretization();
		double[] discretizedTimes = timeDiscretizationOfTheUnderlying.getAsDoubleArray();
		
		/*
		 * At the beginning, it is 1 for all simulated trajectories. It will be 0 for those trajectories
		 * which exit the interval [B_L,B_U]
		 */
		RandomVariable insideBarriersAtAllTimes = new RandomVariableFromDoubleArray(1.0);
		
		/*
		 * 1_{B_L <= X_{t_i}(omega_j)<= B_U for all i=0,1,...,k+1} =
		 * 1_{B_L <= X_{t_{i}}(omega_j)<= B_U for all i=0,1,...,k}1_{B_L <= X_{t_{k+1}}(omega_j)<= B_U}
		 */
		DoubleUnaryOperator indicatorFunction = x -> (x>=lowerBarrier & x<=upperBarrier ? 1.0 : 0.0);
		//we check all times
		for (double currentTime : discretizedTimes) {
			currentTime = Math.min(currentTime, maturity);
			//this will give an object of type RandomVariable representing the array 
			//(X_{t_{k+1}}(omega_0),X_{t_{k+1}}(omega_1),...,X_{t_{k+1}}(omega_m))
			RandomVariable realizationsAtCurrentTime = model.getAssetValue(currentTime, underlyingIndex);
			//1_{B_L<=X_{t_{k+1}}(omega_j}<=B_U} for any simulation omega_j if t_{k+1} is the current time
			RandomVariable realizationsAtCurrentTimeInsideBarrier = realizationsAtCurrentTime.apply(indicatorFunction);
			
			/*
			 * Here we update insideBarriersAtAllTimes: its old value (that is, the one that is taken at the right) is
			 * 1_{B_L<=X_{t_i}(omega_j}<=B_U for any 0 <= i <= k} for any simulation omega_j if t_{k+1} is the current time.
			 * It gets updated by multiplication with realizationsAtCurrentTimeInsideBarrier, so it is now
			 * 1_{B_L<=X_{t_i}(omega_j}<=B_U for any 0 <= i <= k+1} for any simulation omega_j if t_{k+1} is the current time
			 */
			insideBarriersAtAllTimes = insideBarriersAtAllTimes.mult(realizationsAtCurrentTimeInsideBarrier);
		}

		/*
		 * From now on, this is the Finmath library implementation of European option apart from the point where
		 * we multiply values by insideBarriersAtAllTimes.
		 */
		// Get underlying and numeraire

		// Get X(T)
		final RandomVariable underlyingAtMaturity	= model.getAssetValue(maturity, underlyingIndex);

		// The payoff: values = max(underlying - strike, 0) = V(T) = max(X(T)-K,0)
		RandomVariable values = underlyingAtMaturity.sub(strike).floor(0.0);

		values = values.mult(insideBarriersAtAllTimes);
		// Discounting...
		final RandomVariable numeraireAtMaturity	= model.getNumeraire(maturity);
		final RandomVariable monteCarloWeights		= model.getMonteCarloWeights(maturity);
		values = values.div(numeraireAtMaturity).mult(monteCarloWeights);

		// ...to evaluation time.
		final RandomVariable	numeraireAtEvalTime			= model.getNumeraire(evaluationTime);
		final RandomVariable	monteCarloWeightsAtEvalTime	= model.getMonteCarloWeights(evaluationTime);
		values = values.mult(numeraireAtEvalTime).div(monteCarloWeightsAtEvalTime);

		return values;

	}

}
package it.univr.finitedifferences.ourproducts;

import java.util.function.DoubleUnaryOperator;

import it.univr.finitedifferences.oursolvers.FDMThetaMethodForKnockOutOption;
import net.finmath.finitedifference.models.FiniteDifference1DBoundary;
import net.finmath.finitedifference.models.FiniteDifference1DModel;
import net.finmath.finitedifference.products.FiniteDifference1DProduct;


public class FDMBarrierCallOption implements FiniteDifference1DProduct, FiniteDifference1DBoundary {
	private final double maturity;
	private final double strike;
	private final double lowerBarrier;
	private final double upperBarrier;
	private final double theta;

	public FDMBarrierCallOption(final double optionMaturity, final double optionStrike,
			final double lowerBarrier, final double upperBarrier, final double theta) {
		maturity = optionMaturity;
		strike = optionStrike;
		this.lowerBarrier = lowerBarrier;
		this.upperBarrier = upperBarrier;
		this.theta = theta;
	}

	@Override
	public double[][] getValue(final double evaluationTime, final FiniteDifference1DModel model) {

		/*
		 * The FDM algorithm requires the boundary conditions of the product.
		 * This product implements the boundary interface
		 */
		final FiniteDifference1DBoundary boundary = this;
		final FDMThetaMethodForKnockOutOption solver = new FDMThetaMethodForKnockOutOption(model, boundary, maturity, theta, lowerBarrier, upperBarrier);
		return solver.getValue(evaluationTime, maturity, new DoubleUnaryOperator() {
			@Override
			public double applyAsDouble(final double assetValue) {
				return Math.max(assetValue - strike, 0);
			}
		});
	}

	/*
	 * Implementation of the interface:
	 * @see net.finmath.finitedifference.products.FiniteDifference1DBoundary#getValueAtLowerBoundary(net.finmath.finitedifference.models.FDMBlackScholesModel, double, double)
	 */

	@Override
	public double getValueAtLowerBoundary(final FiniteDifference1DModel model, final double currentTime, final double stockPrice) {
		return 0;
	}

	@Override
	public double getValueAtUpperBoundary(final FiniteDifference1DModel model, final double currentTime, final double stockPrice) {
		return 0;
	}
}


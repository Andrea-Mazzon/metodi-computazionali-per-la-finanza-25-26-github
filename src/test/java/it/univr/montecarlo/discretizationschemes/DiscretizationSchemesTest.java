package it.univr.montecarlo.discretizationschemes;

import java.util.Random;

import it.univr.usefulmethodsarrays.UsefulMethodsArrays;
import net.finmath.time.TimeDiscretization;
import net.finmath.time.TimeDiscretizationFromArray;


/**
 * In this class we test the discretization schemes that we have implemented ourselves. The underlying
 * is a Black-Scholes model
 */
public class DiscretizationSchemesTest {

	
	public static void main(String[] args) {

		double initialValue = 100.0;
		double volatility = 0.3;
		double muDrift = 0.0;
		
		//here we see how to construct an object of type TimeDiscretization
		double finalTime = 1.0;
		double timeStep = 0.05;
		int numberOfTimesSteps = (int) (finalTime/timeStep);

		TimeDiscretization times = new TimeDiscretizationFromArray(0.0, numberOfTimesSteps, timeStep);

		//for the logarithm, one time step it is enough: we simulate the exact solution!
		TimeDiscretization timesForLogarithm = new TimeDiscretizationFromArray(0.0, 1, finalTime);

		//this is how much we save from the time discretization for the simulation of the logarithm: we use it for the simulations
		int ratioBetweenNumberOfTimeSteps = times.getNumberOfTimeSteps()/timesForLogarithm.getNumberOfTimeSteps();

		int numberOfSimulatedPaths = 10000;
		
		//we "use the gain" we get by simulating less times to simulate more paths
		int numberOfSimulatedPathsForLogarithm = numberOfSimulatedPaths*ratioBetweenNumberOfTimeSteps;

		
		int numberOfTests = 100;

		double[] expectedValuesEulerMaruyama = new double[numberOfTests];
		double[] expectedValuesMilstein = new double[numberOfTests];
		double[] expectedValuesEulerMaruyamaForLogarithm = new double[numberOfTests];

		Random seedGenerator = new Random();

		for (int i = 0; i<numberOfTests; i++) {

			//for every test, we look at the expectation with a possibly different seed
			int seed = seedGenerator.nextInt();

			AbstractProcessSimulation simulatorEulerMaruyama = new EulerSchemeForBlackScholes(volatility, muDrift, initialValue,
					numberOfSimulatedPaths, seed, times);

			AbstractProcessSimulation simulatorMilstein = new MilsteinSchemeForBlackScholes(volatility, muDrift, initialValue,
					numberOfSimulatedPaths, seed, times);

			AbstractProcessSimulation simulatorLogEuler = new LogEulerSchemeForBlackScholes(volatility, muDrift, initialValue,
					numberOfSimulatedPathsForLogarithm, seed, timesForLogarithm);

			//We will modify these lines together
			expectedValuesEulerMaruyama[i] = 0.0; 
			expectedValuesMilstein[i] = 0.0; 
			expectedValuesEulerMaruyamaForLogarithm[i] = 0.0; 
		}

		System.out.println("Average Euler Maruyama: = " + UsefulMethodsArrays.getAverage(expectedValuesEulerMaruyama));
		System.out.println("Average Milstein: = " + UsefulMethodsArrays.getAverage(expectedValuesMilstein));
		System.out.println("Average Log Euler Maruyama: = " + UsefulMethodsArrays.getAverage(expectedValuesEulerMaruyamaForLogarithm));
	}
}

package it.univr.montecarlo.numbersgeneration;

import java.util.Random;

/**
 * This class generates pseudo random numbers through a linear congruential random number generator:
 * a linear congruential random number generator produces natural numbers
 *
 * x[i+1] =(a*x[i] + c) mod_m,
 *
 * where x[0] = seed, for appropriate values of natural numbers a, c and m (for big m).
 *
 * We have a method generate() which generates all the sequence (i.e., an array), up to the number of
 * pseudo random integers we want to simulate. We also have two public methods, getRandomNumberSequence()
 * and getNextInteger(), that return all the sequence and the next integer x[count + 1], where count
 * gets incremented by 1 every time the method is called, respectively.
 *
 * @author Andrea Mazzon
 *
 */
public class LinearCongruentialGenerator {

    private long[] randomNumbers;// array of long
    // upcasting necessary, the result of Math.pow(2, 48) is understood as an int
    private final long modulus = (long) Math.pow(2, 48);
    private long a = 25214903917L; // if I don't put L after the number, it will complain that is out of range
    private final long c = 11;// automatic upcasting
    private long seed; // it will be the first entry of our pseudo random number list
    private int numberOfPseudoRandomNumbers;
    private int count = 1;

    // constructor
    public LinearCongruentialGenerator(int numberOfPseudoRandomNumbers, long seed) {
        this.numberOfPseudoRandomNumbers = numberOfPseudoRandomNumbers;
        this.seed = seed;
    }

    // overloaded constructor: random seed
    public LinearCongruentialGenerator(int numberOfPseudoRandomNumbers) {
            this.numberOfPseudoRandomNumbers = numberOfPseudoRandomNumbers;
            Random seedGenerator = new Random();
            seed = seedGenerator.nextInt();
    }

    private void generate() {
        // initialization! + 1 because the first one is the seed
        randomNumbers = new long[numberOfPseudoRandomNumbers + 1];
        randomNumbers[0] = seed; // the first entry is the seed: first number of the sequence
        for (int indexOfInteger = 0; indexOfInteger < numberOfPseudoRandomNumbers; indexOfInteger++) {
            randomNumbers[indexOfInteger + 1] = (a * randomNumbers[indexOfInteger] + c) % modulus;
        }
    }

    /**
     * getter method for the sequence of pseudo random natural numbers
     *
     * @return the sequence of pseudo random numbers
     */
    public long[] getRandomNumberSequence() {
        // another use of encapsulation: we call the method generate() only once
        if (randomNumbers == null) {
            generate();
        }
        return randomNumbers; // returns the already generated sequence of numbers.
    }

    /**
     * @return the next number of the sequence of pseudo random numbers
     */
    public long getNextInteger() {
        long[] sequence = getRandomNumberSequence();// it gets really generated only once
        return sequence[count++];
    }

    /**
     * getter method for the modulus
     *
     * @return the modulus of the congruence that generates the pseudo random
     *         numbers
     */
    public long getModulus() {
        return modulus;
    }

    /**
     * getter method for the length of the simulated sequence
     *
     * @return the length of the simulated sequence
     */
    public int getNumberOfPseudoRandomNumbers() {
        return numberOfPseudoRandomNumbers;
    }
} 

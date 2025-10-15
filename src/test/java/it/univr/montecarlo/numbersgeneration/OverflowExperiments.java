package it.univr.montecarlo.numbersgeneration;

/**
 * In this class we make experiments regarding overflows and underflows in Java, since this is
 * connected to the results we get in LinearCongruentialGenerator if we don't do any correction.   
 */
public class OverflowExperiments {

	public static void main(String[] args) {
		
		//you can change it and see what happens
		int valueOfOverflow = 1;
		int valueOfUnderflow = valueOfOverflow;
		
		//overflows and underflows with integers
		int maximumIntegerValue = Integer.MAX_VALUE;
		System.out.println("The maximum int value is " + maximumIntegerValue);//2^31-1
		
		//we go in the negative part of the wheel
		int maximumIntegerValuePlusOverflow = maximumIntegerValue+valueOfOverflow;
		System.out.println("The maximum int value plus " + valueOfOverflow + " is " + maximumIntegerValuePlusOverflow);
		
		System.out.println();

		int minimumIntegerValue = Integer.MIN_VALUE;
		System.out.println("The minimum int value is " + minimumIntegerValue);//-2^31
		
		//we go in the positive part of the wheel
		int minimumIntegerValueMinusUnderflow = minimumIntegerValue-valueOfUnderflow;
		System.out.println("The minimum int value minus  " + valueOfUnderflow + " is " + minimumIntegerValueMinusUnderflow);
			
		System.out.println();
	
		/*
		 * Written like this, it does not change anything: both maximumIntegerValue and valueOfOverflow
		 * are int, so the result is also an int (negative because of the overflow) which gets then
		 * upcasted to double.
		 */
		double maximumIntegerValuePlusOverflowAsDouble = maximumIntegerValue+valueOfOverflow;
		System.out.println("The maximum int value plus " + valueOfOverflow + " as double is " + maximumIntegerValuePlusOverflowAsDouble);
		
		/*
		 * Now, maximumIntegerValue gets upcasted to double, so the result is treated immediately as a double:
		 * it is therefore perfectly representable as a number bigger than maximumIntegerValue (since doubles
		 * have a bigger range)
		 */
		double maximumIntegerValueAsDoublePlusOverflow = (double) maximumIntegerValue+valueOfOverflow;
		System.out.println("The maximum int value as double plus " + valueOfOverflow + " is " + maximumIntegerValueAsDoublePlusOverflow);
		
		/*
		 * Look at what happens when we downcast it back to int: when we downcast a double to an int, we get
		 * "the biggest integer smaller or equal to the double": for example, for 2.8 we get 2.
		 * Now, "the biggest integer" smaller than maximumIntegerValueAsDoublePlusOverflow (representable
		 * in Java) is maximumIntegerValue!
		 */
		int maximumIntegerValueAsDoublePlusOverflowAsInt = (int) maximumIntegerValueAsDoublePlusOverflow;
		System.out.println("Which downcasted to int is " + maximumIntegerValueAsDoublePlusOverflowAsInt);
				
		System.out.println();
		System.out.println();
		
		//overflows and underflows with long
		
		long maximumLongValue = Long.MAX_VALUE;
		System.out.println("The maximum long value is " + maximumLongValue);//2^63-1
		long maximumLongValuePlusOverflow = maximumLongValue+valueOfOverflow;
		System.out.println("The maximum long value plus " + valueOfOverflow + " is " + maximumLongValuePlusOverflow);
		
		System.out.println();

		long minimumLongValue = Long.MIN_VALUE;
		System.out.println("The minimum long value is " + minimumLongValue);//-2^63
		long minimumLongValueMinusUnderflow = minimumLongValue-valueOfUnderflow;
		System.out.println("The minimum long value minus  " + valueOfUnderflow + " is " + minimumLongValueMinusUnderflow);
	}

}

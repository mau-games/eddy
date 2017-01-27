package util;

import java.util.Arrays;

import javafx.geometry.Point2D;

public class Util {

	/**
	 * Calculates the Manhattan (taxicab) distance between two Point2Ds
	 * 
	 * @param start Start point
	 * @param end End point
	 * @return The Manhattan distance between start and end.
	 */
	public static int manhattanDistance(Point2D start, Point2D end){
		return (int) Math.round(Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY()));
	}
	
	/**
	 * Calculates the Euclidean distance between two Point2Ds
	 * 
	 * @param start Start point
	 * @param end End point
	 * @return The Euclidean distance between start and end.
	 */
	public static double euclideanDistance(Point2D start, Point2D end){
		return start.distance(end);
	}
	
	/**
	 * Calculates the average (mean) of an array of Doubles
	 * 
	 * @param numbers Array of doubles
	 * @return The average
	 */
    public static double calcAverage(Double[] numbers)
    {
    	return Arrays.asList(numbers).stream().reduce(0.0,(a,b)-> a + b) / numbers.length;
    }

    /**
     * Calculates the variance of an array of Doubles.
     * TODO: This should maybe be changed to the unbiased version with (...)/(1-N) instead of (...)/N
     * (See http://mathworld.wolfram.com/Variance.html)
     * 
     * @param numbers Array of Doubles
     * @return The variance
     */
    public static double calcVariance(Double[] numbers)
    {
    	double avg = calcAverage(numbers);
    	return Arrays.asList(numbers).stream().reduce(0.0,(a,b)-> a + (b - avg)*(b - avg)) / (numbers.length);
    }
	
}

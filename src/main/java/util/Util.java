package util;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

/**
 * Just a set of utilities.
 * 
 * @author Alexander Baldwin, Malmö University
 * @author Johan Holmberg, Malmö University
 */
public class Util {

	
	private static Random random;
	
	/**
	 * Calculates the Manhattan (taxicab) distance between two Point2Ds
	 * 
	 * @param start Start point
	 * @param end End point
	 * @return The Manhattan distance between start and end.
	 */
	public static int manhattanDistance(Point start, Point end){
		return (int) Math.round(Math.abs(start.getX() - end.getX()) + Math.abs(start.getY() - end.getY()));
	}
	
	/**
	 * Calculates the Euclidean distance between two Point2Ds
	 * 
	 * @param start Start point
	 * @param end End point
	 * @return The Euclidean distance between start and end.
	 */
	public static double euclideanDistance(Point start, Point end){
		return Math.sqrt(Math.pow(start.getX() - end.getX(),2) + Math.pow(start.getY() - end.getY(),2));
	}
	
	/**
	 * Calculates the average (mean) of an array of Doubles
	 * 
	 * @param numbers Array of doubles
	 * @return The average
	 */
    public static double calcAverage(Double[] numbers)
    {
    	return Arrays.asList(numbers).stream().reduce(0.0,Double::sum) / numbers.length;
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
    /**
     * Get a random float between min (inclusive) and max (exclusive)
     * 
     * @param min The lower bound (inclusive)
     * @param max The upper bound (exclusive)
     * @return A random float between min (inclusive) and max (exclusive)
     */
	public static float getNextFloat(float min, float max){
		if(random == null)
			random = new Random();
		return min + (float)random.nextDouble()*(max - min);
	}
	
	/**
	 * Get a random int between min (inclusive) and max (exclusive)
	 * 
	 * @param min The lower bound (inclusive)
	 * @param max The upper bound (exclusive)
	 * @return A random int between min (inclusive) and max (exclusive)
	 */
	public static int getNextInt(int min, int max){
		if(random == null)
			random = new Random();
		return min + random.nextInt(max - min);
	}

	/**
	 * Normalises a path to work equally well on Windows as on sane operating
	 * systems. The provided path should be Unix-formatted. ~/ will be
	 * converted to the current user's home directory.
	 * 
	 * @param path A Unix-formatted path.
	 * @return A path that is usable by the current operating system.
	 */
	public static String normalisePath(String path) {
		if (path.startsWith("~/")) {
			path = path.replace("~", System.getProperty("user.home"));
		}
		if (File.separator.equals("\\")) {
			path = path.replace("/", "\\");
		}

		return path;
	}
	
}

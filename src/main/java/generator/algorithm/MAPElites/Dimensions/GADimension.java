package generator.algorithm.MAPElites.Dimensions;

import game.Room;
import generator.algorithm.ZoneIndividual;

public abstract class GADimension
{
	public enum DimensionTypes //Till example
	{
		NUMBER_PATTERNS,
		DIFFICULTY,
		GEOMETRICAL_COMPLEXITY,
		LEARNING,
		REWARD,
		SYMMETRY,
		SIMILARITY
	}
	
	protected DimensionTypes dimension;
	
	protected double granularity; //This variable relates to how many values the dimension is divided and the assign granularity
	
	public GADimension()
	{
		
	}
	
	public DimensionTypes GetType()
	{
		return dimension;
	}
	
	public static boolean CorrectDimensionLevel(ZoneIndividual individual, DimensionTypes type, double dimensionValue)
	{
		return individual.getDimensionValue(type) <= dimensionValue;
	}
	
//	public abstract boolean CorrectDimensionLevel(ZoneIndividual individual, DimensionType type);
	
	public abstract double CalculateValue(ZoneIndividual individual, Room target);
	
	public double GetGranularity()
	{
		return granularity;
	}
	
	//Fuck it, add the methods here
	//No! 
	
}

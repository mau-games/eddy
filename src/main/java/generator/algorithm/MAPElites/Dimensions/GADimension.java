package generator.algorithm.MAPElites.Dimensions;

import game.Room;
import generator.algorithm.ZoneIndividual;

//PLS COMMENT!!
public abstract class GADimension
{
	public enum DimensionTypes //Till example
	{
		SYMMETRY,
		SIMILARITY,
		NUMBER_PATTERNS,
		DIFFICULTY,
		GEOM_COMPLEXITY,
		LEARNING,
		REWARD,
		LINEARITY

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
	
	public static GADimension CreateDimension(DimensionTypes dim, Number granularity)
	{
		switch(dim)
		{
		case DIFFICULTY:
			break;
		case GEOM_COMPLEXITY:
			break;
		case LEARNING:
			break;
		case LINEARITY:
			return new LinearityGADimension(granularity.floatValue());
		case NUMBER_PATTERNS:
			return new NPatternGADimension(granularity.floatValue());
		case REWARD:
			break;
		case SIMILARITY:
			return new SimilarityGADimension(granularity.floatValue());
		case SYMMETRY:
			return new SymmetryGADimension(granularity.floatValue());
		default:
			break;
		
		}
		
		return null;
	}
	
	//Fuck it, add the methods here
	//No! 
	
}

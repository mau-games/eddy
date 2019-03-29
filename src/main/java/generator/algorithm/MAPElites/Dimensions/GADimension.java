package generator.algorithm.MAPElites.Dimensions;

import game.Room;
import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.GADimensionsGranularity;

//PLS COMMENT!!
public abstract class GADimension
{
	public enum DimensionTypes //Till example
	{
		SYMMETRY,
		SIMILARITY,
		NUMBER_PATTERNS,
		NUMBER_MESO_PATTERN,
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
	
	public static boolean CorrectDimensionLevel(ZoneIndividual individual, DimensionTypes type, GADimensionsGranularity dimGran)
	{
		return individual.getDimensionValue(type) <= dimGran.getMaxValue() && individual.getDimensionValue(type) >= dimGran.getMinValue();
		
//		return (individual.getDimensionValue(type) >= dimensionValue) && individual.getDimensionValue(type) < (double)(index+1)/5.0d;
	}
	
//	public abstract boolean CorrectDimensionLevel(ZoneIndividual individual, DimensionType type);
	
	public abstract double CalculateValue(ZoneIndividual individual, Room target);
	public abstract double CalculateValue(Room individualRoom, Room target);
	
	public double GetGranularity()
	{
		return granularity;
	}
	
	public static GADimension CreateDimension(DimensionTypes dim, Number granularity)
	{
		switch(dim)
		{
		case DIFFICULTY:
//			return  new DifficultyGADimension(granularity.floatValue());
			break;
		case GEOM_COMPLEXITY: //This can be the density
//			return  new GeomComplexityGADimension(granularity.floatValue());
			break;
		case LEARNING:
//			return  new LearningGADimension(granularity.floatValue());
			break;
		case LINEARITY:
			return new LinearityGADimension(granularity.floatValue());
		case NUMBER_PATTERNS:
			return new NPatternGADimension(granularity.floatValue());
		case NUMBER_MESO_PATTERN:
			return new NMesoPatternGADimension(granularity.floatValue());
		case REWARD:
//			return  new RewardGADimension(granularity.floatValue());
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

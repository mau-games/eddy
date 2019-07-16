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
		INNER_SIMILARITY,
		NUMBER_PATTERNS,
		NUMBER_MESO_PATTERN,
		DIFFICULTY,
		GEOM_COMPLEXITY,
		LENENCY,
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
		case DIFFICULTY: //difficulty and lenency can be very very similar
//			return  new DifficultyGADimension(granularity.floatValue());
			break;
		case GEOM_COMPLEXITY: //This can be the density and sparsity
//			return  new GeomComplexityGADimension(granularity.floatValue());
			break;
		case LENENCY:
			return new LenencyGADimension(granularity.floatValue());
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
		case INNER_SIMILARITY:
			return new CharacteristicSimilarityGADimension(granularity.floatValue());
		default:
			break;
		
		}
		
		return null;
	}
	
	public static double calculateIndividualValue(DimensionTypes dim, Room individualRoom)
	{
		switch(dim)
		{
		case DIFFICULTY:
			return -1.0; //Not implemented
		case GEOM_COMPLEXITY:
			return -1.0; //Not implemented
		case LENENCY:
			return LenencyGADimension.getValue(individualRoom);
		case LINEARITY:
			return LinearityGADimension.getValue(individualRoom);
		case NUMBER_MESO_PATTERN:
			return NMesoPatternGADimension.getValue(individualRoom);
		case NUMBER_PATTERNS:
			return NPatternGADimension.getValue(individualRoom);
		case REWARD:
			return -1.0; //Not implemented
		case SIMILARITY:
			return SimilarityGADimension.getValue(individualRoom);
		case INNER_SIMILARITY:
			return CharacteristicSimilarityGADimension.getValue(individualRoom);
		case SYMMETRY:
			return SymmetryGADimension.getValue(individualRoom);
		default:
			return -1.0;
		
		}
	}

	
}

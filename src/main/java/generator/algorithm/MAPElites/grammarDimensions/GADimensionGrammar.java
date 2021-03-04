package generator.algorithm.MAPElites.grammarDimensions;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import game.Room;
import game.narrative.GrammarGraph;
import generator.algorithm.GrammarIndividual;
import generator.algorithm.MAPElites.Dimensions.*;
import generator.algorithm.MAPElites.GADimensionsGranularity;
import generator.algorithm.ZoneIndividual;

//PLS COMMENT!!
public abstract class GADimensionGrammar
{
	public enum GrammarDimensionTypes //Till example
	{
		CONFLICT,
		SIZE,
		DIVERSITY
	}

	protected GrammarDimensionTypes dimension;
	protected double granularity; //This variable relates to how many values the dimension is divided and the assign granularity

	public GADimensionGrammar()
	{
		
	}
	
	public GrammarDimensionTypes GetType()
	{
		return dimension;
	}
	
	public static boolean CorrectDimensionLevel(GrammarIndividual individual, GrammarDimensionTypes type, GADimensionsGranularity dimGran)
	{
		return individual.getDimensionValue(type) <= dimGran.getMaxValue() && individual.getDimensionValue(type) >= dimGran.getMinValue();
		
//		return (individual.getDimensionValue(type) >= dimensionValue) && individual.getDimensionValue(type) < (double)(index+1)/5.0d;
	}
	
//	public abstract boolean CorrectDimensionLevel(ZoneIndividual individual, DimensionType type);
	
	public abstract double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph);
	public abstract double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph);
	
	public double GetGranularity()
	{
		return granularity;
	}
	
	public static GADimensionGrammar CreateDimension(GrammarDimensionTypes dim, Number granularity)
	{
		switch(dim)
		{
			case CONFLICT:
				return new ConflictGADimensionGrammar(granularity.floatValue());
			case SIZE:
				return new SizeGADimensionGrammar(granularity.floatValue());
			case DIVERSITY:
				return new DiversityGADimensionGrammar(granularity.floatValue());
			default:
				break;
		
		}
		
		return null;
	}


	public static double calculateIndividualValue(GrammarDimensionTypes dim, GrammarGraph individual_grammar)
	{
		System.out.println("PRROOOOOOOOOOBLEMS");
		switch(dim)
		{
			case CONFLICT:
				return ConflictGADimensionGrammar.getValue(individual_grammar);
			case SIZE:
				return SizeGADimensionGrammar.getValue(individual_grammar);
			case DIVERSITY:
				return DiversityGADimensionGrammar.getValue(individual_grammar);
			default:
				return -1.0;

		}
	}

	
}

package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import generator.algorithm.GrammarIndividual;
import generator.algorithm.MAPElites.GADimensionsGranularity;

//PLS COMMENT!!
public abstract class GADimensionGrammar
{
	public enum GrammarDimensionTypes //Till example
	{
		CONFLICT,
		STEP,
		DIVERSITY,
		TENSION,
		STRUCTURE,
		PLOT_POINTS,
		PLOT_DEVICES,
		PLOT_TWISTS,
		INTERESTING
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
			case STEP:
				return new StepGADimensionGrammar(granularity.floatValue());
			case DIVERSITY:
				return new DiversityGADimensionGrammar(granularity.floatValue());
			case TENSION:
				return new TensionGADimensionGrammar(granularity.floatValue());
			case STRUCTURE:
				return new StructureGADimensionGrammar(granularity.floatValue());
			case PLOT_POINTS:
				return new PlotPointGADimensionGrammar(granularity.floatValue());
			case PLOT_DEVICES:
				return new PlotDevicesGADimensionGrammar(granularity.floatValue());
			case PLOT_TWISTS:
				return new PlotTwistsGADimensionGrammar(granularity.floatValue());
			case INTERESTING:
				return new InterestingGADimensionGrammar(granularity.floatValue());
			default:
				break;
		
		}
		
		return null;
	}


	public static double calculateIndividualValue(GrammarDimensionTypes dim, GrammarGraph individual_grammar)
	{
//		System.out.println("PRROOOOOOOOOOBLEMS");
		switch(dim)
		{
			case CONFLICT:
				return ConflictGADimensionGrammar.getValue(individual_grammar);
			case STEP:
				return StepGADimensionGrammar.getValue(individual_grammar);
			case DIVERSITY:
				return DiversityGADimensionGrammar.getValue(individual_grammar);
			case TENSION:
				return TensionGADimensionGrammar.getValue(individual_grammar);
			case STRUCTURE:
				return StructureGADimensionGrammar.getValue(individual_grammar);
			case PLOT_POINTS:
				return PlotPointGADimensionGrammar.getValue(individual_grammar);
			case PLOT_DEVICES:
				return PlotDevicesGADimensionGrammar.getValue(individual_grammar);
			case PLOT_TWISTS:
				return PlotTwistsGADimensionGrammar.getValue(individual_grammar);
			case INTERESTING:
				return InterestingGADimensionGrammar.getValue(individual_grammar);
			default:
				return -1.0;

		}
	}

	
}
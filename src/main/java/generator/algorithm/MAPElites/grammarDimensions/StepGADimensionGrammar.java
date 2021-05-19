package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import generator.algorithm.GrammarIndividual;
import util.Util;

public class StepGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;

	public StepGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.STEP;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph, GrammarGraph axiom)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutputBest(axiom, 1);

		short dist = target_graph.distanceBetweenGraphs(nStructure);

		return Math.min(1.0, ((double)dist)/11.0);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph, GrammarGraph axiom) {
//		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		short dist = target_graph.distanceBetweenGraphs(individual_graph);

		return Math.min(1.0, ((double)dist)/11.0);
	}

	public static double getValue(GrammarGraph individual_graph, GrammarGraph target_graph)
	{
		short dist = target_graph.distanceBetweenGraphs(individual_graph);

		return Math.min(1.0, ((double)dist)/11.0);
	}
}

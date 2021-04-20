package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import generator.algorithm.GrammarIndividual;
import generator.algorithm.MAPElites.NSEvolutionarySystemEvaluator;
import util.Util;

public class InterestingGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;
	NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();

	public InterestingGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.INTERESTING;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutputBest(target_graph, 1);

		double[] results = evaluator.testEvaluation(nStructure, target_graph);
		return results[0];
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph) {

		double[] results = evaluator.testEvaluation(individual_graph, target_graph);
		return results[0];
	}

	public static double getValue(GrammarGraph individual_graph)
	{
		return Util.getNextFloat(0.0f, 1.0f);
	}
}

package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import game.narrative.NarrativeFinder.*;
import generator.algorithm.GrammarIndividual;
import util.Util;

/***
 * FIXME: THIS NEEDS TO CHANGE
 */
public class PlotTwistsGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;

	public PlotTwistsGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.PLOT_TWISTS;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph, GrammarGraph axiom)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutputBest(axiom, 1);
		nStructure.pattern_finder.findNarrativePatterns(target_graph);

		float plot_twists = nStructure.pattern_finder.getAllPatternsByType(PlotTwist.class).size();
		return Math.min(1.0, plot_twists/5.0);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph, GrammarGraph axiom) {
//		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		individual_graph.pattern_finder.findNarrativePatterns(target_graph);

		float plot_twists = individual_graph.pattern_finder.getAllPatternsByType(PlotTwist.class).size();
		return Math.min(1.0, plot_twists/5.0);
	}

	public static double getValue(GrammarGraph individual_graph, GrammarGraph target_graph)
	{
		individual_graph.pattern_finder.findNarrativePatterns(target_graph);

		float plot_twists = individual_graph.pattern_finder.getAllPatternsByType(PlotTwist.class).size();
		return Math.min(1.0, plot_twists/5.0);
	}
}

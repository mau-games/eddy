package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import game.narrative.NarrativeFinder.CompoundConflictPattern;
import game.narrative.NarrativeFinder.ImplicitConflictPattern;
import game.narrative.NarrativeFinder.PlotPoint;
import game.narrative.NarrativeFinder.SimpleConflictPattern;
import generator.algorithm.GrammarIndividual;
import util.Util;

/***
 * FIXME: THIS NEEDS TO CHANGE
 */
public class PlotPointGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;

	public PlotPointGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.PLOT_POINTS;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutputBest(target_graph, 1);
		nStructure.pattern_finder.findNarrativePatterns(target_graph);

		float plot_points = nStructure.pattern_finder.getAllPatternsByType(PlotPoint.class).size();
		return Math.min(1.0, plot_points/5.0);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph) {
//		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		individual_graph.pattern_finder.findNarrativePatterns(target_graph);

		float plot_points = individual_graph.pattern_finder.getAllPatternsByType(PlotPoint.class).size();
		return Math.min(1.0, plot_points/5.0);
	}

	public static double getValue(GrammarGraph individual_graph)
	{
		return Util.getNextFloat(0.0f, 1.0f);
	}
}

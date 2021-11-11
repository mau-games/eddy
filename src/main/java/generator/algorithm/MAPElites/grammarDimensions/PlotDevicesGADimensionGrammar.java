package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import game.narrative.NarrativeFinder.*;
import generator.algorithm.GrammarIndividual;
import util.Util;

/***
 * FIXME: THIS NEEDS TO CHANGE
 */
public class PlotDevicesGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;
	static double pattern_normalizer = 5.0;

	public PlotDevicesGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.PLOT_DEVICES;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph, GrammarGraph axiom)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutputBest(axiom, 1);
		nStructure.pattern_finder.findNarrativePatterns(target_graph);

		float basic_plot_device = nStructure.pattern_finder.getAllPatternsByType(PlotDevicePattern.class).size();
		float active_plot_device = nStructure.pattern_finder.getAllPatternsByType(ActivePlotDevice.class).size();

		return Math.min(1.0, active_plot_device/pattern_normalizer);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph, GrammarGraph axiom) {
//		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		individual_graph.pattern_finder.findNarrativePatterns(target_graph);

		float basic_plot_device = individual_graph.pattern_finder.getAllPatternsByType(PlotDevicePattern.class).size();
		float active_plot_device = individual_graph.pattern_finder.getAllPatternsByType(ActivePlotDevice.class).size();

		return Math.min(1.0, active_plot_device/pattern_normalizer);
	}

	public static double getValue(GrammarGraph individual_graph, GrammarGraph target_graph)
	{
		individual_graph.pattern_finder.findNarrativePatterns(target_graph);

		float basic_plot_device = individual_graph.pattern_finder.getAllPatternsByType(PlotDevicePattern.class).size();
		float active_plot_device = individual_graph.pattern_finder.getAllPatternsByType(ActivePlotDevice.class).size();

		return Math.min(1.0, active_plot_device/pattern_normalizer);
	}
}

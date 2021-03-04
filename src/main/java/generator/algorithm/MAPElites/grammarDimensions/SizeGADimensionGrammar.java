package generator.algorithm.MAPElites.grammarDimensions;

import finder.PatternFinder;
import finder.patterns.CompositePattern;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import game.narrative.GrammarGraph;
import generator.algorithm.GrammarIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.ZoneIndividual;
import util.Util;

import java.util.List;

public class SizeGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;

	public SizeGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.SIZE;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		short dist = target_graph.distanceBetweenGraphs(nStructure);

		return Math.min(1.0, ((double)dist)/10.0);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph) {
//		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		short dist = target_graph.distanceBetweenGraphs(individual_graph);

		return Math.min(1.0, ((double)dist)/10.0);
	}

	public static double getValue(GrammarGraph individual_graph)
	{
		return Util.getNextFloat(0.0f, 1.0f);
	}
}

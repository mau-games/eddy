package generator.algorithm.MAPElites.grammarDimensions;

import finder.PatternFinder;
import finder.patterns.CompositePattern;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import game.narrative.GrammarGraph;
import game.tiles.NullTile;
import generator.algorithm.GrammarIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.ZoneIndividual;
import util.Util;

import java.util.List;

public class ConflictGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;

	public ConflictGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.CONFLICT;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph) {
		return Util.getNextFloat(0.0f, 1.0f);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph) {
		return Util.getNextFloat(0.0f, 1.0f);
	}

	public static double getValue(GrammarGraph individual_graph)
	{
		return Util.getNextFloat(0.0f, 1.0f);
	}
}

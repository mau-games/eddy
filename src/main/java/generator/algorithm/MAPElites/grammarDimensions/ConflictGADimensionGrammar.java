package generator.algorithm.MAPElites.grammarDimensions;

import finder.PatternFinder;
import finder.patterns.CompositePattern;
import finder.patterns.meso.ChokePoint;
import finder.patterns.meso.DeadEnd;
import finder.patterns.meso.GuardedTreasure;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.TVTropeType;
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
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		float raw_conf = nStructure.checkAmountNodes(TVTropeType.CONFLICT, false);
		raw_conf += nStructure.checkAmountNodes(TVTropeType.CONA, false);
		raw_conf +=  nStructure.checkAmountNodes(TVTropeType.COSO, false);

//		short dist = target_graph.distanceBetweenGraphs(nStructure);

		return Math.min(1.0, raw_conf/5.0);


//		return Util.getNextFloat(0.0f, 1.0f);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph) {
//		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		float raw_conf = individual_graph.checkAmountNodes(TVTropeType.CONFLICT, false);
		raw_conf += individual_graph.checkAmountNodes(TVTropeType.CONA, false);
		raw_conf +=  individual_graph.checkAmountNodes(TVTropeType.COSO, false);

//		short dist = target_graph.distanceBetweenGraphs(nStructure);

		return Math.min(1.0, raw_conf/5.0);
	}

	public static double getValue(GrammarGraph individual_graph)
	{
		return Util.getNextFloat(0.0f, 1.0f);
	}
}

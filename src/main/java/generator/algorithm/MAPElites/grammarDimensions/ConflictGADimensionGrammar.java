package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import game.narrative.NarrativeFinder.CompoundConflictPattern;
import game.narrative.NarrativeFinder.ImplicitConflictPattern;
import game.narrative.NarrativeFinder.SimpleConflictPattern;
import generator.algorithm.GrammarIndividual;
import util.Util;

/***
 * FIXME: THIS NEEDS TO CHANGE
 */
public class ConflictGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;

	public ConflictGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.CONFLICT;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph, GrammarGraph axiom)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutputBest(axiom, 1);
		nStructure.pattern_finder.findNarrativePatterns(target_graph);

		float explicit_conflicts = nStructure.pattern_finder.getAllPatternsByType(SimpleConflictPattern.class).size();
		float implicit_conflicts = nStructure.pattern_finder.getAllPatternsByType(ImplicitConflictPattern.class).size();
		float compound_conflicts = nStructure.pattern_finder.getAllPatternsByType(CompoundConflictPattern.class).size();

		float conflicts_together = explicit_conflicts + implicit_conflicts;

		return Math.min(1.0, explicit_conflicts/5.0);
//		return Math.min(1.0, implicit_conflicts/5.0);
//		return Math.min(1.0, compound_conflicts/5.0);
//		return Math.min(1.0, conflicts_together/5.0);


//		float raw_conf = nStructure.checkAmountNodes(TVTropeType.CONFLICT, false);
//		return Math.min(1.0, raw_conf/5.0);


//		return Util.getNextFloat(0.0f, 1.0f);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph, GrammarGraph axiom) {
//		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutput(target_graph, 1);

		individual_graph.pattern_finder.findNarrativePatterns(target_graph);

		float explicit_conflicts = individual_graph.pattern_finder.getAllPatternsByType(SimpleConflictPattern.class).size();
		float implicit_conflicts = individual_graph.pattern_finder.getAllPatternsByType(ImplicitConflictPattern.class).size();
		float compound_conflicts = individual_graph.pattern_finder.getAllPatternsByType(CompoundConflictPattern.class).size();

		float conflicts_together = explicit_conflicts + implicit_conflicts;

		return Math.min(1.0, explicit_conflicts/5.0);
//		return Math.min(1.0, implicit_conflicts/5.0);
//		return Math.min(1.0, compound_conflicts/5.0);
//		return Math.min(1.0, conflicts_together/5.0);

//		float raw_conf = individual_graph.checkAmountNodes(TVTropeType.CONFLICT, false);
//		return Math.min(1.0, raw_conf/5.0);
	}

	public static double getValue(GrammarGraph individual_graph, GrammarGraph target_graph)
	{
		individual_graph.pattern_finder.findNarrativePatterns(target_graph);

		float explicit_conflicts = individual_graph.pattern_finder.getAllPatternsByType(SimpleConflictPattern.class).size();
		float implicit_conflicts = individual_graph.pattern_finder.getAllPatternsByType(ImplicitConflictPattern.class).size();
		float compound_conflicts = individual_graph.pattern_finder.getAllPatternsByType(CompoundConflictPattern.class).size();

		float conflicts_together = explicit_conflicts + implicit_conflicts;

		return Math.min(1.0, explicit_conflicts/5.0);
	}
}

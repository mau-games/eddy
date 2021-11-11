package generator.algorithm.MAPElites.grammarDimensions;

import game.narrative.GrammarGraph;
import generator.algorithm.GrammarIndividual;
import util.Util;

/***
 * This class will calculate all the structures within the grammar.
 */
public class StructureGADimensionGrammar extends GADimensionGrammar {

	double patternMultiplier = 4.0;

	public StructureGADimensionGrammar(float granularity)
	{
		super();
		dimension = GrammarDimensionTypes.STRUCTURE;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(GrammarIndividual individual, GrammarGraph target_graph, GrammarGraph axiom)
	{
		GrammarGraph nStructure = individual.getPhenotype().getGrammarGraphOutputBest(axiom, 1);

		float diversity = nStructure.getNodeDiversity(true); //Local diversity (1 if all nodes are different)
//		float diversity = nStructure.getNodeDiversity(false); //Global diversity (1 if the graph have as many nodes as types, and they are all different)
//		float diversity = nStructure.getNodeDiversityBase(); //Global diversity focused on the base types (1 if there is at least 1 of each base node type)

		return Math.min(1.0, diversity);
	}

	@Override
	public double CalculateValue(GrammarGraph individual_graph, GrammarGraph target_graph, GrammarGraph axiom) {

		float diversity = individual_graph.getNodeDiversity(true); //Local diversity (1 if all nodes are different)
//		float diversity = individual_graph.getNodeDiversity(false); //Global diversity (1 if the graph have as many nodes as types, and they are all different)
//		float diversity = individual_graph.getNodeDiversityBase(); //Global diversity focused on the base types (1 if there is at least 1 of each base node type)

		return Math.min(1.0, diversity);
	}

	public static double getValue(GrammarGraph individual_graph, GrammarGraph target_graph)
	{
		return Util.getNextFloat(0.0f, 1.0f);
	}
}

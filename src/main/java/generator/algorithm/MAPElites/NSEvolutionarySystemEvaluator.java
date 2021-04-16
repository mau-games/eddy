package generator.algorithm.MAPElites;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import game.narrative.GrammarGraph;
import game.narrative.NarrativeFinder.CompoundConflictPattern;
import game.narrative.NarrativeFinder.ImplicitConflictPattern;
import game.narrative.NarrativeFinder.NarrativePattern;
import game.narrative.NarrativeFinder.SimpleConflictPattern;
import game.narrative.TVTropeType;
import generator.algorithm.GrammarIndividual;
import sun.java2d.pipe.SpanShapeRenderer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/***
 * This class is the one in charge of evaluating where should the evolutionary system focus on now
 * Basically, it will act as the "focalization" part of the system, as well as deciding when are we at
 * "Engagement" and when are we at "Reflection"
 *
 *  - Focalization: adapted from gervas, is where the system should focus on. For us, 1) it will drive the evolution to
 *  expand, add, or remove narrative "perspectives" (related to constant elements such as 5MA subdividing into specific hero types,
 *  Enemies subdividng, conflcits subdividng, or structure (3SA) subdividng into specific acts). 2) It will also be used top denote
 *  what areas of the graph to focus on.
 *
 *  But how will this be evaluateD? The idea  *  thus far is based on where the designer is focusing, where the MAP-Elites
 *  have focused thus far, and finally and possibly, manually   defined by the designer.
 *
 *  I feel and think that Focalization should be something like a direction vector to where to focus on the graph. It can also be
 *  that in the engagement step is where it should be applied the focalization
 *
 *  - Engagement is the generation of narratives from the system applying the grammar rules and constraints (perhaps here is where
 *
 *  - I am interested on also evaluating the three-four "C"s.
 *      - Coherence: Everything makes sense
 *      - Consistency: Similarity across the structuce
 *      - Cohesion: Linking between words to hold together the text (how related the tropes are??)
 *       --> Probably we can use something like, if a pattern is "nothing" there are cohesion problems?
 *      - Causality (Might be more related specifically to the quests).
 */
public class NSEvolutionarySystemEvaluator
{
    int generation; //current generation - classic counter
    double[] best_intragenerational_fitness; //Best fitness for bot Feasibles [0] and Infeasibles [1] thus far
    ArrayList<Double> Fbest_fitness; //Feasible best fitness at each generation
    ArrayList<Double> IFbest_fitness; //Infeasible best fitness at each generation

    public NSEvolutionarySystemEvaluator()
    {
        generation = 0;
        best_intragenerational_fitness = new double[2];
        Fbest_fitness = new ArrayList<Double>();
        IFbest_fitness = new ArrayList<Double>();

    }

    // Add the necessary methods
    public double evaluateFeasibleIndividual(Method... methods)
    {
//        for(/)
//        nStructure.pattern_finder.findNarrativePatterns();
        return 0.0f;
    }

    public double evaluateINFeasibleIndividual()
    {
//        nStructure.pattern_finder.findNarrativePatterns();
        return 0.0;
    }

    public void evaluateFeasibleGrammarIndividual(GrammarIndividual ind, GrammarGraph axiom)
    {
        List<LinkedHashMap<Integer, Integer>> feasible_grammar_recipes = ind.getPhenotype().feasible_grammar_recipes;
        LinkedHashMap<Integer, Integer> best_recipe = null;
        double best_fitness = Double.NEGATIVE_INFINITY;
        double final_fitness = 0.0;

        for(LinkedHashMap<Integer, Integer> feasible_recipe : feasible_grammar_recipes)
        {
            GrammarGraph nStructure = ind.getPhenotype().getGrammarGraphOutput(axiom, feasible_recipe);
            double fitness = 0.0;
            double w_any = 0.2; //Weight for the amount of "ANY" in the grammar (ANY is a wildcard)
            double w_node_repetition = 0.3; //Weight for the node repetition count
            int min_freq_nodes = 1; //Min freq for the node repetition
            TVTropeType[] excluded_repeated_nodes = {TVTropeType.CONFLICT}; //Nodes to exclude from the count.
            //TODO: Size is going to be done by the elites+
            double w_tSize = 0.5; //Weight for the size of the resulting grammar
            float expected_size = 4.0f; //Expected size (anything more or less than this decreases fitness)


            //AND THEN WHAT?
            //TESTING
//		if(nStructure.nodes.get(0).getGrammarNodeType() == TVTropeType.ANY)
//		{
//			ind.setFitness(0.0);
////		ind.setFitness(1.0);
//			ind.setEvaluate(true);
//			return;
//		}

            short dist = axiom.distanceBetweenGraphs(nStructure);

            //A bit hardcore, perhaps we should scale based on how different
            //then we could use as target one step more.
            if(axiom.testGraphMatchPattern(nStructure))
                fitness = 0.0;
            else
            {
                //Get first how many ANY exist
                float cumulative_any = 1.0f - nStructure.checkAmountNodes(TVTropeType.ANY, true);

                //get the right size!! -- probably for elites
                float targetSize = expected_size - nStructure.checkGraphSize();
                targetSize *= 0.1f;
                targetSize = 1.0f - Math.abs(targetSize);


//			fitness += targetSize;

                //Penalize repeting nodes
                float node_repetition = 1.0f - nStructure.SameNodes(min_freq_nodes, excluded_repeated_nodes);

                fitness = (w_any * (cumulative_any)) + (w_tSize * targetSize) + (w_node_repetition * node_repetition);

            }

            nStructure.pattern_finder.findNarrativePatterns(axiom);
            float structure_count = 0.0f;
            for(NarrativePattern np : nStructure.pattern_finder.all_narrative_patterns)
            {
                if(np instanceof CompoundConflictPattern)
                    structure_count++;
            }

            float targetSize = expected_size - structure_count;
            targetSize *= 0.1f;
            fitness = 1.0f - Math.abs(targetSize);

            if(fitness > best_fitness)
            {
                best_fitness = fitness;
                best_recipe = feasible_recipe;
            }

            final_fitness += fitness;

        }
        // We set not only the best fitness to the individual, but also the avg. of all the feasible recipes!
        final_fitness = final_fitness/(double)feasible_grammar_recipes.size();
        ind.setAvgFitness(final_fitness);
        ind.setFitness(best_fitness);
        ind.getPhenotype().setBestRecipe(best_recipe);
//		ind.setFitness(1.0);
        ind.setEvaluate(true);
    }

    ///////////////// Now we define the methods we want to use!!! ///////////////////

    //Penalize having a big amount of a specific node
    public float getCumulativeNodeFitness(float weight, GrammarGraph nStructure, TVTropeType node_type)
    {
        float node_amount = nStructure.checkAmountNodes(node_type, true);
        return  weight <= -1.0f ? 1.0f - node_amount : (1.0f - node_amount) * weight;
    }

    //Penalize or reward being closer to the target size!
    public float getGeneratedGraphSizeFitness(float weight, GrammarGraph nStructure, float expected_size)
    {
        float targetSize = expected_size - nStructure.checkGraphSize();
        targetSize *= 0.1f;
        targetSize = 1.0f - Math.abs(targetSize);

        return  weight <= -1.0f ? targetSize : targetSize * weight;
    }

    //Penalize repetitiveness
    public float getRepetitivePenalizationFitness(float weight, GrammarGraph nStructure, int min_freq_nodes, TVTropeType ... excluded_repeated_nodes )
    {
        //Penalize repeating nodes
        float node_repetition = 1.0f - nStructure.SameNodes(min_freq_nodes, excluded_repeated_nodes);

        return  weight <= -1.0f ? node_repetition : node_repetition * weight;
    }

    /**
     * No weight here.
     * We are interested in calculating:
     *  Compound conflict amount
     *  Explicit conflicts (Simple conflict pattern)
     *  Implicit conflicts (Implicit conflict pattern)
     *  Fake conflicts - (due to reveal)
     *  Real conflicts - The rest!
     */
    public int[] getConflictPatternMeasurements(GrammarGraph nStructure)
    {
        int[] results = new int[5];
//        ArrayList<SimpleConflictPattern> explicit_conflicts = new ArrayList<SimpleConflictPattern>();
//        ArrayList<ImplicitConflictPattern> implicit_conflicts = new ArrayList<ImplicitConflictPattern>();

        for(NarrativePattern np : nStructure.pattern_finder.all_narrative_patterns)
        {
            if(np instanceof CompoundConflictPattern)
            {
                results[0]++;
            }
            else if(np instanceof SimpleConflictPattern)
            {
                results[1]++;
                if(((SimpleConflictPattern) np).fake_conflict)
                    results[3]++;
                else
                    results[4]++;
            }
            else if(np instanceof ImplicitConflictPattern)
            {
                results[2]++;
                if(((ImplicitConflictPattern) np).fake_conflict)
                    results[3]++;
                else
                    results[4]++;
            }

        }

        //Information about conflicts should be clear after this!
        return results;
    }



}

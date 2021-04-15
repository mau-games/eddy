package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This pattern simply categorize the rest of nodes that do not belong to any composite as Nothing
 * which means that do not contribute to anything.
 * not partial calculations are done (i.e., how close to not being nothing it is), that's the evolutionary algorithm to work on :P
 *
 */
public class NothingNarrativePattern extends NarrativePattern
{
    public static List<NarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<NarrativePattern> results = new ArrayList<NarrativePattern>();
        ArrayList<GrammarNode> node_ids = new ArrayList<GrammarNode>();

        /**
         * Since we record all the subgraphs, we can easily encounter what nodes do not belong to any by checking their id.
         */

        // Get all the ids of the main graph
        node_ids.addAll(narrative_graph.nodes);
//        for(GrammarNode gn : narrative_graph.nodes)
//        {
//            node_ids.add(gn.getID());
//        }

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof CompositeNarrativePattern) //I think implicit conflict shouldn't count!
            {
                for(GrammarGraph pattern_gg : ((CompositeNarrativePattern) np).pattern_subgraphs)
                {
                    for(GrammarNode gn : pattern_gg.nodes)
                    {
//                        if(node_ids.contains(gn.getID()))
//                            node_ids.remove(gn.getID());
                        node_ids.remove(gn);
                    }
                }
            }
        }

        //Now, if there is any node in the main list, they are all NOTHING!!!
        for(GrammarNode gn : node_ids)
        {
            NothingNarrativePattern nothing = new NothingNarrativePattern();
            nothing.connected_node = gn;

            results.add(nothing);
        }



        return results;
    }
}
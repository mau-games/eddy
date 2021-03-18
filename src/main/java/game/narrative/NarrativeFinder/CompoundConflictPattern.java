package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compound Conflict pattern is formally described as:
 * CCp = {Sn, C, Tn} where Sn are all the sources that share a conflict, C is the conflict itself, and Tn are all the targets sharing the same conflict
 * This will be calculated by getting all the simple conflict patterns and grouping them based on the conflict they have
 */
public class CompoundConflictPattern extends CompositeNarrativePattern
{
    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /** So this one is a bit harder to identify
         *  1- First,
         */

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof SimpleConflictPattern)
            {








                //Now we search for the connections
                List<GrammarNode> to_me = new ArrayList<GrammarNode>();
                List<GrammarNode> from_me = new ArrayList<GrammarNode>();
                List<GrammarNode> non_dir = new ArrayList<GrammarNode>();

                //These are from me
                for(Map.Entry<GrammarNode, Integer> keyValue : ((StructureNodePattern) np).connected_node.connections.entrySet()) //Get Target!
                {
                    from_me.add(keyValue.getKey());
                }

                //These are to me
                for(GrammarNode node : narrative_graph.getAllConnectionsToNode(((StructureNodePattern) np).connected_node)) //Get Source
                {
                    to_me.add(node);
                }

                //If there is no source or no targets we do not have
                if(from_me.isEmpty() || to_me.isEmpty())
                    continue;

                //Now lets create a subgraph! and create the simple conflicts!
                for(GrammarNode source_node : to_me)
                {
                    for(GrammarNode target_node : from_me)
                    {
                        GrammarGraph temp = new GrammarGraph();
                        temp.addNode(source_node, false);
                        temp.addNode(((StructureNodePattern) np).connected_node, false);
                        temp.addNode(target_node, false);

                        SimpleConflictPattern scp = new SimpleConflictPattern();
                        scp.pattern_subgraphs.add(temp);
                        results.add(scp);
                    }
                }

            }
        }


        return results;
    }
}
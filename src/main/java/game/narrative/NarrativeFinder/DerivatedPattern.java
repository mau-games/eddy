package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Derivated pattern is formally described as:
 * Dp = {EcndChar} DP is the SUM of all the non-directional connections from a character pattern (hero/enemy)
 * CCp = {Sn, C, Tn} where Sn are all the sources that share a conflict, C is the conflict itself, and Tn are all the targets sharing the same conflict
 * This will be calculated by getting all the simple conflict patterns and grouping them based on the conflict they have
 */
public class DerivatedPattern extends CompositeNarrativePattern
{
    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /** So this one is a bit harder to identify
         *  1- First, lets go through each of there StructureNodePatterns encountered thus far
         *  2- Then we should iterate the connections of those nodes trying to find directions
         *  3- a basic concli
         */

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof HeroNodePattern || np instanceof VillainNodePattern)
            {
                //Now we search for the connections
                List<GrammarNode> non_dir = new ArrayList<GrammarNode>();

                //Need to limit the non_dir connections!!!!

                //These are from me
                for(Map.Entry<GrammarNode, Integer> keyValue : ((StructureNodePattern) np).connected_node.connections.entrySet()) //Get Target!
                {
                    NarrativePattern target_pat = finder.existNodeAsPattern(keyValue.getKey());
                    //Limited to only add if it is hero or villain
                    if(target_pat!= null && (target_pat instanceof HeroNodePattern || target_pat instanceof VillainNodePattern))
                    {
                        from_me.add(keyValue.getKey());
                    }
                }

                //These are to me (only directional!)
                for(GrammarNode node : narrative_graph.getAllConnectionsToNode(((StructureNodePattern) np).connected_node, true)) //Get Source
                {
                    NarrativePattern source_pat = finder.existNodeAsPattern(node);
                    //Only add if it is hero or villain
                    if(source_pat!= null && (source_pat instanceof HeroNodePattern || source_pat instanceof VillainNodePattern))
                    {
                        to_me.add(node);
                    }
//                    to_me.add(node);
                }

                //If there is no source or no targets we do not have
                if(from_me.isEmpty() || to_me.isEmpty())
                    continue;

                //Start filling the compound conflict!
                GrammarGraph combined_graph = new GrammarGraph();
                combined_graph.addNode(((StructureNodePattern) np).connected_node, false);

                //Now lets create a subgraph! and create the simple conflicts!
                for(GrammarNode source_node : to_me)
                {
                    //Add source nodes to the compound graph!
                    combined_graph.addNode(source_node, false);

                    for(GrammarNode target_node : from_me)
                    {
                        GrammarGraph temp = new GrammarGraph();
                        temp.addNode(source_node, false);
                        temp.addNode(((StructureNodePattern) np).connected_node, false);
                        temp.addNode(target_node, false);

                        SimpleConflictPattern scp = new SimpleConflictPattern();
                        scp.addSubgraph(temp);
//                        scp.pattern_subgraphs.add(temp);
//                        scp.relevant_nodes.add(source_node);

                        results.add(scp);
                    }
                }

                for(GrammarNode target_node : from_me)
                {
                    //Add target nodes to the compound graph!!
                    combined_graph.addNode(target_node, false);
                }

                CompoundConflictPattern ccp = new CompoundConflictPattern();
                ccp.addSubgraph(combined_graph);
//                ccp.pattern_subgraphs.add(combined_graph);
                results.add(ccp);


            }
        }


        return results;
    }
}
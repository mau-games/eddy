package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//FIXME: This will change to simple conflict and compound conflict
public class Structure extends CompositeNarrativePattern
{
    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /** So this one is a bit harder to identify
         *  1- First, lets go through each of there StructureNodePatterns encountered thus far
         *  2- Then we should iterate the connections of those nodes trying to find directions
         *  3- a basic concli
         */

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof StructureNodePattern)
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




                //Now we create the subgraphs??




//                for(Map.Entry<GrammarNode, Integer> keyValue : ((StructureNodePattern) np).connected_node.connections.entrySet())
//                {
//
//                }
//
//                for(((StructureNodePattern) np).connected_node.connections)
//                {
//
//                }

            }
        }


        return results;
    }
}
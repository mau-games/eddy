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
    /**
     * Returns a measure of the quality of this pattern
     *
     * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
     */
    public double calculateQuality(List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        this.quality = -1.0;

        //How many conflicts is this compound confict?
//        System.out.println("#Conflicts: " + patterns.size());
//
//        //What is the best way to get bidirectional connections to conflict?
//        // Probably you could check the connections within the subgraphs of the SimpleConflicts
//        // but actually, you can just check the SimpleConflicts and Source and Target should be the same!
//        for(NarrativePattern simple_conflict : patterns)
//        {
//            if(simple_conflict instanceof SimpleConflictPattern) //They are, but sanity check!
//            {
//                System.out.println("This Simple conflict is self conflict: " + ((SimpleConflictPattern) simple_conflict).isSelfConflict());
//            }
//        }

        //More than 1 bidirectional connection is bad! (return -1.0)
//        GrammarGraph sg = pattern_subgraphs.get(0);
//        GrammarNode conflict_node = sg.getNodeDiversity()


        return this.quality;
    }

    public int getConflictCount() {return patterns.size();}
    public int getSelfConflictCount()
    {
        int self_conflict = 0;

        for(NarrativePattern simple_conflict : patterns)
        {
            if(simple_conflict instanceof SimpleConflictPattern && ((SimpleConflictPattern) simple_conflict).isSelfConflict()) //They are, but sanity check!
            {
                self_conflict++;
            }
        }

        return self_conflict;
    }


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
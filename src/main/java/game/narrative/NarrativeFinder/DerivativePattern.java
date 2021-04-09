package game.narrative.NarrativeFinder;

import finder.graph.Node;
import finder.patterns.Pattern;
import finder.patterns.meso.DeadEnd;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.*;

/**
 * Derivated pattern is formally described as:
 * Dp = {E cnd Char} DP is the SUM (E) of all the non-directional connections (cnd) from a character pattern (hero/enemy)
 */
public class DerivativePattern extends CompositeNarrativePattern
{
    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /**
         * So the idea here is to detect all the
         */

        /** So this one is a bit harder to identify
         *  1- First, lets go through each of there StructureNodePatterns encountered thus far
         *  2- Then we should iterate the connections of those nodes trying to find directions
         *  3- a basic concli
         */

        for(NarrativePattern np : currentPatterns)
        {
            // We are not interested in getting conflict nodes or similar (3AS, HJ?, Cmx, ACT)
            if(!(np instanceof StructureNodePattern) && np.connected_patterns_from_me.containsKey(0))
            {
                //Now lets check for non-directional connections from me (Entitles!)
                for(NarrativePattern non_directed_pat : np.connected_patterns_from_me.get(0))
                {
                    non_directed_pat.derivative = true;
                }
            }
        }

        Stack<NarrativePattern> patternQueue = new Stack<NarrativePattern>();

        //Now iterate to find the derivatives!
        for(NarrativePattern np : currentPatterns)
        {
            // We are not interested in getting conflict nodes or similar (3AS, HJ?, Cmx, ACT)
            if(!(np instanceof StructureNodePattern))
            {
                if(!np.derivative)
                {
                    patternQueue.add(np);
                }
            }
        }

        DerivativePattern dp = null;
        GrammarGraph temp = new GrammarGraph();

        // Iterate to find the whole derivative pattern (from the root to the end)!
        while(!patternQueue.isEmpty()){

            if(dp == null)
            {
                dp = new DerivativePattern();
                temp = new GrammarGraph();
            }

            NarrativePattern current = patternQueue.pop();
            dp.addNarrativePattern(current);
            temp.addNode(current.connected_node, false);

            //There is more from this node!
            if(current.connected_patterns_from_me.containsKey(0))
            {
                for(NarrativePattern non_directed_pat : current.connected_patterns_from_me.get(0))
                {
                    if(non_directed_pat.derivative && (!(non_directed_pat instanceof  StructureNodePattern)))
                        patternQueue.add(non_directed_pat);
                }
            }
            else //We have reach the end!
            {
                if(dp.getPatterns().size() > 1) //This node was root, and something derivated!
                {
                    dp.addSubgraph(temp);
                    results.add(dp);
                }
                dp = null;
            }
        }

        //reset all the derivative flags!
        for(NarrativePattern np : currentPatterns)
        {
            np.derivative = false;
        }

        return results;
    }
}
package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Reveal pattern is formally described as:
 * Rp = {H,E(!:)} or {E,H(!:)}, where H is hero, E is Enemy, "," is an uni-directional connection,
 * and ":" is non-directional connection, and (!:) means that the TO connection has no other connection.
 * When a hero is connected
 */
public class RevealPattern extends CompositeNarrativePattern
{
    public NarrativePattern source;
    public NarrativePattern target;

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /**
         * So the idea here is to detect all the
         */

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof HeroNodePattern && np.connected_patterns_from_me.containsKey(1))
            {
                //Now lets check for directional connections from me (Entitles!)
                for(NarrativePattern non_directed_pat : np.connected_patterns_from_me.get(1))
                {
                    if(non_directed_pat instanceof VillainNodePattern && !non_directed_pat.connected_patterns_from_me.containsKey(0))
                    {
                        RevealPattern rp = new RevealPattern();
                        GrammarGraph temp = new GrammarGraph();

                        rp.addNarrativePattern(np);
                        rp.addNarrativePattern(non_directed_pat);
                        temp.addNode(np.connected_node, false);
                        temp.addNode(non_directed_pat.connected_node, false);
                        rp.addSubgraph(temp);
                        rp.source = np;
                        rp.target = non_directed_pat;

                        results.add(rp);


                    }
                }
            }
            else if(np instanceof VillainNodePattern && np.connected_patterns_from_me.containsKey(1))
            {
                //Now lets check for directional connections from me (Entitles!)
                for(NarrativePattern non_directed_pat : np.connected_patterns_from_me.get(1))
                {
                    if(non_directed_pat instanceof HeroNodePattern && !non_directed_pat.connected_patterns_from_me.containsKey(0))
                    {
                        RevealPattern rp = new RevealPattern();
                        GrammarGraph temp = new GrammarGraph();

                        rp.addNarrativePattern(np);
                        rp.addNarrativePattern(non_directed_pat);
                        temp.addNode(np.connected_node, false);
                        temp.addNode(non_directed_pat.connected_node, false);
                        rp.addSubgraph(temp);
                        rp.source = np;
                        rp.target = non_directed_pat;

                        results.add(rp);
                    }
                }
            }
        }

        return results;
    }
}
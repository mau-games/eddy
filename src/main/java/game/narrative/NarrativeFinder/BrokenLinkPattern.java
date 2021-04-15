package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This pattern simply categorize the connections that are broken from micro-patterns!
 * which means that do not contribute to anything.
 * not partial calculations are done (i.e., how close to not being nothing it is), that's the evolutionary algorithm to work on :P
 *
 */
public class BrokenLinkPattern extends CompositeNarrativePattern
{
    public BrokenLinkPattern(NarrativePattern source, NarrativePattern target)
    {
        super();
        GrammarGraph temp = new GrammarGraph();

        temp.addNode(source.connected_node, false);
        temp.addNode(target.connected_node, false);

        addNarrativePattern(source);
        addNarrativePattern(target);
        addSubgraph(temp);

    }

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /***
         * We simply want to go through the heroes, villains, and conflicts to see their connections and which ones are incorrect or "broken"
         *  - Heroes and villains simply check for directional connections (or bi) and if they are connected with another of the same type
         *  we have a broken link
         *  - Struct does the same but uses all the 3 type of connections (Conflicts shouldn't be connected to other conflicts!!!)
         *  at least not directly :)
         */

        for(NarrativePattern np : currentPatterns)
        {
            //Check only basic patterns
            if(np instanceof VillainNodePattern)
            {
                if(np.connected_patterns_from_me.containsKey(1))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(1))
                    {
                        if(from_me instanceof VillainNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me);
                            results.add(blp);
                        }
                    }
                }

                if(np.connected_patterns_from_me.containsKey(2))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(2))
                    {
                        if(from_me instanceof VillainNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me);
                            results.add(blp);
                        }
                    }
                }
            }
            else if(np instanceof HeroNodePattern)
            {
                if(np.connected_patterns_from_me.containsKey(1))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(1))
                    {
                        if(from_me instanceof HeroNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me);
                            results.add(blp);
                        }
                    }
                }

                if(np.connected_patterns_from_me.containsKey(2))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(2))
                    {
                        if(from_me instanceof HeroNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me);
                            results.add(blp);
                        }
                    }
                }
            }
            else if(np instanceof StructureNodePattern)
            {
                if(np.connected_patterns_from_me.containsKey(0))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(0))
                    {
                        if(from_me instanceof StructureNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me);
                            results.add(blp);
                        }
                    }
                }

                if(np.connected_patterns_from_me.containsKey(1))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(1))
                    {
                        if(from_me instanceof StructureNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me);
                            results.add(blp);
                        }
                    }
                }

                if(np.connected_patterns_from_me.containsKey(2))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(2))
                    {
                        if(from_me instanceof StructureNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me);
                            results.add(blp);
                        }
                    }
                }
            }
        }

        return results;
    }
}
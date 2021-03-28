package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeroNodePattern extends BasicNarrativePattern
{
    public HeroNodePattern(GrammarNode node)
    {
        super();
        this.connected_node = node;
    }

    public static List<NarrativePattern> matches(GrammarGraph narrative_graph)
    {
        ArrayList<NarrativePattern> results = new ArrayList<NarrativePattern>();

        //This one is simple, just iterate all nodes and check what are the nodes that are for structure
        //In the waiting for a restruct, that means all the nodes that are conflict
        for(GrammarNode node : narrative_graph.nodes)
        {
            if(node.getGrammarNodeType().getValue() >= 10 && node.getGrammarNodeType().getValue() < 20)
            {
                HeroNodePattern hnp = new HeroNodePattern(node);
                hnp.quality = 1.0; //TODO: Here we need some type of calculation
                results.add(hnp);
            }
        }

        return results;
    }

//    public



    /**
     * Returns a measure of the quality of this pattern
     *
     * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
     */
    public double calculateQuality(List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
//        for(Map.Entry<Integer, List<NarrativePattern>> keyValue : this.connected_patterns_from_me.entrySet())
//        {
//            System.out.println("This HERO node has: " + keyValue.getValue().size() + " narrative patterns connected with type: " + keyValue.getKey());
//        }

        // At the moment we do not really care about non-directional connections
        for(Map.Entry<Integer, List<NarrativePattern>> keyValue : this.connected_patterns_from_me.entrySet())
        {
            if(keyValue.getKey() == 0)
                continue;

            // At the moment it is not good to be connected only to anything that it is not a structure node!
            for(NarrativePattern np : keyValue.getValue())
            {
                if(!(np instanceof StructureNodePattern))
                {
                    //Houston, we got a problem!
//                    System.out.println("This " + this.getClass().getName() + " is connected to " + np.getClass().getSimpleName());
                }
            }
        }

        return quality;
    }
}

package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;

public class VillainNodePattern extends NarrativePattern
{
    public VillainNodePattern(GrammarNode node)
    {
        this.connected_node = node;
    }

    public static List<NarrativePattern> matches(GrammarGraph narrative_graph)
    {
        ArrayList<NarrativePattern> results = new ArrayList<NarrativePattern>();

        //This one is simple, just iterate all nodes and check what are the nodes that are for structure
        //In the waiting for a restruct, that means all the nodes that are conflict
        for(GrammarNode node : narrative_graph.nodes)
        {
            if(node.getGrammarNodeType().getValue() >= 30 && node.getGrammarNodeType().getValue() < 40)
            {
                VillainNodePattern vnp = new VillainNodePattern(node);
                vnp.quality = 1.0; //TODO: Here we need some type of calculation
                results.add(vnp);
            }
        }

        return results;
    }
}

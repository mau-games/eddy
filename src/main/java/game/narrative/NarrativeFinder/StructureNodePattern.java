package game.narrative.NarrativeFinder;

import finder.geometry.Geometry;
import finder.patterns.Pattern;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;

public class StructureNodePattern extends NarrativePattern
{
//    protected GrammarNode connected_node;

    public StructureNodePattern(GrammarNode node)
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
            if(node.getGrammarNodeType().getValue() >= 20 && node.getGrammarNodeType().getValue() < 30)
            {
                StructureNodePattern snp = new StructureNodePattern(node);
                snp.quality = 1.0; //TODO: Here we need some type of calculation
                results.add(snp);
            }
        }

        return results;
    }
}

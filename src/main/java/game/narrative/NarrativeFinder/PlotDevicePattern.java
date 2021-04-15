package game.narrative.NarrativeFinder;

import finder.patterns.Pattern;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import game.narrative.TVTropeType;

import java.util.ArrayList;
import java.util.List;

public class PlotDevicePattern extends BasicNarrativePattern
{
    boolean optional = false;

    public PlotDevicePattern(GrammarNode node)
    {
        super();
        this.connected_node = node;
    }

    public static List<NarrativePattern> matches(GrammarGraph narrative_graph)
    {
        ArrayList<NarrativePattern> results = new ArrayList<NarrativePattern>();

        //This one is simple, just iterate all nodes and check what are the nodes are plot device!
        for(GrammarNode node : narrative_graph.nodes)
        {
            if(node.getGrammarNodeType().getValue() >= 40 && node.getGrammarNodeType().getValue() < 50)
            {
                PlotDevicePattern pdp = new PlotDevicePattern(node);

                if(node.getGrammarNodeType() == TVTropeType.MHQ)
                    pdp.optional = true;

                pdp.quality = 1.0; //TODO: Here we need some type of calculation
                results.add(pdp);
            }
        }

        return results;
    }
}
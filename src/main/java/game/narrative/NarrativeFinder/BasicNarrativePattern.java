package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasicNarrativePattern extends NarrativePattern
{
    public BasicNarrativePattern()
    {
        super();
    }

    /**
     * We store all connections (that are of type basic_pattern!) to the pattern
     * This should be called after all BasicNarrativePatterns have been found! (matched!)
     * @param narrative_graph
     */
    public void storeAllConnections(GrammarGraph narrative_graph, NarrativeStructPatternFinder finder)
    {
        //These are from me - Why are we checking connected patterns here?
        for(Map.Entry<GrammarNode, Integer> keyValue : this.connected_node.connections.entrySet())
        {
            NarrativePattern pat = finder.existNodeAsPattern(keyValue.getKey());

            if(pat!= null)
            {
                if(!connected_patterns_from_me.containsKey(keyValue.getValue()))
                {
//                    connected_patterns.put(keyValue.getValue(), new ArrayList<NarrativePattern>());
                    connected_patterns_from_me.put(keyValue.getValue(), new ArrayList<NarrativePattern>());
                }

//                connected_patterns.get(keyValue.getValue()).add(pat);
                connected_patterns_from_me.get(keyValue.getValue()).add(pat);
            }
        }

        //These are to me All of then!
        for(Map.Entry<GrammarNode, Integer> keyValue : narrative_graph.getAllConnectionsTypesToNode(this.connected_node, false).entrySet())
        {
            NarrativePattern pat = finder.existNodeAsPattern(keyValue.getKey());

            if(pat!= null)
            {
                if(!connected_patterns.containsKey(keyValue.getValue()))
                {
                    connected_patterns.put(keyValue.getValue(), new ArrayList<NarrativePattern>());
                }

                connected_patterns.get(keyValue.getValue()).add(pat);
            }
        }
    }

}

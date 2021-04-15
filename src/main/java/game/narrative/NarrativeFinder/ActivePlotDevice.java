package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Active PLot Device Pattern is formally defined as:
 * APD = {(E,PD && :PD) (; OR , ANY}
 */
public class ActivePlotDevice extends CompositeNarrativePattern
{
    public PlotDevicePattern device;
    public ActivePlotDevice(PlotDevicePattern pdp)
    {
        super();
        device = pdp;
    }

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /**
         * 1- Find The plot devices (specific micro-pattern)
         * 2- Get all the connections from and to
         * 3- Only get if max 1 non-directional, any amount of directional, and no bi-directional
         * 4- Should only have 1 output (and this should be connected to the incoming)
         *      - if and incoming was non-directional then output can be non-directional (else no)
         *      - no bidirectional
         *      - In fact output connection is not necessary (but is restricted to 1 in this implementation)
         */

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof PlotDevicePattern)
            {
                if(np.connected_patterns_from_me.containsKey(2) || np.connected_patterns.containsKey(2) //not accepting bidirectional
                    || (!np.connected_patterns.containsKey(0) && !np.connected_patterns.containsKey(1))) //neither that nothing is connected to it!
                {
                    continue;
                }

                int nondir_connection_counter = np.connected_patterns_from_me.containsKey(0) ? np.connected_patterns_from_me.get(0).size() : 0;
                int dir_connection_counter = np.connected_patterns_from_me.containsKey(1) ? np.connected_patterns_from_me.get(1).size() : 0;

                if(nondir_connection_counter + dir_connection_counter > 1)
                    continue; //Bad plot device! more than one output

                if(np.connected_patterns.containsKey(0) && np.connected_patterns.get(0).size() > 1) //more than 1 non-directional connected to me
                {
                    continue;
                }

                //Ok now I think we are gucci, lets create the active plot!!
                ActivePlotDevice apd = new ActivePlotDevice((PlotDevicePattern) np);
                GrammarGraph temp_graph = new GrammarGraph();

                //All to me
                if(np.connected_patterns.containsKey(0))
                {
                    for(NarrativePattern to_me : np.connected_patterns.get(0))
                    {
                        temp_graph.addNode(to_me.connected_node, false);
                        apd.addNarrativePattern(to_me);
                    }
                }

                if(np.connected_patterns.containsKey(1))
                {
                    for(NarrativePattern to_me : np.connected_patterns.get(1))
                    {
                        temp_graph.addNode(to_me.connected_node, false);
                        apd.addNarrativePattern(to_me);
                    }
                }

                //The actual plot device
                temp_graph.addNode(np.connected_node, false);
                apd.addNarrativePattern(np);

                //ALL from me!
                if(np.connected_patterns_from_me.containsKey(0))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(0))
                    {
                        temp_graph.addNode(from_me.connected_node, false);
                        apd.addNarrativePattern(from_me);
                    }
                }

                if(np.connected_patterns_from_me.containsKey(1))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(1))
                    {
                        temp_graph.addNode(from_me.connected_node, false);
                        apd.addNarrativePattern(from_me);
                    }
                }

                apd.addSubgraph(temp_graph);
                results.add(apd);
            }
        }

        return results;
    }
}
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
    public BrokenLinkPattern(NarrativePattern source, NarrativePattern target, NarrativePattern main)
    {
        super();
        GrammarGraph temp = new GrammarGraph();

        temp.addNode(source.connected_node, false);
        temp.addNode(target.connected_node, false);

        addNarrativePattern(source);
        addNarrativePattern(target);
        addSubgraph(temp);

        connected_node = main.connected_node;

    }

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();
        ArrayList<ActivePlotDevice> active_plot_devices = finder.getAllPatternsByType(ActivePlotDevice.class);
//        ArrayList<StructureNodePattern> cs = finder.getAllPatternsByType(StructureNodePattern.class);
//
//        for(SimpleConflictPattern scp : conflicts)
//        {
//            StructureNodePattern _remove = null;
//            for(StructureNodePattern c : cs)
//            {
//                if(scp.connected_node == c.connected_node)
//                {
//                    _remove = c;
//                    break;
//                }
//            }
//
//            if(_remove != null)
//                cs.remove(_remove);
//        }

        /***
         * We simply want to go through the heroes, villains, and conflicts to see their connections and which ones are incorrect or "broken"
         *  - Heroes and villains simply check for directional connections (or bi) and if they are connected with another of the same type
         *  we have a broken link.
         *   ------ Actually, also if we have direct or indirect connections to others and that does not generate any PATTERN
         *   ------ Actually, If we have a double connection is bad!
         *  - Struct does the same but uses all the 3 type of connections (Conflicts shouldn't be connected to other conflicts!!!)
         *  at least not directly :) (neither being connected with non-directional to anything (except to Plot Device)
         */

        for(NarrativePattern np : currentPatterns)
        {
//            //Check only basic patterns
//            if(np instanceof VillainNodePattern)
//            {
//                if(np.connected_patterns_from_me.containsKey(1))
//                {
//                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(1))
//                    {
//                        if(from_me instanceof VillainNodePattern) //BROKEN LINK!
//                        {
//                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
//                            results.add(blp);
//                        }
//                    }
//                }
//
//                if(np.connected_patterns_from_me.containsKey(2))
//                {
//                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(2))
//                    {
//                        if(from_me instanceof VillainNodePattern) //BROKEN LINK!
//                        {
//                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
//                            results.add(blp);
//                        }
//                    }
//                }
//            }
//            else if(np instanceof HeroNodePattern)
//            {
//                if(np.connected_patterns_from_me.containsKey(1))
//                {
//                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(1))
//                    {
//                        if(from_me instanceof HeroNodePattern) //BROKEN LINK!
//                        {
//                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
//                            results.add(blp);
//                        }
//                    }
//                }
//
//                if(np.connected_patterns_from_me.containsKey(2))
//                {
//                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(2))
//                    {
//                        if(from_me instanceof HeroNodePattern) //BROKEN LINK!
//                        {
//                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
//                            results.add(blp);
//                        }
//                    }
//                }
//            }
            if(np instanceof StructureNodePattern)
            {
                if(np.connected_patterns_from_me.containsKey(0))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(0))
                    {
                        if(from_me instanceof StructureNodePattern || from_me instanceof HeroNodePattern || from_me instanceof VillainNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
                            results.add(blp);
                        }
                        else if(from_me instanceof PlotDevicePattern)
                        {
                            boolean valid_con = false;
                            for(ActivePlotDevice apd : active_plot_devices)
                            {
                                if(apd.device.equals(from_me) && apd.pattern_subgraphs.get(0).nodes.contains(np.connected_node))
                                {
                                    valid_con = true;
                                    break;
                                }
                            }

                            if(!valid_con)
                            {
                                BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
                                results.add(blp);
                            }
                        }
                    }
                }

                if(np.connected_patterns_from_me.containsKey(1))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(1))
                    {
                        if(from_me instanceof StructureNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
                            results.add(blp);
                        }
                        else if(from_me instanceof PlotDevicePattern)
                        {
                            boolean valid_con = false;
                            for(ActivePlotDevice apd : active_plot_devices)
                            {
                                if(apd.device.equals(from_me) && apd.pattern_subgraphs.get(0).nodes.contains(np.connected_node))
                                {
                                    valid_con = true;
                                    break;
                                }
                            }

                            if(!valid_con)
                            {
                                BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
                                results.add(blp);
                            }
                        }
                    }
                }

                if(np.connected_patterns_from_me.containsKey(2))
                {
                    for(NarrativePattern from_me : np.connected_patterns_from_me.get(2))
                    {
                        if(from_me instanceof StructureNodePattern) //BROKEN LINK!
                        {
                            BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
                            results.add(blp);
                        }
                        else if(from_me instanceof PlotDevicePattern)
                        {
                            boolean valid_con = false;
                            for(ActivePlotDevice apd : active_plot_devices)
                            {
                                if(apd.device.equals(from_me) && apd.pattern_subgraphs.get(0).nodes.contains(np.connected_node))
                                {
                                    valid_con = true;
                                    break;
                                }
                            }

                            if(!valid_con)
                            {
                                BrokenLinkPattern blp = new BrokenLinkPattern(np, from_me, np);
                                results.add(blp);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }
}
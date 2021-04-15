package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Plotpoints are not specifically patterns, but more a product of the pattern and they are formally described as:
 * Pp = {E <= Dd & RevP & PlotDevices} : Where E is the SUM of all the partial-ordered DerivatePattern derivates (not the source),
 * & Target of Reveal Pattern & Plot Devices.
 *
 * Pp can be ordered within a set (a derivative) but does not show any other temporal relation.
 * This is due because derivatives have by design TEMPORAL and CAUSAL relation to the source and to each other.
 */
public class PlotTwist extends CompositeNarrativePattern
{
    // Plot points will be saved individually (then they can be assessed if happening or not) but also here as an ordered list.
//    public ArrayList<NarrativePattern> order = new ArrayList<NarrativePattern>();
//
//    public

    public NarrativePattern core_pattern;

    public PlotTwist()
    {
        core_pattern = new NarrativePattern();
    }

    //TODO: implement
    public void getOrderedPattern()
    {

    }

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /**
         * Analyze derivative, plot devices, and other elements that can constitute a plot point!
         * (other elements will require a bit
         */
        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof RevealPattern)
            {
                PlotTwist pt = new PlotTwist();

                pt.core_pattern = np;
                pt.addSubgraph(((RevealPattern) np).pattern_subgraphs.get(0));
                pt.addNarrativePattern(((RevealPattern) np).source);
                pt.addNarrativePattern(((RevealPattern) np).target);

                results.add(pt);
            }
            else if(np instanceof DerivativePattern)
            {
                for(NarrativePattern derivative : ((DerivativePattern) np).derivatives)
                {
                    if(derivative.getClass() != ((DerivativePattern) np).source.getClass())
                    {
                        //So now we have a derivative object that is not the same class (take it?)
                        PlotTwist pt = new PlotTwist();

                        pt.core_pattern = derivative;
                        pt.addNarrativePattern(np);
                        pt.addSubgraph(((DerivativePattern) np).pattern_subgraphs.get(0));

                        results.add(pt);
                    }
                }
            }
            else if(np instanceof ActivePlotDevice) //When using plot devices we are interesting in plot devices that takes us to another plot device!!!
            {
                PlotDevicePattern pdp = ((ActivePlotDevice) np).device;

                if(pdp.connected_patterns_from_me.containsKey(0))
                {
                    for(NarrativePattern from_me : pdp.connected_patterns_from_me.get(0))
                    {
                        if(from_me.getClass() == pdp.getClass())
                        {
                            PlotTwist pt = new PlotTwist();

                            pt.core_pattern = np;
                            pt.addNarrativePattern(np);
                            pt.addSubgraph(((ActivePlotDevice) np).pattern_subgraphs.get(0));

                            results.add(pt);
                        }
                    }
                }

                if(pdp.connected_patterns_from_me.containsKey(1))
                {
                    for(NarrativePattern from_me : pdp.connected_patterns_from_me.get(1))
                    {
                        if(from_me.getClass() == pdp.getClass())
                        {
                            PlotTwist pt = new PlotTwist();

                            pt.core_pattern = np;
                            pt.addNarrativePattern(np);
                            pt.addSubgraph(((ActivePlotDevice) np).pattern_subgraphs.get(0));

                            results.add(pt);
                        }
                    }
                }
            }
        }

        return results;
    }
}
package game.narrative.NarrativeFinder;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import game.Room;
import game.narrative.GrammarGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Plotpoints are not specifically patterns, but more a product of the pattern and they are formally described as:
 * Pp = {E <= Dd & RevP & PlotDevices} : Where E is the SUM of all the partial-ordered DerivatePattern derivates (not the source),
 * & Target of Reveal Pattern & Plot Devices.
 *
 * Pp can be ordered within a set (a derivative) but does not show any other temporal relation.
 * This is due because derivatives have by design TEMPORAL and CAUSAL relation to the source and to each other.
 */
public class PlotPoint extends CompositeNarrativePattern
{
    // Plot points will be saved individually (then they can be assessed if happening or not) but also here as an ordered list.
//    public ArrayList<NarrativePattern> order = new ArrayList<NarrativePattern>();
//
//    public

    public NarrativePattern core_pattern;

    public PlotPoint()
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
            if(np instanceof DerivativePattern)
            {
                PlotPoint[] pps = new PlotPoint[((DerivativePattern) np).derivatives.size()];
                GrammarGraph temp = new GrammarGraph();

                // Gather first all the derivates for plot points
                for(int i = 0; i < pps.length; ++i)
                {
                    pps[i] = new PlotPoint();
                    NarrativePattern der = ((DerivativePattern) np).derivatives.get(i);
                    temp.addNode(der.connected_node, false); //Build subgraph
                    pps[i].connected_node = der.connected_node;
                    pps[i].core_pattern = der;
                }

                //set the plot points together and add to result!
                for(int i = 0; i < pps.length; ++i)
                {
//                    pps[i] = new PlotPoint();
                    pps[i].addSubgraph(temp);
                    pps[i].addNarrativePattern(pps);
                    pps[i].connected_node = np.connected_node;
                    results.add(pps[i]);
                }
            }
            else if(np instanceof RevealPattern) // The reveal pattern itself is the plotpoint and plottwist!
            {
                GrammarGraph temp = new GrammarGraph();

                PlotPoint pp = new PlotPoint();
                pp.core_pattern = np;
                pp.addSubgraph(((RevealPattern) np).pattern_subgraphs.get(0));
                pp.addNarrativePattern(((RevealPattern) np).source);
                pp.addNarrativePattern(((RevealPattern) np).target);

                pp.connected_node = np.connected_node;
                results.add(pp);
            }
            //TODO: Missing Plot Devices
            else if(np instanceof ActivePlotDevice)
            {
                GrammarGraph temp = new GrammarGraph();
                PlotPoint pp = new PlotPoint();

                pp.core_pattern = np;
                pp.addSubgraph(((ActivePlotDevice) np).pattern_subgraphs.get(0));

                pp.connected_node = np.connected_node;
                results.add(pp);
            }
        }

        return results;
    }

    /**
     * Rather than calculating a generic quality for the tropes, I would prefer a generic part and a specific one (based on the node trope!)
     * @param room
     * @return
     */
    public double calculateTropeQuality(Room room, GrammarGraph current, GrammarGraph core, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
//        double generic_quality = super.calculateTropeQuality(room, current, core, currentPatterns, finder);

        double generic_quality = 1.0;

        ArrayList<PlotPoint> all_plotpoints = finder.getAllPatternsByType(PlotPoint.class);

        //Then we want to know the distribution of the explicit conflicts!
        if(core != null) {
            ArrayList<NarrativePattern> core_narrative_patterns = core.pattern_finder.findNarrativePatterns(null);
            ArrayList<PlotPoint> other_plotpoints = core.pattern_finder.getAllPatternsByType(PlotPoint.class);

            //I don't know if 0.0 should be the right one
            if (other_plotpoints.isEmpty()) {
                generic_quality = 0.0;
            } else {

                generic_quality = all_plotpoints.size() <= other_plotpoints.size() ?
                        (double) all_plotpoints.size() / (double) other_plotpoints.size() :
                        2.0 - (double) all_plotpoints.size() / (double) other_plotpoints.size();
            }
        }

        //Calculate the quality of the plot points based on how many there are in relation with how many node exist!
        double quantity_quality = (double)all_plotpoints.size()/currentPatterns.size();

        //Now lets calculate the final quality
        this.quality = (generic_quality + quantity_quality)/2.0;

        return this.quality;
    }
}
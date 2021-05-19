package game.narrative.NarrativeFinder;

import game.Room;
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

                        pt.core_pattern = np;
                        pt.addNarrativePattern(derivative);
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


    /**
     * Rather than calculating a generic quality for the tropes, I would prefer a generic part and a specific one (based on the node trope!)
     * @param room
     * @return
     */
    public double calculateTropeQuality(Room room, GrammarGraph current, GrammarGraph core, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        // ok ok, First the amount in axiom
        double generic_quality = 1.0;

        ArrayList<PlotTwist> all_pt = finder.getAllPatternsByType(PlotTwist.class);

        //Generic bell curve
        if(core != null) //I feel that in this case this one should be weighted down!
        {
            ArrayList<NarrativePattern> core_narrative_patterns = core.pattern_finder.findNarrativePatterns(null);
            ArrayList<PlotTwist> other_pt = core.pattern_finder.getAllPatternsByType(PlotTwist.class);

            //I don't know if 0.0 should be the right one
            if(other_pt.isEmpty())
            {
                generic_quality = 0.0;
            }
            else {
                generic_quality = all_pt.size() <= other_pt.size() ?
                        (double) all_pt.size() / (double) other_pt.size() :
                        2.0 - (double) all_pt.size() / (double) other_pt.size();
            }
        }

        /**
         * Now we should calculate "INTERESTING QUALITY":
         * - The degree of how "good" or big the plot twist will be is define d by the involvement
         * the "from" node has on the structure.
         *  - When the PT is a reveal, the interest is on how the source node is affecting the structure
         *    (i.e. How much the structure changes with the twist!)
         *  - When the PT is in the derivative, the interest is on how different the element is.
         *  - When the PT is a Plot Device, We are interested in how it will affect the narrative and how ewell
         *    developed it is. Especially interesting, when the plot device has interaction with another plot twist!
         */

        double interesting_quality = 0.0;

        if(core_pattern instanceof RevealPattern)
        {
            ArrayList<SimpleConflictPattern> all_explicit_conflicts = finder.getAllPatternsByType(SimpleConflictPattern.class);
            double fake_conf_participation = 0.0;

            for(SimpleConflictPattern scp : all_explicit_conflicts) //Interested in both which are fake
            {
                if(scp.fake_conflict &&
                  (scp.getSource() == ((RevealPattern) core_pattern).source || scp.getTarget() == ((RevealPattern) core_pattern).target))
                {
                    fake_conf_participation++;
                }
            }
            //The moar, da betta
            interesting_quality = all_explicit_conflicts.isEmpty() ? 0.0 : fake_conf_participation/all_explicit_conflicts.size();
        }
        else if(core_pattern instanceof DerivativePattern)
        {
            NarrativePattern np = this.patterns.get(0);

            // The latter, the better
            int pt_index = ((DerivativePattern) core_pattern).derivatives.indexOf(np);
            interesting_quality = (double)(pt_index + 1)/((DerivativePattern) core_pattern).derivatives.size();

            if(((DerivativePattern) core_pattern).derivatives.size() != 1)
            {
                if (pt_index != 0 && pt_index != ((DerivativePattern) core_pattern).derivatives.size() - 1) {
                    NarrativePattern bef_np = ((DerivativePattern) core_pattern).derivatives.get(pt_index - 1);
                    NarrativePattern after_np = ((DerivativePattern) core_pattern).derivatives.get(pt_index + 1);

                    interesting_quality += bef_np.getClass() != np.getClass() ? 0.5 : 0;
                    interesting_quality += after_np.getClass() != np.getClass() ? 0.5 : 0;
                } else if (pt_index == 0) {
                    //                NarrativePattern bef_np = ((DerivativePattern) core_pattern).derivatives.get(pt_index -1);
                    NarrativePattern after_np = ((DerivativePattern) core_pattern).derivatives.get(pt_index + 1);

                    //                interesting_quality += bef_np.getClass() !=  np.getClass() ? 0.5 : 0;
                    interesting_quality += after_np.getClass() != np.getClass() ? 0.5 : 0;
                } else //at the max pos!
                {
                    NarrativePattern bef_np = ((DerivativePattern) core_pattern).derivatives.get(pt_index - 1);
                    //                NarrativePattern after_np = ((DerivativePattern) core_pattern).derivatives.get(pt_index +1);

                    interesting_quality += bef_np.getClass() != np.getClass() ? 0.5 : 0;
                    //                interesting_quality += after_np.getClass() !=  np.getClass() ? 0.5 : 0;
                }
            }

            interesting_quality = interesting_quality/2.0; //TODO: Not fair against one derivative patterns?
        }
        else if(core_pattern instanceof ActivePlotDevice)
        {
            PlotDevicePattern pdp = ((ActivePlotDevice) core_pattern).device;

            //Usability quality (am I connected, and how many are connected to me! - in comparison to the amount of nodes)
            interesting_quality = pdp.to_me_count <= (current.nodes.size()/2) ?
                    (double)pdp.to_me_count/((double)current.nodes.size()/2) :
                    2.0 - (double)pdp.to_me_count/((double)current.nodes.size()/2);

            if(pdp.connected_patterns_from_me.containsKey(0))
            {
                interesting_quality += 0.5; //Magic number!
                NarrativePattern connected_pat = pdp.connected_patterns_from_me.get(0).get(0);
                ArrayList<NarrativePattern> instances =  finder.getAllInstances(connected_pat.connected_node);

                for(NarrativePattern np : instances)
                {
                    if(np instanceof PlotTwist)
                        interesting_quality += 0.5; //Magic number!
                }
            }
            else if(pdp.connected_patterns_from_me.containsKey(1))
            {
                interesting_quality += 0.5; //Magic number!
                NarrativePattern connected_pat = pdp.connected_patterns_from_me.get(1).get(0);
                ArrayList<NarrativePattern> instances =  finder.getAllInstances(connected_pat.connected_node);

                for(NarrativePattern np : instances)
                {
                    if(np instanceof PlotTwist)
                        interesting_quality += 0.5; //Magic number!
                }
            }

            interesting_quality = interesting_quality/2.0;
        }

        /**
         * Then we should evaluate what is the balance of plot twists. If there are more than a handful, the story probably
         * deteriorates as well
         *
         * Actually, is better to compare with the plot points!???
         */

        ArrayList<PlotPoint> all_pp = finder.getAllPatternsByType(PlotPoint.class);

        double coherence_quality = all_pt.size() <= (all_pp.size()/2) ?
                (double)all_pt.size()/((double)all_pp.size()/2) :
                2.0 - (double)all_pt.size()/((double)all_pp.size()/2);

//        double coherence_quality = all_pt.size() <= current.nodes.size() ?
//                (double)pdp.to_me_count/(double)current.nodes.size() :
//                2.0 - (double)pdp.to_me_count/(double)current.nodes.size();

        //Now lets calculate the final quality
        this.quality = (generic_quality + interesting_quality + coherence_quality)/3.0;

        return this.quality;
    }
}
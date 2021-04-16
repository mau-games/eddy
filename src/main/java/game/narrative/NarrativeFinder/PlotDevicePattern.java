package game.narrative.NarrativeFinder;

import game.Room;
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


    /**
     * Rather than calculating a generic quality for the tropes, I would prefer a generic part and a specific one (based on the node trope!)
     * @param room
     * @return
     */
    public double calculateTropeQuality(Room room, GrammarGraph current, GrammarGraph core, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        double generic_quality = super.calculateTropeQuality(room, current, core, currentPatterns, finder);

        ArrayList<PlotDevicePattern> all_plot_devices = finder.getAllPatternsByType(PlotDevicePattern.class);

        //if generic Hero, it already starts in disadvantage
        double quantity_quality = this.connected_node.getGrammarNodeType() == TVTropeType.PLOT_DEVICE ? 0.75 : 1.0;
        double same_plot_devices = 0.0;

        for(PlotDevicePattern other_plot_devices : all_plot_devices)
        {
            if(other_plot_devices.connected_node.getGrammarNodeType() == this.connected_node.getGrammarNodeType())
            {
                same_plot_devices++;
            }
        }

        //If it is only one, is good! (no repetition) TODO: to test this!
        if(same_plot_devices != 1)
            quantity_quality = Math.max(0.0, quantity_quality - same_plot_devices/(double)all_plot_devices.size());


        //Now lets calcualte the final quality, Amount in axiom and repetition (not good to have 2 mcg)
        this.quality = (generic_quality + quantity_quality)/2.0;

        return this.quality;
    }
}
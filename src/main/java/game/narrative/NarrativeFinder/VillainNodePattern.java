package game.narrative.NarrativeFinder;

import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import game.narrative.TVTropeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VillainNodePattern extends BasicNarrativePattern
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

    /**
     * Rather than calculating a generic quality for the tropes, I would prefer a generic part and a specific one (based on the node trope!)
     * @param room
     * @return
     */
    public double calculateTropeQuality(Room room, GrammarGraph current, GrammarGraph core, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        double generic_quality = super.calculateTropeQuality(room, current, core, currentPatterns, finder);

        ArrayList<VillainNodePattern> all_villains = finder.getAllPatternsByType(VillainNodePattern.class);
        ArrayList<SimpleConflictPattern> all_explicit_conflicts = finder.getAllPatternsByType(SimpleConflictPattern.class);
        ArrayList<NarrativePattern> this_pattern_involvement = finder.getAllInstances(this.connected_node);

        //if generic Hero, it already starts in disadvantage
        double quantity_quality = this.connected_node.getGrammarNodeType() == TVTropeType.ENEMY ? 0.75 : 1.0;
        double same_villains = 0.0;

        for(VillainNodePattern other_villain : all_villains)
        {
            if(other_villain.connected_node.getGrammarNodeType() == this.connected_node.getGrammarNodeType())
            {
                same_villains++;
            }
        }

        if(same_villains != 1)
            quantity_quality = Math.max(0.0, quantity_quality - same_villains/(double)all_villains.size());

        //Now we calculate involvement in conflicts! specifically, in explicit conflicts!!
        double involves = 0.0;
        for(NarrativePattern involvements : this_pattern_involvement)
        {
            if(involvements instanceof SimpleConflictPattern)
                involves++;
        }

        double involvement_quality = all_explicit_conflicts.isEmpty() ? 0.0 : involves/(double)all_explicit_conflicts.size();

        //Now lets calcualte the final quality
        this.quality = (generic_quality + quantity_quality + involvement_quality)/3.0;

        return this.quality;
    }

    /**
     * Returns a measure of the quality of this pattern
     *
     * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
     */
    public double calculateQuality(List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
//        for(Map.Entry<Integer, List<NarrativePattern>> keyValue : this.connected_patterns_from_me.entrySet())
//        {
//            System.out.println("This VILLAIN node has: " + keyValue.getValue().size() + " narrative patterns connected with type: " + keyValue.getKey());
//        }

        // At the moment we do not really care about non-directional connections
        for(Map.Entry<Integer, List<NarrativePattern>> keyValue : this.connected_patterns_from_me.entrySet())
        {
            if(keyValue.getKey() == 0)
                continue;

            // At the moment it is not good to be connected only to anything that it is not a structure node!
            for(NarrativePattern np : keyValue.getValue())
            {
                if(!(np instanceof StructureNodePattern))
                {
                    //Houston, we got a problem!
//                    System.out.println("This " + this.getClass().getName() + " is connected to " + np.getClass().getSimpleName());
                }
            }
        }

        return quality;
    }

    
}

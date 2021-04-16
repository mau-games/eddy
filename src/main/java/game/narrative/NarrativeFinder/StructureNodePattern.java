package game.narrative.NarrativeFinder;

import finder.geometry.Geometry;
import finder.patterns.Pattern;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import game.narrative.TVTropeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StructureNodePattern extends BasicNarrativePattern
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

    /**
     * Rather than calculating a generic quality for the tropes, I would prefer a generic part and a specific one (based on the node trope!)
     * At the moment we only have conflict as structure node.
     * @param room
     * @return
     */
    public double calculateTropeQuality(Room room, GrammarGraph current, GrammarGraph core, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        double generic_quality = super.calculateTropeQuality(room, current, core, currentPatterns, finder);

        ArrayList<StructureNodePattern> all_structures = finder.getAllPatternsByType(StructureNodePattern.class);
        ArrayList<SimpleConflictPattern> all_explicit_conflicts = finder.getAllPatternsByType(SimpleConflictPattern.class);
        ArrayList<NarrativePattern> this_pattern_involvement = finder.getAllInstances(this.connected_node);

        //Now we calculate involvement in conflicts! specifically, in explicit conflicts!!
        double involves = 0.0;
        for(NarrativePattern involvements : this_pattern_involvement)
        {
            if(involvements instanceof SimpleConflictPattern)
                involves++;
        }

        double involvement_quality = involves/(double)all_explicit_conflicts.size();

        if(involvement_quality == 0)
        {
            this.quality = 0.0;
            return quality;
        }

        ////For now the quality of the conflict is how many times it is used to compose simple conflicts and the axiom!
        this.quality = (generic_quality + involvement_quality)/2.0;

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
//            System.out.println("This CONFLICT node has: " + keyValue.getValue().size() + " narrative patterns connected with type: " + keyValue.getKey());
//        }

        for(Map.Entry<Integer, List<NarrativePattern>> keyValue : this.connected_patterns_from_me.entrySet())
        {
//            // At the moment we do not really care about non-directional connections (but maybe we do for structure nodes)
//            if(keyValue.getKey() == 0)
//                continue;

            // At the moment it is only bad to be connected to another structure node! - probably will change :)
            for(NarrativePattern np : keyValue.getValue())
            {
                if(np instanceof StructureNodePattern)
                {
                    //Houston, we got a problem!
//                    System.out.println("This " + this.getClass().getName() + " is connected to " + np.getClass().getSimpleName());
                }
            }
        }

        return quality;
    }
}

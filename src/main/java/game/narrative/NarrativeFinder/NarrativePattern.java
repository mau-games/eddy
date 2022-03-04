package game.narrative.NarrativeFinder;

import finder.geometry.Geometry;
import finder.patterns.Pattern;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NarrativePattern
{
    protected double quality;
    protected GrammarNode connected_node;
    protected HashMap<Integer, List<NarrativePattern>> connected_patterns; // Mainly for the Basic Patterns
    protected HashMap<Integer, List<NarrativePattern>> connected_patterns_from_me; // Mainly for the Basic Patterns

    protected int from_me_count = 0;
    protected int to_me_count = 0;

    protected boolean derivative = false;

    public NarrativePattern revealed = null;
    public boolean ambiguous = false;
    public boolean faction = false;

    public NarrativePattern()
    {
        connected_patterns = new HashMap<Integer, List<NarrativePattern>>();
        connected_patterns_from_me  = new HashMap<Integer, List<NarrativePattern>>();
    }

    /**
     * Searches a map for instances of this pattern and returns a list of found
     * instances.
     *
     * @param narrative_graph the current graph we are finding
     * @return A list of found instances.
     */
    public static List<NarrativePattern> matches(GrammarGraph narrative_graph) {
        return new ArrayList<NarrativePattern>();
    }

    /**
     * Returns a measure of the quality of this pattern
     *
     * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
     */
    public double getQuality(){
        return quality;
    }

    /**
     * Returns a measure of the quality of this pattern
     *
     * @return A number between 0.0 and 1.0 representing the quality of the pattern (where 1 is best)
     */
    public double calculateQuality(List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder){
        return quality;
    }

    public double calculateTropeQuality(Room room, GrammarGraph current, GrammarGraph core, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        //This method is basically to knmow the generic quality based on what is the designer creating!

        this.quality = 1.0;
        if(core == null)
            return quality;

        if(this.connected_node == null)
        {
            System.out.println("STOP RIGHT HERE!");
        }

        float node_amount = core.checkGenericAmountNodes(this.connected_node.getGrammarNodeType().getGeneric(), false); //how many are the target
        ArrayList<NarrativePattern> all_same_class = finder.getAllPatternsByType((Class<NarrativePattern>) this.getClass()); //how many we are

        if(node_amount == 0)
        {
            quality = 0.0;
        }
        else
        {
            quality = all_same_class.size() <= node_amount ? all_same_class.size()/node_amount : 2.0 - all_same_class.size()/node_amount;
        }

        return quality;
    }
}

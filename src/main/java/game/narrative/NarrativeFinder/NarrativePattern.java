package game.narrative.NarrativeFinder;

import finder.geometry.Geometry;
import finder.patterns.Pattern;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.List;

public class NarrativePattern
{
    protected double quality;
    protected GrammarNode connected_node;

    public NarrativePattern()
    {

    }

    /**
     * Searches a map for instances of this pattern and returns a list of found
     * instances.
     *
     * @param room The map to search for patterns in.
     * @param boundary A boundary in which the pattern is searched for.
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
}

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

    public NarrativePattern()
    {
        connected_patterns = new HashMap<Integer, List<NarrativePattern>>();
        connected_patterns_from_me  = new HashMap<Integer, List<NarrativePattern>>();
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
    public double getQuality(List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder){
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
}

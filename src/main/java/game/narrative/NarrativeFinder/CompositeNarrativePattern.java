package game.narrative.NarrativeFinder;

import finder.geometry.Geometry;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import game.Room;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeNarrativePattern extends NarrativePattern
{
    protected List<NarrativePattern> patterns = new ArrayList<NarrativePattern>();
    protected List<GrammarNode> relevant_nodes;

    //But actually, it might be smarter to create subgraphs as we do to apply the rules
    protected List<GrammarGraph> pattern_subgraphs;

    public CompositeNarrativePattern()
    {
        pattern_subgraphs = new ArrayList<GrammarGraph>();
        relevant_nodes = new ArrayList<GrammarNode>();
    }

    public void addSubgraph(GrammarGraph subgraph)
    {
        pattern_subgraphs.add(subgraph);
        relevant_nodes.addAll(subgraph.nodes);
    }

    /**
     * Given that this is a Composite Pattern, we need to add the narrative patterns that compose this pattern
     * e.g., for a SimpleConflictPattern, the patterns would be the Conflict Pattern, and Hero/Enemy Pattern
     * for a CompoundConflictPattern, the patterns would be the set of SimpleConflictPatterns.
     * @param np
     */
    public void addNarrativePattern(NarrativePattern... np)
    {
//        System.out.println(np.length);
        try
        {
            patterns.addAll(Arrays.asList(np));
        }
        catch(Exception e)
        {
            System.out.println("Something wrong!");
        }
        catch (OutOfMemoryError e){
            System.out.println("OUT OF MEMORY!");
        }
    }
//
//    public void addNarrativePattern(NarrativePattern np...)
//    {
//        patterns.add(np);
//    }

    /**
     * Returns a list of all pattern instances making up this pattern.
     *
     * @return A list of patterns.
     */
    public List<NarrativePattern> getPatterns(){
        return patterns;
    }

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        return results;
    }
}

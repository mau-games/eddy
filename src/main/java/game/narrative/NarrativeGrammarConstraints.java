package game.narrative;

import java.util.ArrayList;
import java.util.List;

/**
 * The idea of this class is to set constraints (in the shape of expert knowledge) to the narrative; but what constraints?:
 * 1. A conflict node can only have ONE BIDIRECTIONAL connection
 * 2. There can only be ONE conflict between factions (to avoid having repetition) //This one is a bit more complex
 * 3. No direct connection between entities?
 * 4. No NOTHING patterns
 */
public class NarrativeGrammarConstraints
{
    List<GrammarPattern> constraints;

    public NarrativeGrammarConstraints()
    {
        constraints = new ArrayList<GrammarPattern>();

        //Set first pattern
        GrammarPattern gp = new GrammarPattern();
        GrammarGraph gpp = new GrammarGraph();
    }
}

package game.narrative.NarrativeFinder;

import game.Room;
import game.narrative.GrammarGraph;
import sun.java2d.pipe.SpanShapeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Reveal pattern is formally described as:
 * Rp = {H,E(!:)} or {E,H(!:)}, where H is hero, E is Enemy, "," is an uni-directional connection,
 * and ":" is non-directional connection, and (!:) means that the TO connection has no other connection.
 * When a hero is connected
 */
public class RevealPattern extends CompositeNarrativePattern
{
    public NarrativePattern source;
    public NarrativePattern target;

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();

        /**
         * So the idea here is to detect all the
         */

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof HeroNodePattern && np.connected_patterns_from_me.containsKey(1))
            {
                //Now lets check for directional connections from me (Entitles!)
                for(NarrativePattern non_directed_pat : np.connected_patterns_from_me.get(1))
                {
                    if(non_directed_pat instanceof VillainNodePattern && !non_directed_pat.connected_patterns_from_me.containsKey(0))
                    {
                        RevealPattern rp = new RevealPattern();
                        GrammarGraph temp = new GrammarGraph();

                        rp.addNarrativePattern(np);
                        rp.addNarrativePattern(non_directed_pat);
                        temp.addNode(np.connected_node, false);
                        temp.addNode(non_directed_pat.connected_node, false);
                        rp.addSubgraph(temp);
                        rp.source = np;
                        rp.target = non_directed_pat;
                        rp.connected_node = np.connected_node;
                        results.add(rp);


                    }
                }
            }
            else if(np instanceof VillainNodePattern && np.connected_patterns_from_me.containsKey(1))
            {
                //Now lets check for directional connections from me (Entitles!)
                for(NarrativePattern non_directed_pat : np.connected_patterns_from_me.get(1))
                {
                    if(non_directed_pat instanceof HeroNodePattern && !non_directed_pat.connected_patterns_from_me.containsKey(0))
                    {
                        RevealPattern rp = new RevealPattern();
                        GrammarGraph temp = new GrammarGraph();

                        rp.addNarrativePattern(np);
                        rp.addNarrativePattern(non_directed_pat);
                        temp.addNode(np.connected_node, false);
                        temp.addNode(non_directed_pat.connected_node, false);
                        rp.addSubgraph(temp);
                        rp.source = np;
                        rp.target = non_directed_pat;
                        rp.connected_node = np.connected_node;

                        results.add(rp);
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

        ArrayList<RevealPattern> all_reveal = finder.getAllPatternsByType(RevealPattern.class);

        //Generic bell curve
        if(core != null) //I feel that in this case this one should be weighted down!
        {
            ArrayList<NarrativePattern> core_narrative_patterns = core.pattern_finder.findNarrativePatterns(null);
            ArrayList<RevealPattern> other_reveals = core.pattern_finder.getAllPatternsByType(RevealPattern.class);
            generic_quality = all_reveal.size() <= other_reveals.size() ?
                    (double)all_reveal.size()/(double)other_reveals.size() :
                    2.0 - (double)all_reveal.size()/(double)other_reveals.size();
        }

        //Now we should calculate the relation to effective characters (Still at a general level)
        // - Effective characters do not count the sources. -- This relates to how interesting the story is? Lower score means not too interesting!
        // - Because there are too many reveals! (but probably also something for the plot twist thingy)
        // Or also, how many conflicts are fake due to reveal? thats a good idea.

        //First effective!
        ArrayList<SimpleConflictPattern> all_explicit_conflicts = finder.getAllPatternsByType(SimpleConflictPattern.class);
        ArrayList<SimpleConflictPattern> fake_conflicts = new ArrayList<SimpleConflictPattern>();
//        ArrayList<NarrativePattern> active_characters = new ArrayList<NarrativePattern>();

        ArrayList<HeroNodePattern> heroes = finder.getAllPatternsByType(HeroNodePattern.class);
        ArrayList<VillainNodePattern> villains = finder.getAllPatternsByType(VillainNodePattern.class);

        //Only bad if there are more reveals than half of the characters
        double quality_character_relation = all_reveal.size() > (heroes.size() + villains.size() - all_reveal.size())/2 ?
                1.0 - all_reveal.size() / (double)(heroes.size() + villains.size() - all_reveal.size()) :
                1.0;

        for(SimpleConflictPattern scp : all_explicit_conflicts) //Interested in both which are fake
        {
            if(scp.fake_conflict)
                fake_conflicts.add(scp);

//            if(!active_characters.contains(scp.source_pattern))
//                active_characters.add(scp);
//
//            if(!active_characters.contains(scp.target_pattern))
//                active_characters.add(scp);

        }

        double fake_conf_participation = 0.0;

        //This might not be necessary faktist
        for(SimpleConflictPattern fake_conflict : fake_conflicts)
        {
            if(fake_conflict.getSource() == this.source || fake_conflict.getTarget() == this.source)
            {
                //Then we know we are part of fake encounters!
                fake_conf_participation++;
            }
        }

        double quality_reveal_struct = 1.0 - fake_conf_participation/all_explicit_conflicts.size();

//
//
//        for(NarrativePattern np : active_characters)
//        {
//            if(np == this.source)
//
//        }


        //Now lets calculate the final quality
        this.quality = (generic_quality + quality_character_relation + quality_reveal_struct)/3.0;

        return this.quality;
    }
}
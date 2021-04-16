package game.narrative.NarrativeFinder;

import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implicit Conflict pattern is formally described as:
 * SCp = {S, C, T} where S is the source that have a conflict, C is the conflict itself, and T is the target of the conflict
 * I am thinking, and actually S and T might not be positive if they are not
 * Implicit Conflict also checks for fake and real conflicts!!
 */
public class ImplicitConflictPattern extends CompositeNarrativePattern
{
    protected NarrativePattern source_pattern;
    protected NarrativePattern target_pattern;
    public boolean fake_conflict = false;

    public ImplicitConflictPattern(){super();}

    public ImplicitConflictPattern(NarrativePattern source, NarrativePattern target, GrammarNode conflict_node)
    {
        super();
        setSource(source);
        setTarget(target);
        addNarrativePattern(source);
//        addNarrativePattern(conflict_node);
        addNarrativePattern(target);

        this.connected_node = conflict_node;
    }

    public void setSource(NarrativePattern sp) {source_pattern = sp;}
    public void setTarget(NarrativePattern tp) {target_pattern = tp;}

    public NarrativePattern getSource() {return source_pattern;}
    public NarrativePattern getTarget() {return target_pattern;}

    public boolean isSelfConflict()
    {
        // If source and target are equals we know this is a bidirectional connection to conflict, which means selfconflict
        return source_pattern.equals(target_pattern);
    }

    public static List<CompositeNarrativePattern> matches(GrammarGraph narrative_graph, List<NarrativePattern> currentPatterns, NarrativeStructPatternFinder finder)
    {
        ArrayList<CompositeNarrativePattern> results = new ArrayList<CompositeNarrativePattern>();
        ArrayList<ImplicitConflictPattern> resultsImplicit = new ArrayList<ImplicitConflictPattern>();

        /** So this one is a bit harder to identify
         *  1- First, lets go through each of there StructureNodePatterns encountered thus far
         *  2- Then we should iterate the connections of those nodes trying to find directions
         *  3- a basic concli
         */

        for(NarrativePattern np : currentPatterns)
        {
            if(np instanceof SimpleConflictPattern)
            {
                if(((SimpleConflictPattern) np).isSelfConflict()) //not interested in self conflicts
                    continue;

                SimpleConflictPattern npConflict = (SimpleConflictPattern) np;
                NarrativePattern source = npConflict.getSource();
                NarrativePattern target = npConflict.getTarget();

                //Create target to source!
//                resultsImplicit.add(new ImplicitConflictPattern(target, source));

                ArrayList<NarrativePattern> source_derivatives = new ArrayList<NarrativePattern>();
                source_derivatives.add(source);
                ArrayList<NarrativePattern> target_derivatives = new ArrayList<NarrativePattern>();
                target_derivatives.add(target);

                //We collect possible derivatives
                for(NarrativePattern possible_derivative : currentPatterns)
                {
                    if(possible_derivative instanceof DerivativePattern)
                    {
                        if(((DerivativePattern) possible_derivative).source == source)
                        {
                            for(NarrativePattern derivative : ((DerivativePattern) possible_derivative).derivatives)
                            {
                                if(source instanceof HeroNodePattern && derivative instanceof HeroNodePattern)
                                {
                                    source_derivatives.add(derivative);
                                }
                                else if( source instanceof VillainNodePattern && derivative instanceof VillainNodePattern)
                                {
                                    source_derivatives.add(derivative);
                                }
                            }

                        }
                        else if(((DerivativePattern) possible_derivative).source == target)
                        {
                            for(NarrativePattern derivative : ((DerivativePattern) possible_derivative).derivatives)
                            {
                                if(target instanceof HeroNodePattern && derivative instanceof HeroNodePattern)
                                {
                                    target_derivatives.add(derivative);
                                }
                                else if( target instanceof VillainNodePattern && derivative instanceof VillainNodePattern)
                                {
                                    target_derivatives.add(derivative);
                                }
                            }
                        }
                    }
                }

                for(NarrativePattern sdp : source_derivatives)
                {
                    for(NarrativePattern tdp : target_derivatives)
                    {
                        boolean[] already_exist = new boolean[2];
                        for(ImplicitConflictPattern current_result : resultsImplicit)
                        {
                            if(current_result.getSource() == sdp && current_result.getTarget() == tdp)
                            {
                                already_exist[0] = true;
                            }
                            else if(current_result.getSource() == tdp && current_result.getTarget() == sdp)
                            {
                                already_exist[1] = true;
                            }
                        }

                        if(!already_exist[0])
                        {
                            resultsImplicit.add(new ImplicitConflictPattern(sdp, tdp, np.connected_node));
                        }

                        if(!already_exist[1])
                        {
                            resultsImplicit.add(new ImplicitConflictPattern(tdp, sdp, np.connected_node));
                        }

                    }
                }
            }
        }

        //Store reveals!
        ArrayList<RevealPattern> reveal_pats = new ArrayList<RevealPattern>();
        ArrayList<SimpleConflictPattern> simple_conflicts = new ArrayList<SimpleConflictPattern>();

        for(NarrativePattern np : currentPatterns)
        {
            if (np instanceof SimpleConflictPattern)
            {
                simple_conflicts.add((SimpleConflictPattern) np);

                boolean explicit = false;
                ImplicitConflictPattern bad_implicit = null;
                for(ImplicitConflictPattern current_result : resultsImplicit) //Now we need to know if the implicits belong already to explicits!
                {
                    if(current_result.getSource() == ((SimpleConflictPattern) np).getSource() &&
                            current_result.getTarget() == ((SimpleConflictPattern) np).getTarget())
                    {
                        explicit = true;
                        bad_implicit = current_result;
                    }
                }

                if(explicit)
                    resultsImplicit.remove(bad_implicit);
            }
            else if(np instanceof RevealPattern)
            {
                reveal_pats.add((RevealPattern) np);
            }

        }

        //Now lets just check if the conflicts are fake or real!
        for(RevealPattern rp : reveal_pats)
        {
            for(ImplicitConflictPattern icp : resultsImplicit)
            {
                if(icp.getSource() == rp.source || icp.getTarget() == rp.source)
                    icp.fake_conflict = true; //Needs to be checked!
            }

            for(SimpleConflictPattern scp : simple_conflicts)
            {
                if(scp.getSource() == rp.source || scp.getTarget() == rp.source)
                    scp.fake_conflict = true; //Needs to be checked!
            }
        }


        results.addAll(resultsImplicit);
        return results;
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
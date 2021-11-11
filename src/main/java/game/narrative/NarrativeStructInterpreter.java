package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import game.narrative.NarrativeFinder.NarrativePattern;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NarrativeStructInterpreter
{
//    List<String> plot_points;
//    List<String> plot_devices;
//    LinkedList<String> narrative_structure;

    ArrayList<NarrativePattern> heroes = new ArrayList<>();
    ArrayList<NarrativePattern> villains = new ArrayList<>();
    ArrayList<NarrativePattern> factions = new ArrayList<>();
    ArrayList<NarrativePattern> plot_points = new ArrayList<>();

//
//    ArrayList<NarrativePattern> heroes = new ArrayList<>();
//    ArrayList<NarrativePattern> heroes = new ArrayList<>();
//    ArrayList<NarrativePattern> heroes = new ArrayList<>();
//    ArrayList<NarrativePattern> heroes = new ArrayList<>();


    public NarrativeStructInterpreter()
    {

    }

    public void AnalyzeNarrativeGraph(GrammarGraph current_narrative, GrammarGraph axiom)
    {
        ArrayList<NarrativePattern> narrative_patterns = current_narrative.pattern_finder.findNarrativePatterns(axiom);


    }

    public String translateNarrative()
    {
        String translation = "";

        translation += "This narrative contains " + heroes.size() + "heroes";

        return translation;
    }


}

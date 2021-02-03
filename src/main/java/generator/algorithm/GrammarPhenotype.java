package generator.algorithm;

import game.Room;
import game.TileTypes;
import game.narrative.GrammarGraph;
import game.narrative.GrammarPattern;
import game.narrative.NarrativeStructure;
import generator.config.GeneratorConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrammarPhenotype
{
    public GrammarGenotype genotype;
    public NarrativeStructure narrative;
    private GeneratorConfig config;

    public GrammarPhenotype(GrammarGenotype genotype)
    {
        this.config = null;
        this.genotype = genotype;
    }
//
//    public GrammarPhenotype()
//    {
//
//    }

    public GrammarGenotype getGenotype() {
        return genotype;
    }

    /**
     * Generates a grammar graph, applies all the rules X amount of time sequentially from the genotype!
     * Needs t
     * Therefore, algorithm should know size and doors
     * @param GrammarGraph the core graph!
     * @return The Map for this Genotype
     */
    public GrammarGraph getGrammarGraphOutput(GrammarGraph coreGraph, int application) {

        GrammarGraph output = new GrammarGraph(coreGraph);
        List<GrammarPattern> patterns = this.genotype.getChromosome();

        for(int i = 0; i < application; i++)
        {
            for(GrammarPattern pattern : patterns)
            {
                pattern.match(output, 4);
            }
        }

        return output;
    }
}

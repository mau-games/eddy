package generator.algorithm;

import game.Room;
import game.TileTypes;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import game.narrative.GrammarPattern;
import game.narrative.NarrativeStructure;
import generator.config.GeneratorConfig;
import sun.awt.image.ImageWatched;
import util.Util;

import java.util.*;

public class GrammarPhenotype
{
    public GrammarGenotype genotype;
    public NarrativeStructure narrative;
    private GeneratorConfig config;

    public LinkedHashMap<Integer, Integer> grammar_recipe;
    public LinkedHashMap<Integer, Integer> current_rnd_recipe;

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

    public void setRecipe()
    {
        grammar_recipe = new LinkedHashMap<Integer, Integer>();
        for(Map.Entry<Integer, Integer> keyValue : current_rnd_recipe.entrySet())
        {
            grammar_recipe.put(keyValue.getKey(), keyValue.getKey());
        }
    }

    /**
     * Generates a grammar graph, applies all the rules X amount of time sequentially from the genotype!
     * Needs t
     * Therefore, algorithm should know size and doors
     * @param GrammarGraph the core graph!
     * @return The Map for this Genotype
     */
    public GrammarGraph getGrammarGraphOutputRndRecipe(GrammarGraph coreGraph, int application)
    {
        GrammarGraph output = new GrammarGraph(coreGraph);
        List<GrammarPattern> patterns = this.genotype.getChromosome();
        int min_size = patterns.size() * application;
        current_rnd_recipe = generateRNDRecipe(min_size, min_size + 5, patterns.size() - 1, false, false);

        for(Map.Entry<Integer, Integer> keyValue : current_rnd_recipe.entrySet())
        {
            for(int i = 0; i < keyValue.getKey(); i++)
            {
                patterns.get(keyValue.getKey()).match(output, 4);
            }
        }
//
//        for(int i = 0; i < application; i++)
//        {
//            for(GrammarPattern pattern : patterns)
//            {
//                pattern.match(output, 4);
//            }
//        }

        return output;
    }

    /**
     * Generates a grammar graph, applies all the rules X amount of time sequentially from the genotype!
     * Needs t
     * Therefore, algorithm should know size and doors
     * @param GrammarGraph the core graph!
     * @return The Map for this Genotype
     */
    public GrammarGraph getGrammarGraphOutputBest(GrammarGraph coreGraph, int application)
    {
        GrammarGraph output = new GrammarGraph(coreGraph);
        List<GrammarPattern> patterns = this.genotype.getChromosome();

        for(Map.Entry<Integer, Integer> keyValue : grammar_recipe.entrySet())
        {
            for(int i = 0; i < keyValue.getKey(); i++)
            {
                patterns.get(keyValue.getKey()).match(output, 4);
            }
        }

        return output;
    }

    /**
     * Generates a grammar graph, applies all the rules X amount of time sequentially from the genotype!
     * Needs t
     * Therefore, algorithm should know size and doors
     * @param GrammarGraph the core graph!
     * @return The Map for this Genotype
     */
    public GrammarGraph getGrammarGraphOutput(GrammarGraph coreGraph, int application)
    {
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

    /***
     *
     * @param min is usually the size of the patterns
     * @param max arbitrary number of maximum steps
     * @param size size of the patterns - 1
     * @param use_all if True, we must call at least once each pattern (not needed in that order!)
     * @param ordered if true, we must keep the order of the patterns (must be used with use_all)
     * @return
     */
    private LinkedHashMap<Integer, Integer> generateRNDRecipe(int min, int max, int size, boolean use_all, boolean ordered)
    {
        LinkedHashMap<Integer, Integer> current_recipe = new LinkedHashMap<Integer, Integer>();

        int rnd_size = Util.getNextInt(min, max);

        //if True, we make sure that all the different patterns that exist in this individual will be used!
        if(use_all)
        {
            List<Integer> list = new ArrayList<Integer>();

            for(int i = 0; i < min; i++)
            {
                list.add(i);
            }

            if(!ordered)
            {
                Collections.shuffle(list);
            }

            for(int i : list)
            {
                current_recipe.put(i, 1);
            }

            rnd_size = rnd_size - min; //extra steps
        }

        // We add randomly to the recipe.
        for(int i = 0; i < rnd_size; i++)
        {
            int index = Util.getNextInt(min, max);
            if(current_recipe.containsKey(index))
            {
                current_recipe.put(index, current_recipe.get(index) + 1);
            }
            else
            {
                current_recipe.put(index, 1);
            }
        }

        return current_recipe;
    }
}

package generator.algorithm;

import game.narrative.NarrativeStructure;
import generator.config.GeneratorConfig;

public class GrammarPhenotype
{
    public GrammarGenotype genotype;
    public NarrativeStructure narrative;
    private GeneratorConfig config;

    public GrammarPhenotype()
    {

    }

    public GrammarGenotype getGenotype() {
        return genotype;
    }
}

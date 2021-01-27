package generator.algorithm;

import game.narrative.GrammarGraph;
import game.narrative.GrammarPattern;
import generator.config.GeneratorConfig;
import util.Util;

import java.util.List;

public class GrammarGenotype
{
    private List<GrammarPattern> chromosome;
    private GeneratorConfig config;

    /**
     * Get chromosome
     *
     * @return Chromosome
     */
    public List<GrammarPattern> getChromosome(){
        return chromosome;
    }

    /**
     * Set chromosome
     *
     * @param chromosome Chromosome
     */
    public void setChromosome(List<GrammarPattern> chromosome){
        this.chromosome = chromosome;
    }

    /**
     * Get the length of the chromosome
     *
     * @return Chromosome length
     */
    public int getSizeChromosome(){
        return chromosome.size();
    }

    public GrammarGenotype()
    {
        produceRndChromosome();
    }

    public GrammarGenotype(List<GrammarPattern> chromosome)
    {
        this.chromosome = chromosome;
    }

    private void produceRndChromosome()
    {

    }

    public void addNodeRndRule()
    {
        GrammarPattern rndRule = chromosome.get(Util.getNextInt(0, this.chromosome.size() - 1));
        GrammarGraph pat = rndRule.pattern;



//        rndRule.
    }

    public void removeNodeRndRule() {
    }

    public void exchangeNodeRndRule() {
    }

    public void changeNodeTypeRndRule() {
    }

    public void addConnectionRndRule() {
    }

    public void removeConnectionRndRule() {
    }

    public void changeConnectionRndRule() {
    }

    public void addNodeRndOutput() {
    }

    public void removeNodeRndOutput() {
    }

    public void exchangeNodeRndOutput() {
    }

    public void changeNodeTypeRndOutput() {
    }

    public void addConnectionRndOutput() {
    }

    public void removeConnectionRndOutput() {
    }

    public void changeConnectionRndOutput() {
    }

    // THESE TWO ARE MORE HARDCORE
    public void createRule() {
    }

    public void createConnection() {
    }
}

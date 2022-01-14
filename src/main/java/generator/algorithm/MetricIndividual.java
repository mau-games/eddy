package generator.algorithm;

import finder.geometry.Point;
import game.Room;
import game.TileTypes;
import game.narrative.GrammarGraph;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.config.GeneratorConfig;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MetricIndividual
{
    // {addfunc, removefun, change func type, changefunc, changeweight}
    private int[] w_mutate = {3, 3, 4, 25, 65};

    private double fitness; //Fitness based on how well it is adapting the
    private double novelty;

    protected HashMap<GADimension.DimensionTypes, Double> dimensionValues; //not sure

    private MetricGenotype genotype;
    private MetricPhenotype phenotype;
    private boolean evaluate;
    private float mutationProbability;

    private boolean childOfInfeasibles = false;

    private Room editedRoom;

    public boolean isChildOfInfeasibles(){
        return childOfInfeasibles;
    }

    public void setChildOfInfeasibles(boolean cOI){
        childOfInfeasibles = cOI;
    }

//    public MetricIndividual(float mutationProbability) {
//        this(unll, new MetricGenotype(), mutationProbability);
//    }

    public MetricIndividual(Room room, float mutationProbability) {
        this(room, new MetricGenotype(), mutationProbability);
    }

    public MetricIndividual(Room room, MetricGenotype genotype, float mutationProbability){
        this.editedRoom = room;
        this.genotype = genotype;
        this.phenotype = null;
        this.fitness = 0.0;
        this.evaluate = false;
        this.mutationProbability = mutationProbability;
    }
//
//    public MetricIndividual(Room room, float mutationProbability)
//    {
//        config = room.getConfig();
//        genotype = new MetricGenotype(config, room.getColCount() * room.getRowCount());
//        phenotype = null;
//        fitness = 0.0;
//        evaluate = false;
//        this.mutationProbability = mutationProbability;
//
//        genotype.ProduceGenotype(room);
//    }

    /**
     * Generate a genotype
     *
     */
    public void initialize() {
        genotype = new MetricGenotype();
    }


    // This needs to vary!
//    public void SetDimensionValues(ArrayList<GADimension> dimensions, Room original)
//    {
//        dimensionValues = new HashMap<GADimension.DimensionTypes, Double>();
//
//        for(GADimension dimension : dimensions)
//        {
//            dimensionValues.put(dimension.GetType(), dimension.CalculateValue(this, original));
//        }
//
//        this.getPhenotype().getMap(-1, 1, null, null, null).SetDimensionValues(dimensionValues);
//    }
//
//    public double getDimensionValue(GADimension.DimensionTypes currentDimension)
//    {
//        return dimensionValues.get(currentDimension);
//    }

    public void BroadcastIndividualDimensions()
    {
        System.out.print("Metric fitness: " + getFitness());

        for (Map.Entry<GADimension.DimensionTypes, Double> entry : dimensionValues.entrySet())
        {
            System.out.print(", " + entry.getKey().toString() + ": " + entry.getValue());
        }

        System.out.println();
    }
    /**
     * Two point crossover between two individuals.
     *
     * @param other An Individual to reproduce with.
     * @return An array of offspring resulting from the crossover.
     */
    public MetricIndividual[] twoPointCrossover(MetricIndividual other){ //This is one way of doing crossover (Icould do others)

        if(this.getGenotype().equals(other.getGenotype()))
        {
            MetricIndividual[] children = new MetricIndividual[2];
            children[0] = mutate(true);
            children[1] = other.mutate(true);

            return children;
//            return null;
        }

        MetricIndividual[] children = new MetricIndividual[2]; //FIXME: Here needs some fix1
        children[0] = new MetricIndividual(this.editedRoom, new MetricGenotype(this.genotype.getChromosomes()), mutationProbability);
        children[1] = new MetricIndividual(other.editedRoom, new MetricGenotype(other.genotype.getChromosomes()), mutationProbability);

        //All of this is needed since we could have different genotype's sizes.
        int this_chromosome_size = children[0].getGenotype().getSizeChromosome();
        int other_chromosome_size = children[1].getGenotype().getSizeChromosome();
        int min_size = Math.min(this_chromosome_size, other_chromosome_size);
        int rnd_cut = Util.getNextInt(1, min_size + 1);
        //Ok with this we know where we could start
        int this_chromosome_maxcut = this_chromosome_size - rnd_cut;
        int other_chromosome_maxcut = other_chromosome_size - rnd_cut;
        //And now we know where to start!
        int this_chromosome_startpos = 0;
        int other_chromosome_startpos = 0;
        try{

            this_chromosome_startpos = this_chromosome_maxcut == 0 ? 0 : Util.getNextInt(0, this_chromosome_maxcut +1);
            other_chromosome_startpos = other_chromosome_maxcut == 0 ? 0 : Util.getNextInt(0, other_chromosome_maxcut +1);
        }
        catch(Exception e)
        {
            System.out.println("BOund must be positive!");
        }

        MetricChromosome[] this_metric_chromosomes = this.getGenotype().getMetricsToExchange(this_chromosome_startpos, rnd_cut);
        MetricChromosome[] other_metric_chromosomes = other.getGenotype().getMetricsToExchange(other_chromosome_startpos, rnd_cut);

        if(this_metric_chromosomes.length != other_metric_chromosomes.length)
            System.out.println("AGAIN SOMETHING BAD WITH THE EXCHANGE!");

        //Now SWAP!
        children[0].getGenotype().exchangeMetrics(this_chromosome_startpos, rnd_cut, other_metric_chromosomes);
        children[1].getGenotype().exchangeMetrics(other_chromosome_startpos, rnd_cut, this_metric_chromosomes);


        //Now test if we want to mutate these children!
        children[0] = children[0].mutate(false);
        children[1] = children[1].mutate(false);

//        int rnd_lower_bound = Util.getNextInt(0, children[0].getGenotype().getSizeChromosome());
//        int rnd_upper_bound = Util.getNextInt(rnd_lower_bound, children[0].getGenotype().getSizeChromosome());
//
//        for(int i = rnd_lower_bound; i <= rnd_upper_bound; i++)
//        {
//            children[0].getGenotype().getChromosomes().get(i) = other.getGenotype().getChromosomes().get(i);
//            children[1].getGenotype().getChromosomes().get(i) = this.getGenotype().getChromosomes().get(i);
//        }

        return children;
    }

    private int get_mutation_by_prob(int ... probability_table)
    {
        int index=Util.getNextInt(0, IntStream.of(probability_table).sum());
        int sum = 0;
        int i = 0;

        while(sum < index)
        {
            sum += probability_table[i++];
        }

        return i-1;
    }

    //Beautiful
    public MetricIndividual mutate(boolean test_mutation_prob)
    {
        //Check if mutate at all
        if(!test_mutation_prob || Util.getNextFloat(0.0f, 1.0f) > mutationProbability)
        {
            //For now just a random
//            int select=Util.getNextInt(0, 6);
//            GrammarIndividual mutated_version = new GrammarIndividual(config, new ZoneGenotype(config, genotype.getChromosome().clone(), genotype.GetRootChromosome()), mutationProbability);
            MetricIndividual mutated_version = new MetricIndividual(this.editedRoom, new MetricGenotype(this.genotype.getChromosomes()), mutationProbability);

            switch(get_mutation_by_prob(w_mutate))
            {
                case 0:
                    mutated_version.genotype.addFunc();
                    break;
                case 1:
                    mutated_version.genotype.removeFunc();
                    break;
                case 2:
                    mutated_version.genotype.alterFuncType();
                    break;
                case 3:
                    mutated_version.genotype.alterFunc();
                    break;
                case 4:
                    mutated_version.genotype.alterWeight();
            }

            return mutated_version;

        }
        else
            return this;
    }

    //TODO: WORKING ON THIS!
    public boolean infeasibilityCheck(ArrayList<MetricExampleRooms> examples)
    {
        boolean feasible = true;
        for(MetricExampleRooms example : examples)
        {
            if(example.positive)
            {
                double score = this.getPhenotype().createMetric().calculateMetric(example.room);
                if(Math.abs(score - example.metric_value) > 0.7)
                    return false;
            }
            else
            {
                double score = this.getPhenotype().createMetric().calculateMetric(example.room);
                if(Math.abs(score - example.metric_value) < 0.3)
                    return false;
            }

//            if(score >= example.granularity_value.getMaxValue() || score <= example.granularity_value.getMinValue() )
//            {
//                feasible = false;
//                return false;
//            }
        }

        return feasible;

//        return true;
//        boolean feasible = true;
//        for(MetricExampleRooms example : examples)
//        {
//            double score = this.getPhenotype().createMetric().calculateMetric(example.room);
//            if(score >= example.granularity_value.getMaxValue() || score <= example.granularity_value.getMinValue() )
//            {
//                feasible = false;
//                return false;
//            }
//        }
//
//        return feasible;
    }

    /**
     * Get this individual's calculated fitness
     *
     * @return Fitness
     */
    public double getFitness(){
        return fitness;
    }

    /**
     * Set this individual's fitness
     *
     * @param fitness Fitness
     */
    public void setFitness(double fitness){
        this.fitness = fitness;
    }


    public double getNovelty() {
        return novelty;
    }

    public void setNovelty(double novelty) {
        this.novelty = novelty;
    }

    /**
     * Has the fitness of this Individual been evaluated yet?
     *
     * @return true if the fitness of this individual has already been evaluated
     */
    public boolean isEvaluated() {
        return evaluate;
    }

    /**
     * Set that this Individual has been evaluated.
     *
     * @param evaluate true if the fitness of this Individual has been evaluated
     */
    public void setEvaluate(boolean evaluate){
        this.evaluate = evaluate;
    }

    /**
     * Get genotype
     *
     * @return Genotype
     */
    public MetricGenotype getGenotype(){
        return genotype;
    }

    /**
     * Get phenotype
     *
     * @return Phenotype
     */
    public MetricPhenotype getPhenotype(){
        if(phenotype == null){
            phenotype = new MetricPhenotype(genotype);
        }
        return phenotype;
    }

    /*
     * create once again the phenotype
     * This operation can be very costly!
     */
    public void ResetPhenotype()
    {
        phenotype = null;
    }

    public boolean equals(MetricIndividual other, ArrayList<MetricExampleRooms> examples)
    {
        for(MetricExampleRooms example : examples)
        {
            double this_metric = this.getPhenotype().createMetric().calculateMetric(example.room);
            double other_metric = other.getPhenotype().createMetric().calculateMetric(example.room);
            if(this_metric > other_metric + 0.05 || this_metric < other_metric - 0.05)
            {
                return false;
            }
        }

        return true;
    }

    protected void FilterChromosomes()
    {
        boolean nothingToFilter = false;
        int counter = 0;
        int limit = this.getGenotype().getChromosomes().size();
        int duplicates = 0;

        while(limit-- > 0)
        {
            List<MetricChromosome> to_remove = new ArrayList<>();

            if(counter >=  this.getGenotype().getChromosomes().size())
                counter =  this.getGenotype().getChromosomes().size() - 1;

            for(int i = 0; i <  this.getGenotype().getChromosomes().size(); i++)
            {
                if(i == counter)
                    continue;

                if(this.getGenotype().getChromosomes().get(counter).equals(this.getGenotype().getChromosomes().get(i)))
                {
                    to_remove.add(this.getGenotype().getChromosomes().get(i));
                }
            }
            counter++;
            duplicates += to_remove.size();
            this.getGenotype().getChromosomes().removeAll(to_remove);
        }

        //Check the duplicates
        duplicates += 1;

    }
}

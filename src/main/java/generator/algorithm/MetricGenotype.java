package generator.algorithm;

import game.Room;
import game.narrative.GrammarGraph;
import generator.algorithm.MAPElites.GADimensionsGranularity;
import util.Util;

import java.util.ArrayList;

public class MetricGenotype
{
    //Chromosome is always a
    private ArrayList<MetricChromosome> chromosomes;
    int maxRandomInitNumber = 5;

    public MetricGenotype()
    {
        this.chromosomes = new ArrayList<MetricChromosome>();
        ProduceGenotype();
    }

    public MetricGenotype(ArrayList<MetricChromosome> chromosomes)
    {
        this.chromosomes = new ArrayList<MetricChromosome>();

        for(MetricChromosome chromosome : chromosomes)
        {
            this.chromosomes.add(new MetricChromosome(chromosome.weight, chromosome.functionType, chromosome.function));
        }

//        ProduceGenotype();
    }

    public void ProduceGenotype()
    {
        int rnd = Util.getNextInt(2, maxRandomInitNumber + 1);

        while(rnd != 0)
        {
            rnd--;

            float w = Util.getNextFloat(-1.0f, 1.0f);
            int func_type = Util.getNextInt(0, 2);
            int func = func_type == 0 ?  Util.getNextInt(0, 5) : Util.getNextInt(0, 28);

            chromosomes.add(new MetricChromosome(w, func_type, func));
        }
    }

    public MetricChromosome[] getMetricsToExchange(int starting_pos, int amount)
    {
        MetricChromosome[] exchange_chromosomes = new MetricChromosome[amount];
        int ind = 0;
        for(int i = starting_pos; i < starting_pos + amount; i++, ind++)
        {
            try
            {
                exchange_chromosomes[ind] = this.chromosomes.get(i);
            }
            catch(Exception e)
            {
                System.out.println("OUT OF BOUNDS");
            }
        }

        return  exchange_chromosomes;
    }

    public void exchangeMetrics(int starting_pos, int amount, MetricChromosome[] other_chromosomes)
    {//FIXME: Here it needs some fix!
        int ind = 0;
        for(int i = starting_pos; i < starting_pos + amount ; i++, ind++)
        {
            this.chromosomes.set(i, new MetricChromosome(
                    other_chromosomes[ind].weight,
                    other_chromosomes[ind].functionType,
                    other_chromosomes[ind].function));

//            this.chromosomes.get(i) =
        }
    }

    public void addFunc()
    {
        float w = Util.getNextFloat(-1.0f, 1.0f);
        int func_type = Util.getNextInt(0, 2);
        int func = func_type == 0 ?  Util.getNextInt(0, 5) : Util.getNextInt(0, 28);

        this.chromosomes.add(new MetricChromosome(w, func_type, func));
    }

    public void removeFunc()
    {
        if(getSizeChromosome() < 2)
            return;

        int rndPos = Util.getNextInt(0, getSizeChromosome());
        this.chromosomes.remove(rndPos);
    }

    public void alterWeight()
    {
        int rndPos = Util.getNextInt(0, getSizeChromosome());
        this.chromosomes.get(rndPos).weight = Util.getNextFloat(-1.0f, 1.0f);
    }

    //TODO: Maybe check for changing the func as well!
    public void alterFuncType()
    {
        int rndPos = Util.getNextInt(0, getSizeChromosome());
        this.chromosomes.get(rndPos).functionType = this.chromosomes.get(rndPos).functionType == 0 ? 1 : 0;
//
//        int prev_func_type =  this.chromosomes.get(rndPos).functionType;
//        this.chromosomes.get(rndPos).functionType = Util.getNextInt(0, 2);
//
//        if(this.chromosomes.get(rndPos).functionType != prev_func_type)
//        {
//
//        }

    }

    public void alterFunc()
    {
        int rndPos = Util.getNextInt(0, getSizeChromosome());

//        int func = func_type == 0 ?  Util.getNextInt(0, 5) : Util.getNextInt(0, 28);

        this.chromosomes.get(rndPos).function =  this.chromosomes.get(rndPos).functionType == 0 ?  Util.getNextInt(0, 5) : Util.getNextInt(0, 28);
    }



    /**
     * Set chromosome
     *
     * @param chromosome Chromosome
     */
    public void setChromosome(ArrayList<MetricChromosome> other_chromosomes)
    {
        for (MetricChromosome oc: other_chromosomes)
        {
            chromosomes.add(new MetricChromosome(oc.weight, oc.functionType, oc.function));
        }
    }

    /**
     * Get chromosome
     *
     * @return Chromosome
     */
    public ArrayList<MetricChromosome> getChromosomes(){
        return chromosomes;
    }

    /**
     * Get the length of the chromosome
     *
     * @return Chromosome length
     */
    public int getSizeChromosome(){
        return chromosomes.size();
    }
}

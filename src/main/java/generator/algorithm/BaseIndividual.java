package generator.algorithm;

import generator.config.GeneratorConfig;

public class BaseIndividual<T extends BaseGenotype<?>, T2 extends Phenotype> //It has to be something like this
{
	T genotype;
	T2 phenotype;
	
	
	private double fitness;
	
	//TODO: Reconsider these...
	private double treasureAndEnemyFitness;
    private double roomFitness;
	private double corridorFitness;
	private double roomArea;
	private double corridorArea;
	
//	private Genotype genotype;
//	private Phenotype phenotype;
	private boolean evaluate;
	private float mutationProbability;
	private GeneratorConfig config;
	
	public BaseIndividual()
	{
		//BaseIndividual<BaseGenotype<?>, Phenotype> a = new BaseIndividual<BaseGenotype<int[]>, Phenotype>();
	}
}

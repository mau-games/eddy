package generator.algorithm;

import generator.config.GeneratorConfig;

public abstract class BaseGenotype<Genome>
{
	private Genome chromosome;
	private GeneratorConfig config;
	
	public BaseGenotype(GeneratorConfig config, Genome chromosome){
		this.config = config;
		setChromosome(chromosome);
	}
	
	public BaseGenotype(GeneratorConfig config, int size){
		this.config = config;
	}
	
	/**
	 * Get chromosome
	 * 
	 * @return Chromosome
	 */
	public Genome getChromosome(){
		return chromosome;
	}
	
	/**
	 * Set chromosome
	 * 
	 * @param chromosome Chromosome
	 */
	public void setChromosome(Genome chromosome){
		this.chromosome = chromosome;
	}
	
	/**
	 * Get the length of the chromosome
	 * 
	 * @return Chromosome length
	 */
	public abstract int getSizeChromosome();

	/**
	 * Generates a random chromosome where genes are chosen based on Ranges.getSupervisedRandomType()
	 */
	public abstract void randomSupervisedChromosome();
}

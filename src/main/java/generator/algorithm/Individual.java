package generator.algorithm;

import util.Util;

/**
 * Represents a member of Eddy's dungeon level population
 * TODO: Not so sold on how mutationProbability is passed around here
 * 
 * @author Alexander Baldwin, Malmö Högskola
 *
 */
public class Individual {
	private double fitness;
	private Genotype genotype;
	private Phenotype phenotype;
	private boolean evaluate;
	private float mutationProbability;
	
	public Individual(int size, float mutationProbability) {
		this(new Genotype(size), mutationProbability);
	}
	
	public Individual(Genotype genotype, float mutationProbability){
		this.genotype = genotype;
		this.phenotype = null;
		this.fitness = 0.0;
		this.evaluate = false;
		this.mutationProbability = mutationProbability;
	}
	
	/**
	 * Generate a genotype
	 * 
	 */
	public void initialize() {
		genotype.randomSupervisedChromosome();
	}
	
	/**
	 * Two point crossover between two individuals.
	 * 
	 * @param other An Individual to reproduce with.
	 * @return An array of offspring resulting from the crossover.
	 */
	public Individual[] twoPointCrossover(Individual other){
		Individual[] children = new Individual[2];
		children[0] = new Individual(new Genotype(genotype.getChromosome().clone()), mutationProbability);
		children[1] = new Individual(new Genotype(other.getGenotype().getChromosome().clone()), mutationProbability);
		
		int lowerBound = Util.getNextInt(0, genotype.getSizeChromosome());
		int upperBound = Util.getNextInt(lowerBound, genotype.getSizeChromosome());
		
		for(int i = lowerBound; i <= upperBound; i++){
			//exchange
			children[0].getGenotype().getChromosome()[i] = other.getGenotype().getChromosome()[i];
			children[1].getGenotype().getChromosome()[i] = genotype.getChromosome()[i];
		}
		
		//mutate
		for(int i = 0; i < 2; i++){
			if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability)
				children[i].mutate();
		}
		
		return children;
	}
	
	
	/**
	 * Mutate ONE bit of the chromosome
	 */
	public void mutate() {
		int indexToMutate = Util.getNextInt(0,genotype.getSizeChromosome());
		boolean bit = genotype.getChromosome()[indexToMutate] == 0;
		genotype.getChromosome()[indexToMutate] = bit ? 1 : 0;
	}
	
	
	/**
	 * Mutate each bit of the chromosome with a small probability
	 */
	public void bitStringMutation(){
		for(int i = 0; i < genotype.getSizeChromosome(); i++){
			if(Math.random() < 0.5){
				int bit = genotype.getChromosome()[i];
				genotype.getChromosome()[i] = bit == 0 ? 1 : 0;
			}
		}
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
	public Genotype getGenotype(){
		return genotype;
	}
	
	/**
	 * Get phenotype
	 * 
	 * @return Phenotype
	 */
	public Phenotype getPhenotype(){
		if(phenotype == null){
			phenotype = new Phenotype(genotype);
		}
		return phenotype;
	}
	
}

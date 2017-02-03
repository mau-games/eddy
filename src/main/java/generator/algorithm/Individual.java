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
	private double mFitness;
	private Genotype mGenotype;
	private Phenotype mPhenotype;
	private boolean mEvaluate;
	private float mutationProbability;
	
	public Individual(int size, float mutationProbability) {
		this(new Genotype(size), mutationProbability);
	}
	
	public Individual(Genotype genotype, float mutationProbability){
		mGenotype = genotype;
		mPhenotype = null;
		mFitness = 0.0;
		mEvaluate = false;
		this.mutationProbability = mutationProbability;
	}
	
	public Individual initialize() {
		mGenotype.randomSupervisedChromosome();
		return this;
	}
	
	/**
	 * Two point crossover between two individuals.
	 * 
	 * @param other An Individual to reproduce with.
	 * @return An array of offspring resulting from the crossover.
	 */
	public Individual[] twoPointCrossover(Individual other){
		Individual[] children = new Individual[2];
		children[0] = new Individual(new Genotype(mGenotype.getChromosome().clone()), mutationProbability);
		children[1] = new Individual(new Genotype(other.getGenotype().getChromosome().clone()), mutationProbability);
		
		int lowerBound = Util.getNextInt(0, mGenotype.getSizeChromosome());
		int upperBound = Util.getNextInt(lowerBound, mGenotype.getSizeChromosome());
		
		for(int i = lowerBound; i <= upperBound; i++){
			//exchange
			children[0].getGenotype().getChromosome()[i] = other.getGenotype().getChromosome()[i];
			children[1].getGenotype().getChromosome()[i] = mGenotype.getChromosome()[i];
		}
		
		//mutate
		for(int i = 0; i < 2; i++){
			if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability)
				children[i].mutate();
		}
		
		return children;
	}
	
//	public Individual[] reproduce(Individual other){
//		Individual[] children = new Individual[2];
//		children[0] = new Individual(new Genotype(mGenotype.getChromosome().clone()), mutationProbability);
//		children[1] = new Individual(new Genotype(other.getGenotype().getChromosome().clone()), mutationProbability);
//		
//		if(mGenotype.getSizeChromosome() == other.getGenotype().getSizeChromosome()){
//			int bitsCountToExchange = Util.getNextInt(2,mGenotype.getSizeChromosome());
//			
//			for(int i = 0; i < bitsCountToExchange; i++){
//				int bitIndexToExchange = Util.getNextInt(0,mGenotype.getSizeChromosome());
//				int bitIndividualOne = mGenotype.getChromosome()[bitIndexToExchange];
//				int bitIndividualTwo = other.getGenotype().getChromosome()[bitIndexToExchange];
//				
//				//exchange
//				children[0].getGenotype().getChromosome()[bitIndexToExchange] = bitIndividualTwo;
//				children[1].getGenotype().getChromosome()[bitIndexToExchange] = bitIndividualOne;
//			}
//			
//			//mutate
//			for(int i = 0; i < 2; i++){
//				if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability)
//					children[i].mutate();
//			}
//			
//			return children;
//		}
//		
//		return null;
//	}
	
	
	//Uniform mutation?
	public void mutate() {
		int indexToMutate = Util.getNextInt(0,mGenotype.getSizeChromosome());
		boolean bit = mGenotype.getChromosome()[indexToMutate] == 0;
		mGenotype.getChromosome()[indexToMutate] = bit ? 1 : 0;
	}
	
	
	/**
	 * Another attempt at mutation?
	 */
	public void bitStringMutation(){
		for(int i = 0; i < mGenotype.getSizeChromosome(); i++){
			if(Math.random() < 0.5){
				int bit = mGenotype.getChromosome()[i];
				mGenotype.getChromosome()[i] = bit == 0 ? 1 : 0;
			}
		}
	}
	
	public double getFitness(){
		return mFitness;
	}
	
	public void setFitness(double fitness){
		mFitness = fitness;
	}
	
	// Returns true if the fitness of this individual has already been evaluated
	public boolean isEvaluated() {
		return mEvaluate;
	}
	
	public void setEvaluate(boolean evaluate){
		mEvaluate = evaluate;
	}
	
	public Genotype getGenotype(){
		return mGenotype;
	}
	
	public Phenotype getPhenotype(){
		if(mPhenotype == null){
			mPhenotype = new Phenotype(mGenotype);
		}
		return mPhenotype;
	}
	
	public void setPhenotype(Phenotype phenotype){
		mPhenotype = phenotype;
	}
	
	public void printGenotype() {
		mGenotype.print();
	}
}

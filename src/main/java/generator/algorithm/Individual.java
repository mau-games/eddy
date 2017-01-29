package generator.algorithm;

import game.Game;
import util.Util;

public class Individual {
	private double mFitness;
	private Genotype mGenotype;
	private Phenotype mPhenotype;
	private boolean mEvaluate;
	
	public Individual(int size) {
		mGenotype = new Genotype(size);
		mPhenotype = null;
		mFitness = 0.0;
		mEvaluate = false;
	}
	
	public Individual(Genotype genotype){
		mGenotype = genotype;
		mPhenotype = null;
		mFitness = 0.0;
		mEvaluate = false;
	}
	
	public Individual initialize() {
		mGenotype.randomSupervisedChromosome();
		return this;
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
	
	//Multi-point crossover????
	public Individual[] reproduce(Individual other){
		Individual[] sons = new Individual[2];
		sons[0] = new Individual(new Genotype(mGenotype.getChromosome()));
		sons[1] = new Individual(new Genotype(other.getGenotype().getChromosome()));
		
		if(mGenotype.getSizeChromosome() == other.getGenotype().getSizeChromosome()){
			int bitsCountToExchange = Util.getNextInt(2,mGenotype.getSizeChromosome());
			
			for(int i = 0; i < bitsCountToExchange; i++){
				int bitIndexToExchange = Util.getNextInt(0,mGenotype.getSizeChromosome());
				int bitIndividualOne = mGenotype.getChromosome()[bitIndexToExchange];
				int bitIndividualTwo = other.getGenotype().getChromosome()[bitIndexToExchange];
				
				//exchange
				sons[0].getGenotype().getChromosome()[bitIndexToExchange] = bitIndividualTwo;
				sons[1].getGenotype().getChromosome()[bitIndexToExchange] = bitIndividualOne;
			}
			
			//mutate
			for(int i = 0; i < 2; i++){
				if(Util.getNextFloat(0.0f,1.0f) <= Algorithm.MUTATION_PROB)
					sons[i].mutate();
			}
			
			return sons;
		}
		
		return null;
	}
	
	
	//Uniform mutation?
	//TODO: CHECK THAT THIS WORKS IN A REASONABLE WAY
	public void mutate() {
		int indexToMutate = Util.getNextInt(0,mGenotype.getSizeChromosome());
		boolean bit = mGenotype.getChromosome()[indexToMutate] == 0;
		mGenotype.getChromosome()[indexToMutate] = bit ? 1 : 0;
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
	
	public void setPhenotype(Phenotype phenotype){
		mPhenotype = phenotype;
	}
	
	public void printGenotype() {
		mGenotype.print();
	}
}

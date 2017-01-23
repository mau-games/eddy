package generator.algorithm;

import game.Game;

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
	}
	
	//Multi-point crossover????
	public Individual[] reproduce(Individual other){
		Individual[] sons = new Individual[2];
		sons[0] = new Individual(new Genotype(mGenotype.getChromosome()));
		sons[1] = new Individual(new Genotype(other.getGetotype().getChromosome()));
		
		if(mGenotype.getSizeChromosome() == other.getGenotype().getSizeChromosome()){
			int bitsCountToExchange = Game.getRanges().getNextInt(2,mGenotype.getSizeChromosome());
			
			for(int i = 0; i < bitsCountToExchange; i++){
				int bitIndexToExchange = Game.getRanges().getNextInt(0,mGenotype.getSizeChromosome());
				int bitIndividualOne = mGeotype.getChromosome()[bitIndexToExchange];
				int bitIndividualTwo = other.getGenotype().getChromosome()[bitIndexToExchange];
				
				//exchange
				sons[0].getGenotype().getChromosome()[bitIndexToExchange] = bitIndividualTwo;
				sons[1].getGenotype().getChromosome()[bitIndexToExchange] = bitIndividualOne;
			}
			
			//mutate
			for(int i = 0; i < 2; i++){
				if(Game.getRanges().getNextFloat(0.0f,1.0f) <= Algorithm.MUTATION_PROB)
					sons[i].mutate();
			}
			
			return sons;
		}
		
		return null;
	}
	
	
	//Uniform mutation?
	//TODO: CHECK THAT THIS WORKS IN A REASONABLE WAY
	public void mutate() {
		int indexToMutate = Game.getRanges().getNextInt(0,mGenotype.getSizeChromosome());
		boolean bit = mGenotype.getChromosome()[indexToMutate] == 0;
		mGenotype.getChromosome()[indexToMutate] = bit ? 1 : 0;
	}
	
	public double getFitness(){
		return mFitness;
	}
	
	public void setFitness(double fitness){
		mFitness = fitness;
	}
	
	public boolean isEvaluate() {
		return mEvaluate;
	}
	
	public void setEvaluate(bool evaluate){
		mEvaluate = evaluate;
	}
	
	public void setPhenotype(Phenotype phenotype){
		mPhenotype = phenotype;
	}
	
	public void printGenotype() {
		mGenotype.print();
	}
}

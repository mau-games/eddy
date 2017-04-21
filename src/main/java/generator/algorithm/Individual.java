package generator.algorithm;

import game.Game;
import generator.config.GeneratorConfig;
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
	
	//TODO: Reconsider these...
	private double treasureAndEnemyFitness;
    private double roomFitness;
	private double corridorFitness;
	private double roomArea;
	private double corridorArea;
	
	private Genotype genotype;
	private Phenotype phenotype;
	private boolean evaluate;
	private float mutationProbability;
	private GeneratorConfig config;
	
	private boolean childOfInfeasibles = false;
	
	public void setTreasureAndEnemyFitness(double treasureAndEnemyFitness){
		this.treasureAndEnemyFitness = treasureAndEnemyFitness;
	}
	public void setRoomFitness(double roomFitness){
		this.roomFitness = roomFitness;
	}
	public void setCorridorFitness(double corridorFitness){
		this.corridorFitness = corridorFitness;
	}
	
	/**
	 * Room area NOT weighted by quality
	 * @param roomArea
	 */
	public void setRoomArea(double roomArea){
		this.roomArea = roomArea;
	}
	
	/**
	 * Corridor area NOT weighted by quality
	 * @param corridorArea
	 */
	public void setCorridorArea(double corridorArea){
		this.corridorArea = corridorArea;
	}
	
	public double getTreasureAndEnemyFitness(){
		return treasureAndEnemyFitness;
	}
	public double getRoomFitness(){
		return roomFitness;
	}
	public double getCorridorFitness(){
		return corridorFitness;
	}
	
	public boolean isChildOfInfeasibles(){
		return childOfInfeasibles;
	}
	
	public void setChildOfInfeasibles(boolean cOI){
		childOfInfeasibles = cOI;
	}
	
	/**
	 * Room area NOT weighted by quality
	 * @param roomArea
	 * @return
	 */
	public double getRoomArea(){
		return roomArea;
	}
	
	/**
	 * Corridor area NOT weighted by quality
	 * @param corridorArea
	 * @return
	 */
	public double getCorridorArea(){
		return corridorArea;
	}
	
	public Individual(GeneratorConfig config, int size, float mutationProbability) {
		this(config, new Genotype(config,size), mutationProbability);
	}
	
	public Individual(GeneratorConfig config, Genotype genotype, float mutationProbability){
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
		children[0] = new Individual(config, new Genotype(config, genotype.getChromosome().clone()), mutationProbability);
		children[1] = new Individual(config, new Genotype(config, other.getGenotype().getChromosome().clone()), mutationProbability);
		
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
				if(Util.getNextFloat(0, 1) <= 0.8f)
					children[i].mutate();
				else
					children[i].mutateRotate180();
		}
		
		return children;
	}
	
	public Individual[] rectangularCrossover(Individual other){
		Individual[] children = new Individual[2];
		children[0] = new Individual(config, new Genotype(config, genotype.getChromosome().clone()), mutationProbability);
		children[1] = new Individual(config, new Genotype(config, other.getGenotype().getChromosome().clone()), mutationProbability);
		
		int lowerBoundM = Util.getNextInt(0, Game.sizeM);
		int upperBoundM = Util.getNextInt(lowerBoundM, Game.sizeM);
		int lowerBoundN = Util.getNextInt(0, Game.sizeN);
		int upperBoundN = Util.getNextInt(lowerBoundN, Game.sizeN);
		
		for(int i = lowerBoundM; i <= upperBoundM; i++){
			for(int j = lowerBoundN; j <= upperBoundN; j++){
				//exchange
				children[0].getGenotype().getChromosome()[j*Game.sizeM + i] = other.getGenotype().getChromosome()[j*Game.sizeM + i];
				children[1].getGenotype().getChromosome()[j*Game.sizeM + i] = genotype.getChromosome()[j*Game.sizeM + i];
			}
		}
		
		//mutate
		for(int i = 0; i < 2; i++){
			if(Util.getNextFloat(0.0f,1.0f) <= mutationProbability)
				if(Util.getNextFloat(0, 1) <= 0.8f)
					children[i].mutate();
				else
					children[i].mutateRotate180();
		}
		
		return children;
		
	}
	
	
	/**
	 * Mutate ONE bit of the chromosome
	 */
	public void mutate() {
		int indexToMutate = Util.getNextInt(0,genotype.getSizeChromosome());
		genotype.getChromosome()[indexToMutate] = (genotype.getChromosome()[indexToMutate] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
	}
	
	/**
	 * Mutate each bit of the chromosome with a small probability
	 */
	public void mutateAll(){
		for(int i = 0; i < genotype.getSizeChromosome(); i++){
			if(Math.random() < 0.01){
				genotype.getChromosome()[i] = (genotype.getChromosome()[i] + Util.getNextInt(0, 4)) % 4; //TODO: Change this - hard coding the number of tile types is bad!!!
			}
		}
	}
	
	private void mutateRotate180(){
		int[] chromosomeCopy = genotype.getChromosome().clone();
		for(int i = 0; i < Game.sizeM; i++)
			for(int j = 0; j < Game.sizeN; j++)
				genotype.getChromosome()[j*Game.sizeM + i] = chromosomeCopy[(Game.sizeN - 1 - j)*Game.sizeM + Game.sizeM - 1 - i];
	}
	
	private void mutateRotate90(){
		int[] chromosomeCopy = genotype.getChromosome().clone();
		for(int i = 0; i < Game.sizeM; i++)
			for(int j = 0; j < Game.sizeN; j++)
				genotype.getChromosome()[j*Game.sizeM + i] = chromosomeCopy[(Game.sizeN - 1 - i)*Game.sizeM + Game.sizeM - 1 - j];
	}
	
	

	
	public double getDistance(Individual other){
		int[] thisChromosome = this.getGenotype().getChromosome();
		int[] otherChromosome = other.getGenotype().getChromosome();
		
		int match = 0;
		for(int i = 0; i < thisChromosome.length;i++)
			if(thisChromosome[i] == otherChromosome[i]) match++;
		
		return (double)(thisChromosome.length-match)/thisChromosome.length;
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

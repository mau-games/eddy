package generator.algorithm;

import game.Game;
import game.TileTypes;

public class Genotype {
	private int[] chromosome;
	
	public Genotype(int[] chromosome){
		setChromosome(chromosome);
	}
	
	public Genotype(int size){
		chromosome = new int[size];
	}
	
	/**
	 * Get chromosome
	 * 
	 * @return Chromosome
	 */
	public int[] getChromosome(){
		return chromosome;
	}
	
	/**
	 * Set chromosome
	 * 
	 * @param chromosome Chromosome
	 */
	public void setChromosome(int[] chromosome){
		this.chromosome = chromosome;
	}
	
	/**
	 * Get the length of the chromosome
	 * 
	 * @return Chromosome length
	 */
	public int getSizeChromosome(){
		return chromosome.length;
	}

	/**
	 * Generates a random chromosome where genes are chosen based on Ranges.getSupervisedRandomType()
	 */
	public void randomSupervisedChromosome() {
		int i = 0;
		while(i < chromosome.length){
			chromosome[i++] = Game.getRanges().getSupervisedRandomType().getValue();
		}
	}
}

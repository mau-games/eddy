package generator.algorithm;

import game.Game;
import game.TileTypes;

public class Genotype {
	private int[] chromosome;
	private final int BITS_PER_GENE = 3;
	
	public Genotype(int[] chromosome){
		setChromosome(chromosome);
	}
	
	public Genotype(int size){
		chromosome = new int[size * BITS_PER_GENE];
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
	 * Get the number of bits in the chromosome
	 * 
	 * @return Number of bits
	 */
	public int getSizeChromosome(){
		return chromosome.length;
	}
	
	/**
	 * Get the number of bits per gene
	 * 
	 * @return Bits per gene
	 */
	public int getChromosomeItemBits(){
		return BITS_PER_GENE;
	}
	
	/**
	 * Generates a random chromosome where genes are chosen based on Ranges.getSupervisedRandomType()
	 */
	public void randomSupervisedChromosome() {
		int i = 0;
		while(i < chromosome.length){
			TileTypes type = Game.getRanges().getSupervisedRandomType();
			
			String type_binary = toBinary(type);
			
			for (char c : type_binary.toCharArray()){
				chromosome[i] = Character.getNumericValue(c);
				i++;
			}
		}
	}
	
	/**
	 * Add the given number of zeroes to the left of a string
	 * 
	 * @param str Starting string
	 * @param zeros Number of zeroes to add
	 * @return The string with added zeroes
	 */
	private String addZeroToLeft(String str, int zeros)
	{
		for(int i = zeros; i > 0; i--)
			str = '0' + str;
		return str;
	}
	
	/**
	 * Convert a TileTypes to a binary string
	 * 
	 * @param type TileTypes to convert
	 * @return Converted binary string.
	 */
	private String toBinary(TileTypes type){
		String type_binary = Integer.toBinaryString(type.ordinal());
		return addZeroToLeft(type_binary, BITS_PER_GENE - type_binary.length());
	}
}

package generator.algorithm;

import game.Game;
import game.TileTypes;
import junit.framework.Assert;

public class Genotype {
	private int[] mChromosome; // TODO: Couldn't this more compactly be stored as an array of bytes?
	private int mSizeChromosome; // (Number of bits) * (bits per gene)
	private int mBits = 3; //The number of bits in a gene
	
	public Genotype(int[] mChromosome){
		setChromosome(mChromosome);
		mSizeChromosome = mChromosome.length;
	}
	
	//Size passed: n x n
	public Genotype(int sizeTileMap){
		mSizeChromosome = sizeTileMap * mBits;
		mChromosome = new int[mSizeChromosome];
	}
	
	public int[] getChromosome(){
		return mChromosome;
	}
	
	public void setChromosome(int[] chromosome){
		mChromosome = chromosome;
	}
	
	public int getSizeChromosome(){
		return mSizeChromosome;
	}
	
	public int getChromosomeItemBits(){
		return mBits;
	}
	
	/**
	 * Generates a random chromosome where genes are chosen based on Ranges.getSupervisedRandomType()
	 */
	public void randomSupervisedChromosome() {
		int i = 0;
		while(i < mSizeChromosome){
			TileTypes type = Game.getRanges().getSupervisedRandomType();
			
			String type_binary = toBinary(type);
			
			for (char c : type_binary.toCharArray()){
				mChromosome[i] = Character.getNumericValue(c);
				i++;
			}
		}
	}
	
//	public void testZeroToLeft()
//	{
//		String str = "1";
//		String str_with_zero = addZeroToLeft(str,2);
//		
//		//Assert.assertEquals("001", str_with_zero);
//		System.out.println("Test: " + str_with_zero);
//	}
	
	private String addZeroToLeft(String str, int zeros)
	{
		for(int i = zeros; i > 0; i--)
			str = '0' + str;
		return str;
	}
	
	private String toBinary(TileTypes type){
		String type_binary = Integer.toBinaryString(type.ordinal());
		return addZeroToLeft(type_binary, mBits - type_binary.length());
	}
	
	public void print(){
		String strOut = "|";
		for(int i = 0; i < mSizeChromosome; i++){
			strOut += Integer.toString(mChromosome[i]);
			if(((i+1) % 3) == 0)
				strOut += "|";
		}
		
		System.out.println(strOut);
	}
}

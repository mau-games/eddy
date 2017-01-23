package generator.algorithm;

import game.TileTypes;
import junit.framework.Assert;

public class Genotype {
	private int[] mChromosome;
	private int mSizeChromosome;
	private int mSizeItem;
	private int mBits = 3; //TODO: investigate this a bit
	
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
	
	public void randomSupervisedChromosome() {
		int i = 0;
		while(i < mSizeChromosome){
			TileTypes type = Game.getRanges().getSupervisedRandomType();
			
			String type_binary = toBinary(type);
			
			for (char c : type_binary.toCharArray()){ //TODO: Make sure this works
				mChromosome[i] = Character.getNumericValue(c); //TODO: And this! (rewritten a bit)
				i++;
			}
		}
	}
	
	public void testZeroToLeft()
	{
		String str = "1";
		String str_with_zero = addZeroToLeft(str,2);
		
		//Assert.assertEquals("001", str_with_zero);
		System.out.println("Test: " + str_with_zero);
	}
	
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
	
	//TODO: figure out what is REALLY being printed here
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

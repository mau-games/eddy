package generator.algorithm;

import game.Game;
import game.Map;
import game.TileTypes;
import generator.config.GeneratorConfig;
import game.ZoneNode;

import java.util.Arrays;
import java.util.List;

public class ZoneGenotype
{
	private int[] chromosome;
	private GeneratorConfig config;
	private ZoneNode m;
	
	public ZoneGenotype(GeneratorConfig config, int[] chromosome, ZoneNode rootM){
		this.config = config;
		setChromosome(chromosome);
		m = new ZoneNode(rootM);
	}
	
	public ZoneGenotype(GeneratorConfig config, int size){
		this.config = config;
		chromosome = new int[size];
	}
	
	public void ProduceGenotype(Map map)
	{
		int[][] mat = map.toMatrix();
		
		for(int i = 0; i < map.getRowCount(); i++)
		{
			for(int j = 0; j <map.getColCount();j++)
			{
				chromosome[i*Game.sizeWidth + j] = mat[i][j];
			}
		}
		
		m = new ZoneNode(map.root);
	}
	
	public ZoneNode GetRootChromosome()
	{
		return m;
	}
	
	public void SetRootChromosome(ZoneNode zn)
	{
		m = zn;
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
			chromosome[i++] = config.getSupervisedRandomType().getValue();
		}
//		
//		//THERE NEED TO BE NICER WAYS TO DO SUCH A THING
//		TileTypes[] tileTypes = Arrays.stream(getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
//		Map map = new Map(config, tileTypes, Game.sizeHeight, Game.sizeWidth, Game.doorCount);
//		m = map.root;
	}
}

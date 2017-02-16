package generator.algorithm;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.Map;
import game.TileTypes;

public class Phenotype {
	Genotype genotype;
	Map map;
	
	public Phenotype(Genotype genotype){
		this.genotype = genotype;
		map = null;
	}
	
	public Phenotype(Map map){
		genotype = null;
		this.map = map;
	}
	
	/**
	 * Generates a Map from the Genotype
	 * 
	 * @return The Map for this Genotype
	 */
	public Map getMap() {
		if(map == null){
			int size = genotype.getSizeChromosome() / genotype.getChromosomeItemBits();
			List<Integer> genes = new ArrayList<Integer>(size);
			for(int i = 0; i < size; i++){
				
				String binaryNumber = "";
				for(int j = 0; j < genotype.getChromosomeItemBits(); j++){
					binaryNumber += Integer.toString(genotype.getChromosome()[i*3 + j]);
				}
				int decimalNumber = Integer.parseInt(binaryNumber,2);
				
				genes.add(decimalNumber);
			}
			
			TileTypes[] types = genes.stream().map(x -> TileTypes.values()[x]).toArray(TileTypes[]::new);
			
			map = new Map(types, Game.sizeN, Game.sizeM, Game.doorCount);
		}
		return map;
	}
}

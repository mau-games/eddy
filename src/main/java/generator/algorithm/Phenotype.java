package generator.algorithm;


import java.util.Arrays;
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
			TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
			map = new Map(tileTypes, Game.sizeN, Game.sizeM, Game.doorCount);
		}
		return map;
	}
}

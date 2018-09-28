package generator.algorithm;


import java.util.Arrays;
import game.Game;
import game.Map;
import game.TileTypes;
import generator.config.GeneratorConfig;

public class ZonePhenotype {
	private ZoneGenotype genotype;
	private Map map;
	private GeneratorConfig config;
	
	public ZonePhenotype(GeneratorConfig config, ZoneGenotype genotype){
		this.config = config;
		this.genotype = genotype;
		map = null;
	}
	
	public ZonePhenotype(Map map){
		genotype = null;
		this.map = map;
	}
	
	/**
	 * Generates a Map from the Genotype
	 * 
	 * @return The Map for this Genotype
	 */
	public Map getMap() {
		if(map == null)
		{
			if(genotype.GetRootChromosome() != null)
			{
				map = new Map(config, genotype.GetRootChromosome(), genotype.getChromosome(), Game.sizeHeight, Game.sizeWidth, Game.doorCount);
//				TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
//				map = new Map(config, tileTypes, Game.sizeHeight, Game.sizeWidth, Game.doorCount);
				genotype.SetRootChromosome(map.root);
			}
			else
			{
				TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
				map = new Map(config, tileTypes, Game.sizeHeight, Game.sizeWidth, Game.doorCount);
				genotype.SetRootChromosome(map.root);
			}
			
			genotype.ProduceGenotype(map);
		}
		
		return map;
	}
}

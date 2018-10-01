package generator.algorithm;


import java.util.Arrays;
import game.Game;
import game.Room;
import game.TileTypes;
import generator.config.GeneratorConfig;

public class ZonePhenotype {
	private ZoneGenotype genotype;
	private Room room;
	private GeneratorConfig config;
	
	public ZonePhenotype(GeneratorConfig config, ZoneGenotype genotype){
		this.config = config;
		this.genotype = genotype;
		room = null;
	}
	
	public ZonePhenotype(Room room){
		genotype = null;
		this.room = room;
	}
	
	/**
	 * Generates a Map from the Genotype
	 * 
	 * @return The Map for this Genotype
	 */
	public Room getMap() {
		if(room == null)
		{
			if(genotype.GetRootChromosome() != null)
			{
				room = new Room(config, genotype.GetRootChromosome(), genotype.getChromosome(), Game.sizeHeight, Game.sizeWidth, Game.doorCount);
//				TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
//				map = new Map(config, tileTypes, Game.sizeHeight, Game.sizeWidth, Game.doorCount);
				genotype.SetRootChromosome(room.root);
			}
			else
			{
				TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
				room = new Room(config, tileTypes, Game.sizeHeight, Game.sizeWidth, Game.doorCount);
				genotype.SetRootChromosome(room.root);
			}
			
			genotype.ProduceGenotype(room);
		}
		
		return room;
	}
}

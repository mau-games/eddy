package generator.algorithm;


import java.util.Arrays;
import java.util.List;

import game.Game;
import game.Room;
import game.Tile;
import game.TileTypes;
import generator.config.GeneratorConfig;
import util.Point;

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
	 * Generates a Map from the Genotype, is called from algorithm
	 * Therefore, algorithm should know size and doors
	 * 
	 * @return The Map for this Genotype
	 */
	public Room getMap(int width, int height, List<Point> doorPositions, List<Tile> customTiles) {
		if(room == null)
		{
			if(genotype.GetRootChromosome() != null)
			{
				room = new Room(config, genotype.GetRootChromosome(), genotype.getChromosome(), genotype.GetRootChromosome().getHeight(), genotype.GetRootChromosome().getWidth());
//				TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
//				map = new Map(config, tileTypes, Game.sizeHeight, Game.sizeWidth, Game.doorCount);
				genotype.SetRootChromosome(room.root);
			}
			else
			{
				TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
				room = new Room(config, tileTypes, height, width, doorPositions, customTiles);
				genotype.SetRootChromosome(room.root);
			}
			
			genotype.ProduceGenotype(room);
		}
		
		return room;
	}
}

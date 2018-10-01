package generator.algorithm;


import java.util.Arrays;
import game.Game;
import game.Room;
import game.TileTypes;
import generator.config.GeneratorConfig;

public class Phenotype {
	private Genotype genotype;
	private Room room;
	private GeneratorConfig config;
	
	public Phenotype(GeneratorConfig config, Genotype genotype){
		this.config = config;
		this.genotype = genotype;
		room = null;
	}
	
	public Phenotype(Room room){
		genotype = null;
		this.room = room;
	}
	
	/**
	 * Generates a Map from the Genotype
	 * 
	 * @return The Map for this Genotype
	 */
	public Room getMap() {
		if(room == null){
			TileTypes[] tileTypes = Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
			room = new Room(config, tileTypes, Game.sizeHeight, Game.sizeWidth, Game.doorCount);
		}
		return room;
	}
}
